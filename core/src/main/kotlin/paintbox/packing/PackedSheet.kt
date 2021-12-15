package paintbox.packing

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import paintbox.Paintbox
import paintbox.util.gdxutils.disposeQuietly
import java.io.File


/**
 * Represents a set of textures that are packed at runtime and addressable via an ID.
 */
class PackedSheet(val config: Config, initial: List<Packable> = emptyList())
    : TextureRegionMap, Disposable {

    private class PackResult(val atlas: TextureAtlas, val originalPackables: List<Packable>, val timeTaken: Double)
        : Disposable {

        val regions: Map<String, TextureAtlas.AtlasRegion> = atlas.regions.associateBy { it.name }
        val indexedRegions: Map<String, Map<Int, TextureAtlas.AtlasRegion>> = atlas.regions.filter { it.index >= 0 }
                .groupBy { it.name }
                .mapValues { (_, value) -> value.associateBy { r -> r.index } }

        override fun dispose() {
            atlas.dispose()
        }
    }

    private val packables: MutableMap<String, Packable> = mutableMapOf()
    private var atlas: PackResult? = null

    init {
        initial.forEach { p ->
            addPackable(p)
        }
    }

    fun addPackable(packable: Packable) {
        packables[packable.id] = packable
    }

    fun removePackable(id: String) {
        packables.remove(id)
    }

    fun removePackable(packable: Packable) {
        packables.remove(packable.id, packable)
    }

    fun pack() {
        atlas?.dispose()
        atlas = null

        val nano = System.nanoTime()
        val size = config.maxSize
        val packer = PixmapPacker(size, size, config.format, config.padding, config.duplicateBorder, config.packStrategy).also { packer ->
            packer.transparentColor = config.transparentColor
        }
        val packables = packables.values.toList()
        packables.forEach { p ->
            val tex = p.obtainTexture()
            val td = tex.textureData
            if (!td.isPrepared) td.prepare()
            val pixmap = td.consumePixmap()

            packer.pack(p.id, pixmap)

            if (td.disposePixmap()) {
                pixmap.dispose()
            }
            if (p.shouldDisposeTexture()) {
                tex.dispose()
            }
        }

        val newAtlas = TextureAtlas()
        packer.updateTextureAtlas(newAtlas, config.atlasMinFilter, config.atlasMagFilter, config.atlasMipMaps, config.atlasUseIndexing)

        packer.dispose()
        val endNano = System.nanoTime()
        val result = PackResult(newAtlas, packables, (endNano - nano) / 1_000_000.0)
        this.atlas = result
//        println("Took ${result.timeTaken} ms to pack ${packables.size} packables")

        val outputFile = config.debugOutputFile
        if (outputFile != null) {
            outputToFile(outputFile)
        }
    }
    
    fun outputToFile(outputFile: FileHandle) {
        val newAtlas = this.atlas?.atlas ?: return
        val textures = newAtlas.textures
        if (textures.size > 0) {
            val onlyOne = textures.size == 1
            textures.forEachIndexed { index, texture ->
                val file = if (onlyOne) outputFile else outputFile.sibling(outputFile.nameWithoutExtension() + ".${index}.png")
                val td = texture.textureData
                if (!td.isPrepared) {
                    td.prepare()
                }
                val pix = td.consumePixmap()
                PixmapIO.writePNG(file, pix)
                if (td.disposePixmap()) {
                    pix.disposeQuietly()
                }
            }
        }
    }

    override operator fun get(id: String): TextureAtlas.AtlasRegion {
        return getOrNull(id) ?: error("No atlas region found with ID $id")
    }

    override fun getOrNull(id: String): TextureAtlas.AtlasRegion? {
        val atlas = this.atlas
        return if (atlas != null) {
            atlas.regions[id]
        } else error("Atlas was not loaded. Call pack() first")
    }

    override fun getIndexedRegions(id: String): Map<Int, TextureAtlas.AtlasRegion> {
        return getIndexedRegionsOrNull(id) ?: error("No indexed map of atlas regions found with ID $id")
    }

    override fun getIndexedRegionsOrNull(id: String): Map<Int, TextureAtlas.AtlasRegion>? {
        val atlas = this.atlas
        return if (atlas != null) {
            atlas.indexedRegions[id]
        } else error("Atlas was not loaded. Call pack() first")
    }

    override fun dispose() {
        atlas?.dispose()
        atlas = null
    }

    data class Config(
            val maxSize: Int = 1024, val format: Pixmap.Format = Pixmap.Format.RGBA8888, val padding: Int = 2,
            val duplicateBorder: Boolean = true,
            val packStrategy: PixmapPacker.PackStrategy = PixmapPacker.GuillotineStrategy(),
            val atlasMinFilter: Texture.TextureFilter = Texture.TextureFilter.Nearest,
            val atlasMagFilter: Texture.TextureFilter = Texture.TextureFilter.Nearest,
            val atlasMipMaps: Boolean = false,
            val atlasUseIndexing: Boolean = true,
            val debugOutputFile: FileHandle? = null,
            val transparentColor: Color = Color(1f, 1f, 1f, 0f),
    )
}

/**
 * Used by [PackedSheet] to accept [Texture]s to pack.
 */
interface Packable {

    companion object {
        /**
         * Creates a [Packable] with the given ID and file handle.
         */
        operator fun invoke(id: String, fileHandle: FileHandle): Packable {
            return TemporaryPackableTex(id, fileHandle)
        }

        /**
         * Creates a [Packable] with the given ID and internal file path.
         */
        operator fun invoke(id: String, internalPath: String): Packable {
            return TemporaryPackableTex(id, internalPath)
        }
    }

    val id: String

    fun obtainTexture(): Texture

    fun shouldDisposeTexture(): Boolean

}

/**
 * Wraps a [Texture]. It is recommended to use the extension function [Texture.asPackable].
 */
class PackableTextureWrapper(override val id: String, val texture: Texture) : Packable {
    override fun obtainTexture(): Texture = this.texture

    override fun shouldDisposeTexture(): Boolean = false
}

/**
 * Wraps a [Texture] to be [Packable].
 */
fun Texture.asPackable(id: String): Packable = PackableTextureWrapper(id, this)

/**
 * An implementation of [Packable] that loads the [Texture] given by the [fileHandle], then disposes it immediately after.
 * It is recommended to use the [Packable.Companion.invoke] functions to retrieve implementations.
 */
class TemporaryPackableTex(override val id: String, val fileHandle: FileHandle) : Packable {

    constructor(id: String, internalPath: String) : this(id, Gdx.files.internal(internalPath))

    override fun obtainTexture(): Texture {
        return Texture(fileHandle)
    }

    override fun shouldDisposeTexture(): Boolean = true
}

class PackedSheetLoader(resolver: FileHandleResolver)
    : AsynchronousAssetLoader<PackedSheet, PackedSheetLoader.PackedSheetLoaderParam>(resolver) {

    class PackedSheetLoaderParam(
            val packables: List<Packable> = emptyList(),
            val config: PackedSheet.Config = PackedSheet.Config()
    ) : AssetLoaderParameters<PackedSheet>()

    override fun getDependencies(fileName: String?, file: FileHandle?, parameter: PackedSheetLoaderParam?): Array<AssetDescriptor<Any>>? {
        return null
    }

    override fun loadAsync(manager: AssetManager, fileName: String, file: FileHandle, parameter: PackedSheetLoaderParam?) {
        // Nothing to load async.
    }

    override fun loadSync(manager: AssetManager, fileName: String, file: FileHandle, parameter: PackedSheetLoaderParam?): PackedSheet {
        val param = parameter ?: PackedSheetLoaderParam()
        val packedSheet = PackedSheet(param.config, param.packables)
        packedSheet.pack()
        return packedSheet
    }
}

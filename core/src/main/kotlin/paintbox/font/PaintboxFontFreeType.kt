package paintbox.font

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import paintbox.binding.LongVar
import paintbox.binding.ReadOnlyLongVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.util.WindowSize
import paintbox.util.gdxutils.copy
import kotlin.math.max
import kotlin.math.min

/**
 * A wrapper around a [FreeTypeFontGenerator].
 * 
 * The [params] passed in will be copied with the [ftfParameter]'s font and border size.
 */
open class PaintboxFontFreeType(params: PaintboxFontParams,
                           val ftfParameter: FreeTypeFontGenerator.FreeTypeFontParameter)
    : PaintboxFont(params.copy(fontSize = ftfParameter.size, borderSize = ftfParameter.borderWidth)) {

    companion object {
        init {
            FreeTypeFontGenerator.setMaxTextureSize(2048)
        }
    }

    private var isInBegin: Boolean = false
    private var isLoaded: Boolean = false
    
    private var currentFontNum: Long = Long.MIN_VALUE + 1
    override val currentFontNumber: Long get() = currentFontNum
    override val currentFontNumberVar: ReadOnlyLongVar = LongVar(currentFontNum)
    private var lastWindowSize: WindowSize = WindowSize(1280, 720)
    private var upscaledFactor: Float = -1f // 
    private var generator: FreeTypeFontGenerator? = null
        set(value) {
            field?.dispose()
            field = value
        }
    private var currentFont: BitmapFont? = null
        set(value) {
            if (value !== field) {
                currentFontNum++
                (currentFontNumberVar as LongVar).set(currentFontNum)
                
//                val current = field
//                if (current != null) {
//                    current.dispose()
//                    (current.data as? Disposable)?.dispose()
//                }
            }
            field = value
        }
    
    private var afterLoad: PaintboxFontFreeType.(BitmapFont) -> Unit = {}
    
    override fun begin(areaWidth: Float, areaHeight: Float): BitmapFont {
        if (isInBegin) {
            if (PaintboxFont.LENIENT_BEGIN_END) {
                end()
            } else {
                error("Cannot call begin before end")
            }
        }
        isInBegin = true

        if (!isLoaded || currentFont == null) {
            // If the loadPriority is ALWAYS, this will already have been loaded in resize()
            load()
        }
        
        if (this.params.scaleToReferenceSize) {
            val font = currentFont!!
            val referenceSize = this.params.referenceSize
            val scaleX = (referenceSize.width / areaWidth)
            val scaleY = (referenceSize.height / areaHeight)
            val scale = max(scaleX, scaleY)
            if (scaleX >= 0f && scaleY >= 0f && scaleX.isFinite() && scaleY.isFinite()) {
                font.data.setScale(scale, scale)
            }
        }
        
        return currentFont!!
    }

    override fun end() {
        if (!isInBegin && !PaintboxFont.LENIENT_BEGIN_END) error("Cannot call end before begin")
        isInBegin = false
        
        // Sets font back to scaleXY = 1.0
        val currentFont = this.currentFont
        if (currentFont != null) {
            this.fontDataInfo.applyToFont(currentFont)
        }
    }
    
    override fun resize(width: Int, height: Int) {
        this.dispose()
        lastWindowSize = WindowSize(width, height)
        if (params.loadPriority == PaintboxFontParams.LoadPriority.ALWAYS/* || params.scaleToReferenceSize*/) {
            load()
        }
    }
    
    private fun load() {
        val windowSize = this.lastWindowSize
        val referenceSize = params.referenceSize
        val scale: Float = if (!params.scaleToReferenceSize) 1f else min(windowSize.width.toFloat() / referenceSize.width, windowSize.height.toFloat() / referenceSize.height)
        
        if (this.upscaledFactor != scale) {
            this.dispose()
//            println("New upscaled factor: ${this.upscaledFactor} to $scale    $windowSize, ref $referenceSize")
            this.upscaledFactor = scale
            

            val newParam = ftfParameter.copy()
            val params = this.params
            newParam.size = (params.fontSize * scale).toInt()
            newParam.borderWidth = params.borderSize * scale

            val generator = FreeTypeFontGenerator(params.file)
            val generatedFont = generator.generateFont(newParam)
            this.generator = generator
            this.currentFont = generatedFont
            
            this.afterLoad(generatedFont)
            this.fontDataInfo.copyFromFont(generatedFont)
        }
        
        this.isLoaded = true
    }

    fun setAfterLoad(func: PaintboxFontFreeType.(BitmapFont) -> Unit): PaintboxFontFreeType {
        afterLoad = func
        return this
    }

    @Synchronized
    override fun dispose() {
        this.isLoaded = false
    }
}

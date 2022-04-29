package paintbox.font

import com.badlogic.gdx.graphics.g2d.BitmapFont
import paintbox.binding.LongVar
import paintbox.binding.ReadOnlyLongVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var

/**
 * A wrapper around a plain [BitmapFont].
 * 
 * @param ownsFont If true, this [PaintboxFontBitmap] will dispose the provided [font] when [dispose] is called
 */
class PaintboxFontBitmap(
    params: PaintboxFontParams,
    private val font: BitmapFont, val ownsFont: Boolean
) : PaintboxFont(params) {
    
    private var isInBegin: Boolean = false
    override val currentFontNumber: Long = 0L // Backing font never changes
    override val currentFontNumberVar: ReadOnlyLongVar = LongVar(currentFontNumber)

    init {
        this.font.data.setScale(1f)
        this.fontDataInfo.copyFromFont(font)
    }
    
    override fun resize(width: Int, height: Int) {
        // Nothing needs to happen with the font for resize. Referential resizing is done with begin()
    }

    override fun begin(areaWidth: Float, areaHeight: Float): BitmapFont {
        if (isInBegin) {
            if (PaintboxFont.LENIENT_BEGIN_END) {
                end()
            } else {
                error("Cannot call begin before end")
            }
        }
        isInBegin = true
        
        val params = this.params.getOrCompute()
        if (params.scaleToReferenceSize) {
            val scaleX = areaWidth / params.referenceSize.width
            val scaleY = areaHeight / params.referenceSize.height
            if (scaleX >= 0f && scaleY >= 0f && scaleX.isFinite() && scaleY.isFinite()) {
                font.data.setScale(scaleX, scaleY)
            }
        }
        
        return font
    }

    override fun end() {
        if (!isInBegin && !PaintboxFont.LENIENT_BEGIN_END) error("Cannot call end before begin")
        isInBegin = false
        
        // Sets font back to scaleXY = 1.0
        this.fontDataInfo.applyToFont(this.font)
    }

    override fun dispose() {
        if (ownsFont) {
            font.dispose()
        }
    }

    override fun onParamsChanged() {
    }
}

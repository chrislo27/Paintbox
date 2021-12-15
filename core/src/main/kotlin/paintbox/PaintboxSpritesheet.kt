package paintbox

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlin.math.absoluteValue


class PaintboxSpritesheet(val texture: Texture) {
    val roundedCorner: TextureRegion = TextureRegion(texture, 0, 0, 32, 32)
    val roundedCorner7: TextureRegion = TextureRegion(texture, 34, 7, 7, 7)
    val roundedCorner6: TextureRegion = TextureRegion(texture, 34, 16, 6, 6)
    val roundedCorner5: TextureRegion = TextureRegion(texture, 34, 24, 5, 5)
    val roundedCorner4: TextureRegion = TextureRegion(texture, 43, 7, 4, 4)
    val roundedCorner3: TextureRegion = TextureRegion(texture, 42, 16, 3, 3)
    val roundedCorner2: TextureRegion = TextureRegion(texture, 42, 16, 3, 3)
    val fill: TextureRegion = TextureRegion(texture, 36, 1, 2, 2)
    val logo128: TextureRegion = TextureRegion(texture, 0, 384, 128, 128)
    
    val checkboxEmpty: TextureRegion = TextureRegion(texture, 1, 35, 64, 64)
    val checkboxCheck: TextureRegion = TextureRegion(texture, 68, 35, 64, 64)
    val checkboxX: TextureRegion = TextureRegion(texture, 135, 35, 64, 64)
    val checkboxLine: TextureRegion = TextureRegion(texture, 403, 35, 64, 64)
    val radioButtonEmpty: TextureRegion = TextureRegion(texture, 202, 35, 64, 64)
    val radioButtonFilled: TextureRegion = TextureRegion(texture, 269, 35, 64, 64)
    val circleFilled: TextureRegion = TextureRegion(texture, 336, 35, 64, 64)
    val upArrow: TextureRegion = TextureRegion(texture, 51, 2, 30, 30)
    val upChevronArrow: TextureRegion = TextureRegion(texture, 83, 2, 30, 30)
    val downChevronArrow: TextureRegion = TextureRegion(upChevronArrow).apply { 
        flip(false, true)
    }
    
    fun getRoundedCornerForRadius(rad: Int): TextureRegion {
        val radius = rad.absoluteValue
        return when {
            radius <= 2 -> roundedCorner2
            radius <= 3 -> roundedCorner3
            radius <= 4 -> roundedCorner4
            radius <= 5 -> roundedCorner5
            radius <= 6 -> roundedCorner6
            radius <= 7 -> roundedCorner7
            else -> roundedCorner
        }
    }
}
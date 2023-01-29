package paintbox.util.gdxutils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Call this with the function to draw primitives, then draw sprites with [useStencilMask].
 * 
 * This requires that there is a **depth** buffer available in the current frame buffer.
 */
@OptIn(ExperimentalContracts::class)
inline fun ShapeRenderer.prepareStencilMask(
    batch: SpriteBatch, clearDepthBuffer: Boolean = true,
    inverted: Boolean = false,
    drawing: ShapeRenderer.() -> Unit,
): SpriteBatch {
    contract {
        callsInPlace(drawing, InvocationKind.AT_MOST_ONCE)
    }

    if (batch.isDrawing)
        batch.end()

    if (clearDepthBuffer) {
        Gdx.gl.glClearDepthf(1f)
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)
    }
    Gdx.gl.glDepthFunc(GL20.GL_LESS)
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    Gdx.gl.glDepthMask(true)
    Gdx.gl.glColorMask(false, false, false, false)

    this.drawing()

    batch.begin()
    Gdx.gl.glDepthMask(false)
    Gdx.gl.glColorMask(true, true, true, true)
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
    Gdx.gl.glDepthFunc(if (!inverted) GL20.GL_EQUAL else GL20.GL_NOTEQUAL)

    return batch
}

@OptIn(ExperimentalContracts::class)
inline fun SpriteBatch.useStencilMask(drawing: () -> Unit) {
    contract {
        callsInPlace(drawing, InvocationKind.EXACTLY_ONCE)
    }

    drawing()
    this.flush()
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
}

package paintbox.transition

import com.badlogic.gdx.utils.Disposable


abstract class Transition(val duration: Float) : Disposable {

    companion object {
        val EMPTY: Transition = object : Transition(0f) {
            override fun dispose() {
                // NO-OP
            }

            override fun render(transitionScreen: TransitionScreen, screenRender: () -> Unit) {
                // NO-OP
            }
        }
    }

    var overrideDone: Boolean = false
        private set

    abstract fun render(transitionScreen: TransitionScreen, screenRender: () -> Unit)

}
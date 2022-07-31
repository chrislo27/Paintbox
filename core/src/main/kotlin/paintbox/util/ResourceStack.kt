package paintbox.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


abstract class ResourceStack<T> {

    private val pool: InternalPool = InternalPool()
    private val stack: ArrayDeque<T> = ArrayDeque()
    val numInStack: Int
        get() = stack.size

    /**
     * Gets a new object from the pool and returns it. This puts it on the resource stack.
     */
    fun getAndPush(): T {
        val obtained = pool.obtain()
        resetBeforePushed(obtained)
        stack.add(obtained)
        return obtained
    }

    /**
     * Pops the last resource off the stack. If there is nothing on the stack, null is returned.
     * The resource that is returned will not be reset but may mutate or be reused later from
     * another invocation of [getAndPush].
     */
    fun pop(): T? {
        if (stack.isEmpty()) return null
        val last = stack.removeLast()
        pool.free(last)
        return last
    }

    protected abstract fun newObject(): T
    protected abstract fun resetBeforePushed(obj: T)
    protected abstract fun resetWhenFreed(obj: T?)

    /**
     * Uses a pooled resource inside the [action] block. This automatically frees the obtained resource.
     */
    @OptIn(ExperimentalContracts::class)
    inline fun use(action: (obj: T) -> Unit) {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        val obj = getAndPush()
        action(obj)
        pop()
    }

    private inner class InternalPool : Pool<T>(64) {
        override fun newObject(): T {
            return this@ResourceStack.newObject()
        }

        override fun reset(`object`: T?) {
            this@ResourceStack.resetWhenFreed(`object`)
        }
    }
}

/**
 * A convenience singleton for implementing temporary, pooled [com.badlogic.gdx.graphics.Color]s in a stack method.
 */
object ColorStack : ResourceStack<Color>() {
    override fun newObject(): Color {
        return Color(1f, 1f, 1f, 1f)
    }

    override fun resetWhenFreed(obj: Color?) {
        // Intentional: don't do a reset. The popped colour is returned so the information may be required temporarily
    }

    override fun resetBeforePushed(obj: Color) {
        obj.set(1f, 1f, 1f, 1f)
    }
}

object RectangleStack : ResourceStack<Rectangle>() {
    override fun newObject(): Rectangle {
        return Rectangle()
    }

    override fun resetBeforePushed(obj: Rectangle) {
        obj.set(0f, 0f, 0f, 0f)
    }

    override fun resetWhenFreed(obj: Rectangle?) {
        // Intentional: don't do a reset. The popped rectangle is returned so the information may be required temporarily
    }
}

object Vector2Stack : ResourceStack<Vector2>() {
    override fun newObject(): Vector2 {
        return Vector2()
    }

    override fun resetBeforePushed(obj: Vector2) {
        obj.set(0f, 0f)
    }

    override fun resetWhenFreed(obj: Vector2?) {
        // Intentional: don't do a reset. The popped vec2 is returned so the information may be required temporarily
    }
}

object Vector3Stack : ResourceStack<Vector3>() {
    override fun newObject(): Vector3 {
        return Vector3()
    }

    override fun resetBeforePushed(obj: Vector3) {
        obj.set(0f, 0f, 0f)
    }

    override fun resetWhenFreed(obj: Vector3?) {
        // Intentional: don't do a reset. The popped vec3 is returned so the information may be required temporarily
    }
}

object Matrix4Stack : ResourceStack<Matrix4>() {
    override fun newObject(): Matrix4 {
        return Matrix4()
    }

    override fun resetBeforePushed(obj: Matrix4) {
    }

    override fun resetWhenFreed(obj: Matrix4?) {
        // Intentional: don't do a reset. The popped mat4 is returned so the information may be required temporarily
    }
}

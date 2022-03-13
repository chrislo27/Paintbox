package paintbox.binding


/**
 * A listener that is fired when a [ReadOnlyVar] is changed.
 */
fun interface VarChangedListener<in T> {
    fun onChange(v: ReadOnlyVar<T>)
}

/**
 * A special kind of [VarChangedListener] that can be flagged as needing to be removed.
 */
interface DisposableVarChangedListener<in T> : VarChangedListener<T> {
    val shouldBeDisposed: Boolean
}

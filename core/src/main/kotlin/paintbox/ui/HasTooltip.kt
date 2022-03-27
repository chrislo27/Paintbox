package paintbox.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import paintbox.PaintboxGame
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.font.PaintboxFont
import paintbox.ui.area.Insets
import paintbox.ui.control.TextLabel


interface HasTooltip {

    val tooltipElement: Var<UIElement?>

    /**
     * Called when the [tooltip] is added to the scene.
     *
     * The [tooltip] should recompute its bounds if it is dynamically sized.
     * Behaviour for [Tooltip] is already implemented by default.
     */
    fun onTooltipStarted(tooltip: UIElement) {
        if (tooltip is Tooltip)
            tooltip.resizeBoundsToContent()
    }

    /**
     * Called when the [tooltip] is removed.
     */
    fun onTooltipEnded(tooltip: UIElement) {
    }

    /**
     * A default implementation of [HasTooltip] that can be delegated to. The default element is null.
     */
    open class DefaultImpl : HasTooltip {
        override val tooltipElement: Var<UIElement?> = Var(null)
    }
}


open class Tooltip
    : TextLabel {

    init {
        this.backgroundColor.set(Color(0f, 0f, 0f, 0.85f))
        this.textColor.set(Color.WHITE)
        this.bgPadding.set(Insets(8f))
        this.renderBackground.set(true)
        this.doXCompression.set(true)
        this.renderAlign.set(Align.topLeft)
        this.autosizeBehavior.set(AutosizeBehavior.Active(AutosizeBehavior.Dimensions.WIDTH_AND_HEIGHT, true))
    }

    constructor(text: String, font: PaintboxFont = UIElement.defaultFont)
            : super(text, font)

    constructor(binding: Var.Context.() -> String, font: PaintboxFont = UIElement.defaultFont)
            : super(binding, font)
    
    constructor(bindable: ReadOnlyVar<String>, font: PaintboxFont = UIElement.defaultFont)
            : super(bindable, font)

}
package io.github.frostzie.skyfall.hud

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter

/**
 * A generic, configurable HUD element that acts as an adapter for any feature.
 * This prevents the need to create a new class file for every simple HUD element.
 *
 * @param id The unique identifier for this element.
 * @param name The display name for the HUD editor.
 * @param defaultConfig The default position, size, and state.
 * @param advancedSizingOverride Overrides the default 'advancedSizing' behavior.
 * @param minWidthOverride Overrides the default minimum width.
 * @param minHeightOverride Overrides the default minimum height.
 * @param renderAction The lambda function that contains the actual rendering logic for the feature.
 *                     It receives the DrawContext and the element's current state to draw correctly.
 */
class FeatureHudElement(
    id: String,
    name: String,
    defaultConfig: HudElementConfig,
    private val advancedSizingOverride: Boolean = false,
    private val minWidthOverride: Int? = null,
    private val minHeightOverride: Int? = null,
    private val renderAction: (drawContext: DrawContext, element: HudElement) -> Unit
) : HudElement(id, name, defaultConfig) {

    /**
     * The advanced sizing behavior is now determined by the value passed during construction.
     */
    override val advancedSizing: Boolean
        get() = advancedSizingOverride

    /**
     * The render function here simply invokes the lambda function provided
     * when this element was created, passing itself as a parameter so the
     * renderer knows the current position and size.
     */
    override fun render(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        renderAction(drawContext, this)
    }

    /**
     * The minimum width is determined by the value passed during construction,
     * falling back to the base class's default if not provided.
     */
    override fun getMinWidth(): Int {
        return minWidthOverride ?: super.getMinWidth()
    }

    /**
     * The minimum height is determined by the value passed during construction,
     * falling back to the base class's default if not provided.
     */
    override fun getMinHeight(): Int {
        return minHeightOverride ?: super.getMinHeight()
    }
}
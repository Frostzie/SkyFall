package io.github.frostzie.skyfall.hud

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier

data class HudElementConfig(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val enabled: Boolean = true
)

abstract class HudElement(
    val id: String,
    val name: String,
    val defaultConfig: HudElementConfig
) {
    var config: HudElementConfig = defaultConfig
        private set

    /**
     * Determines if this element supports advanced, free-form resizing (using Ctrl).
     * This is the toggle you wanted. Subclasses override this.
     */
    open val advancedSizing: Boolean = false

    /**
     * The only correct way to update an element's configuration.
     * This replaces the old config with a new, updated instance.
     */
    fun updateConfig(newConfig: HudElementConfig) {
        this.config = newConfig
    }

    fun resetToDefault() {
        updateConfig(defaultConfig)
    }

    abstract fun render(drawContext: DrawContext, tickCounter: RenderTickCounter)

    open fun getMinWidth(): Int = 20

    open fun getMinHeight(): Int = 20

    open fun getTextScale(): Float {
        val baseWidth = defaultConfig.width
        val baseHeight = defaultConfig.height
        val currentWidth = config.width
        val currentHeight = config.height
        val widthScale = currentWidth.toFloat() / baseWidth
        val heightScale = currentHeight.toFloat() / baseHeight

        return kotlin.math.min(widthScale, heightScale).coerceIn(0.5f, 2.0f)
    }

    fun isPointInside(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= config.x && mouseX <= config.x + config.width &&
                mouseY >= config.y && mouseY <= config.y + config.height
    }

    fun getResizeHandle(mouseX: Int, mouseY: Int): ResizeHandle? {
        val handleSize = 8
        val x = config.x
        val y = config.y
        val width = config.width
        val height = config.height

        return when {
            mouseX >= x && mouseX <= x + handleSize &&
                    mouseY >= y && mouseY <= y + handleSize -> ResizeHandle.TOP_LEFT

            mouseX >= x + width - handleSize && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + handleSize -> ResizeHandle.TOP_RIGHT

            mouseX >= x && mouseX <= x + handleSize &&
                    mouseY >= y + height - handleSize && mouseY <= y + height -> ResizeHandle.BOTTOM_LEFT

            mouseX >= x + width - handleSize && mouseX <= x + width &&
                    mouseY >= y + height - handleSize && mouseY <= y + height -> ResizeHandle.BOTTOM_RIGHT

            else -> null
        }
    }

    enum class ResizeHandle {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}
package io.github.frostzie.skyfall.hud

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier

data class HudElementConfig(
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 50,
    val height: Int = 20,
    val enabled: Boolean = true
)

abstract class HudElement(
    val id: Identifier,
    val name: String,
    val defaultConfig: HudElementConfig
) {
    var config: HudElementConfig = defaultConfig.copy()
        private set

    fun updateConfig(newConfig: HudElementConfig) {
        config = newConfig
    }

    fun resetToDefault() {
        config = defaultConfig.copy()
    }

    abstract fun render(drawContext: DrawContext, tickCounter: RenderTickCounter)
    open fun getMinWidth(): Int = 20
    open fun getMinHeight(): Int = 15

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
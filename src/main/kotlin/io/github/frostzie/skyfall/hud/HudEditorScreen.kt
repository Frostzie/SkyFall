package io.github.frostzie.skyfall.hud

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.LoggerProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.io.File
import kotlin.math.max
import kotlin.math.min

class HudEditorScreen : Screen(Text.literal("HUD Editor")) {

    private var selectedElement: HudElement? = null
    private var isDragging = false
    private var isResizing = false
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private var resizeHandle: HudElement.ResizeHandle? = null
    private var originalConfig: HudElementConfig? = null
    private val fullHudEditor = SkyFall.feature.gui.fullHudEditor

    private var showCenterTooltip = true
    private var showInfoBoxTooltip = false
    private val infoBoxWidth = 20
    private val infoBoxHeight = 20

    companion object {
        private val logger = LoggerProvider.getLogger("hudEditor")
        private val tooltipShownFile = File(MinecraftClient.getInstance().runDirectory, "config/skyfall/tooltip_shown.flag")

        private fun hasTooltipBeenShown(): Boolean {
            return tooltipShownFile.exists()
        }

        private fun markTooltipAsShown() {
            try {
                tooltipShownFile.parentFile.mkdirs()
                tooltipShownFile.createNewFile()
            } catch (e: Exception) {
                logger.error("Failed to mark tooltip as shown: ${e.message}", e)
            }
        }
    }

    override fun init() {
        super.init()
        HudManager.saveConfig()
        showCenterTooltip = !hasTooltipBeenShown()
    }

    override fun render(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        drawContext.fill(0, 0, width, height, 0x90000000.toInt())

        super.render(drawContext, mouseX, mouseY, delta)

        HudManager.getElements().forEach { element ->
            if (element.config.enabled) {
                val dummyTickCounter = client!!.renderTickCounter
                element.render(drawContext, dummyTickCounter)

                if (element == selectedElement) {
                    drawSelectionOutline(drawContext, element)
                    if (fullHudEditor) {
                        drawResizeHandles(drawContext, element)
                    }
                }
            }
        }

        drawInfoBox(drawContext, mouseX, mouseY)

        if (showCenterTooltip && !isDragging && !isResizing) {
            drawCenterTooltip(drawContext, mouseX, mouseY)
        }

        if (showInfoBoxTooltip && !isDragging && !isResizing) {
            drawInfoBoxTooltip(drawContext, mouseX, mouseY)
        }

        selectedElement?.let { element ->
            drawContext.drawTextWithShadow(
                textRenderer,
                "Selected: ${element.name} (${element.config.x}, ${element.config.y}) ${element.config.width}x${element.config.height}",
                10, height - 20, 0x00FF00
            )
        }
    }

    private fun drawInfoBox(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
        val infoBoxX = width - infoBoxWidth - 10
        val infoBoxY = 10

        val isHovering = mouseX >= infoBoxX && mouseX <= infoBoxX + infoBoxWidth &&
                mouseY >= infoBoxY && mouseY <= infoBoxY + infoBoxHeight

        showInfoBoxTooltip = isHovering

        val backgroundColor = if (isHovering) 0xCC444444.toInt() else 0xCC333333.toInt()
        drawContext.fill(infoBoxX, infoBoxY, infoBoxX + infoBoxWidth, infoBoxY + infoBoxHeight, backgroundColor)

        val borderColor = if (isHovering) 0xFF888888.toInt() else 0xFF666666.toInt()
        drawContext.drawBorder(infoBoxX, infoBoxY, infoBoxWidth, infoBoxHeight, borderColor)

        val infoText = "?"
        val textWidth = textRenderer.getWidth(infoText)
        val textX = infoBoxX + (infoBoxWidth - textWidth) / 2
        val textY = infoBoxY + (infoBoxHeight - textRenderer.fontHeight) / 2

        drawContext.drawTextWithShadow(textRenderer, infoText, textX, textY, 0xFFFFFF)
    }

    private fun drawInfoBoxTooltip(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
        val tooltipText = if (fullHudEditor) {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over element to resize",
                "Drag corners to resize (hold Ctrl for free resize)",
                "Press R to reset selected element"
            )
        } else {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over element to resize",
                "Press R to reset selected element"
            )
        }

        val padding = 6
        val lineHeight = textRenderer.fontHeight + 2
        val maxWidth = tooltipText.maxOfOrNull { textRenderer.getWidth(it) } ?: 0
        val tooltipWidth = maxWidth + padding * 2
        val tooltipHeight = tooltipText.size * lineHeight + padding * 2

        val infoBoxX = width - infoBoxWidth - 10
        val tooltipX = infoBoxX - tooltipWidth - 10
        val tooltipY = 10

        drawContext.fill(
            tooltipX, tooltipY,
            tooltipX + tooltipWidth, tooltipY + tooltipHeight,
            0xCC000000.toInt()
        )

        drawContext.drawBorder(
            tooltipX, tooltipY,
            tooltipWidth, tooltipHeight,
            0xFF666666.toInt()
        )

        tooltipText.forEachIndexed { index, line ->
            drawContext.drawTextWithShadow(
                textRenderer,
                line,
                tooltipX + padding,
                tooltipY + padding + index * lineHeight,
                0xFFFFFF
            )
        }
    }

    private fun drawCenterTooltip(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
        val tooltipText = if (fullHudEditor) {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over element to resize",
                "Drag corners to resize (hold Ctrl for free resize)",
                "Press R to reset selected element"
            )
        } else {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over element to resize",
                "Press R to reset selected element"
            )
        }

        val padding = 8
        val lineHeight = textRenderer.fontHeight + 3
        val maxWidth = tooltipText.maxOfOrNull { textRenderer.getWidth(it) } ?: 0
        val buttonText = "Understood"
        val buttonWidth = textRenderer.getWidth(buttonText) + 12
        val buttonHeight = textRenderer.fontHeight + 6

        val tooltipWidth = max(maxWidth + padding * 2, buttonWidth + padding * 2)
        val tooltipHeight = tooltipText.size * lineHeight + padding * 2 + buttonHeight + 8

        val tooltipX = (width - tooltipWidth) / 2
        val tooltipY = height / 3

        drawContext.fill(
            tooltipX, tooltipY,
            tooltipX + tooltipWidth, tooltipY + tooltipHeight,
            0xDD000000.toInt()
        )

        drawContext.drawBorder(
            tooltipX, tooltipY,
            tooltipWidth, tooltipHeight,
            0xFF888888.toInt()
        )

        tooltipText.forEachIndexed { index, line ->
            val textX = tooltipX + (tooltipWidth - textRenderer.getWidth(line)) / 2
            drawContext.drawTextWithShadow(
                textRenderer,
                line,
                textX,
                tooltipY + padding + index * lineHeight,
                0xFFFFFF
            )
        }

        val buttonX = tooltipX + (tooltipWidth - buttonWidth) / 2
        val buttonY = tooltipY + tooltipHeight - buttonHeight - padding

        val isButtonHovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                mouseY >= buttonY && mouseY <= buttonY + buttonHeight

        val buttonColor = if (isButtonHovered) 0xFF444444.toInt() else 0xFF333333.toInt()
        val buttonBorderColor = if (isButtonHovered) 0xFFAAAAA.toInt() else 0xFF666666.toInt()

        drawContext.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, buttonColor)
        drawContext.drawBorder(buttonX, buttonY, buttonWidth, buttonHeight, buttonBorderColor)

        val buttonTextX = buttonX + (buttonWidth - textRenderer.getWidth(buttonText)) / 2
        val buttonTextY = buttonY + (buttonHeight - textRenderer.fontHeight) / 2

        drawContext.drawTextWithShadow(textRenderer, buttonText, buttonTextX, buttonTextY, 0xFFFFFF)
    }

    private fun drawSelectionOutline(drawContext: DrawContext, element: HudElement) {
        val config = element.config
        val color = 0xFF00FF00.toInt()

        drawContext.drawBorder(config.x - 1, config.y - 1, config.width + 2, config.height + 2, color)
    }

    private fun drawResizeHandles(drawContext: DrawContext, element: HudElement) {
        val config = element.config
        val handleSize = 6
        val handleColor = 0xFFFFFFFF.toInt()

        val x = config.x
        val y = config.y
        val width = config.width
        val height = config.height

        drawContext.fill(x - handleSize/2, y - handleSize/2, x + handleSize/2, y + handleSize/2, handleColor)
        drawContext.fill(x + width - handleSize/2, y - handleSize/2, x + width + handleSize/2, y + handleSize/2, handleColor)
        drawContext.fill(x - handleSize/2, y + height - handleSize/2, x + handleSize/2, y + height + handleSize/2, handleColor)
        drawContext.fill(x + width - handleSize/2, y + height - handleSize/2, x + width + handleSize/2, y + height + handleSize/2, handleColor)
    }

    private fun isClickOnUnderstoodButton(mouseX: Int, mouseY: Int): Boolean {
        if (!showCenterTooltip) return false

        val tooltipText = if (fullHudEditor) {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over element to resize",
                "Drag corners to resize (hold Ctrl for free resize)",
                "Press R to reset selected element"
            )
        } else {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over element to resize",
                "Press R to reset selected element"
            )
        }

        val padding = 8
        val lineHeight = textRenderer.fontHeight + 3
        val maxWidth = tooltipText.maxOfOrNull { textRenderer.getWidth(it) } ?: 0
        val buttonText = "Understood"
        val buttonWidth = textRenderer.getWidth(buttonText) + 12
        val buttonHeight = textRenderer.fontHeight + 6

        val tooltipWidth = max(maxWidth + padding * 2, buttonWidth + padding * 2)
        val tooltipHeight = tooltipText.size * lineHeight + padding * 2 + buttonHeight + 8

        val tooltipX = (width - tooltipWidth) / 2
        val tooltipY = height / 3

        val buttonX = tooltipX + (tooltipWidth - buttonWidth) / 2
        val buttonY = tooltipY + tooltipHeight - buttonHeight - padding

        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                mouseY >= buttonY && mouseY <= buttonY + buttonHeight
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            if (isClickOnUnderstoodButton(mouseX.toInt(), mouseY.toInt())) {
                showCenterTooltip = false
                markTooltipAsShown()
                return true
            }

            val clickedElement = findElementAt(mouseX.toInt(), mouseY.toInt())

            if (clickedElement != null) {
                selectedElement = clickedElement
                originalConfig = clickedElement.config.copy()

                if (fullHudEditor) {
                    val handle = clickedElement.getResizeHandle(mouseX.toInt(), mouseY.toInt())
                    if (handle != null) {
                        isResizing = true
                        resizeHandle = handle
                        return true
                    }
                }

                isDragging = true
                dragOffsetX = mouseX.toInt() - clickedElement.config.x
                dragOffsetY = mouseY.toInt() - clickedElement.config.y
                return true
            } else {
                selectedElement = null
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            isDragging = false
            isResizing = false
            resizeHandle = null
            originalConfig = null
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val hoveredElement = findElementAt(mouseX.toInt(), mouseY.toInt())

        if (hoveredElement != null) {
            val scrollDirection = if (verticalAmount > 0) 1 else -1
            val scaleFactor = 1.1f

            val currentConfig = hoveredElement.config
            val newWidth: Int
            val newHeight: Int

            if (scrollDirection > 0) {
                newWidth = (currentConfig.width * scaleFactor).toInt()
                newHeight = (currentConfig.height * scaleFactor).toInt()
            } else {
                val newWidthFloat = currentConfig.width / scaleFactor
                val newHeightFloat = currentConfig.height / scaleFactor

                val minWidth = hoveredElement.getMinWidth()
                val minHeight = hoveredElement.getMinHeight()

                if (newWidthFloat <= minWidth * 1.2f || newHeightFloat <= minHeight * 1.2f) {
                    val defaultConfig = hoveredElement.defaultConfig
                    hoveredElement.updateConfig(
                        currentConfig.copy(
                            width = defaultConfig.width,
                            height = defaultConfig.height
                        )
                    )
                    return true
                }

                newWidth = newWidthFloat.toInt()
                newHeight = newHeightFloat.toInt()
            }

            hoveredElement.updateConfig(
                currentConfig.copy(
                    width = max(hoveredElement.getMinWidth(), newWidth),
                    height = max(hoveredElement.getMinHeight(), newHeight)
                )
            )
            selectedElement = hoveredElement

            return true
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (button == 0 && selectedElement != null && fullHudEditor) {
            val element = selectedElement!!
            val original = originalConfig ?: element.config

            if (isDragging) {
                val newX = max(0, min(width - element.config.width, mouseX.toInt() - dragOffsetX))
                val newY = max(0, min(height - element.config.height, mouseY.toInt() - dragOffsetY))

                element.updateConfig(element.config.copy(x = newX, y = newY))
                return true
            } else if (isResizing && resizeHandle != null) {
                val isCtrlHeld = GLFW.glfwGetKey(client!!.window.handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                        GLFW.glfwGetKey(client!!.window.handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS

                val mouseXInt = mouseX.toInt()
                val mouseYInt = mouseY.toInt()

                if (isCtrlHeld) {
                    when (resizeHandle) {
                        HudElement.ResizeHandle.BOTTOM_RIGHT -> {
                            val newWidth = max(10, min(width - original.x, mouseXInt - original.x))
                            val newHeight = max(10, min(height - original.y, mouseYInt - original.y))
                            element.updateConfig(original.copy(width = newWidth, height = newHeight))
                        }
                        HudElement.ResizeHandle.BOTTOM_LEFT -> {
                            val newX = max(0, min(mouseXInt, original.x + original.width - 10))
                            val newWidth = max(10, original.x + original.width - newX)
                            val newHeight = max(10, min(height - original.y, mouseYInt - original.y))
                            element.updateConfig(original.copy(x = newX, width = newWidth, height = newHeight))
                        }
                        HudElement.ResizeHandle.TOP_RIGHT -> {
                            val newY = max(0, min(mouseYInt, original.y + original.height - 10))
                            val newWidth = max(10, min(width - original.x, mouseXInt - original.x))
                            val newHeight = max(10, original.y + original.height - newY)
                            element.updateConfig(original.copy(y = newY, width = newWidth, height = newHeight))
                        }
                        HudElement.ResizeHandle.TOP_LEFT -> {
                            val newX = max(0, min(mouseXInt, original.x + original.width - 10))
                            val newY = max(0, min(mouseYInt, original.y + original.height - 10))
                            val newWidth = max(10, original.x + original.width - newX)
                            val newHeight = max(10, original.y + original.height - newY)
                            element.updateConfig(original.copy(x = newX, y = newY, width = newWidth, height = newHeight))
                        }
                        else -> {}
                    }
                } else {
                    val centerX = original.x + original.width / 2
                    val centerY = original.y + original.height / 2

                    val originalDistanceX = kotlin.math.abs(original.width / 2)
                    val originalDistanceY = kotlin.math.abs(original.height / 2)

                    val newDistanceX = kotlin.math.abs(mouseXInt - centerX)
                    val newDistanceY = kotlin.math.abs(mouseYInt - centerY)

                    val scaleX = if (originalDistanceX > 0) newDistanceX.toFloat() / originalDistanceX else 1f
                    val scaleY = if (originalDistanceY > 0) newDistanceY.toFloat() / originalDistanceY else 1f
                    val scale = max(scaleX, scaleY)

                    val newWidth = max(10, (original.width * scale).toInt())
                    val newHeight = max(10, (original.height * scale).toInt())

                    val newX = max(0, min(width - newWidth, centerX - newWidth / 2))
                    val newY = max(0, min(height - newHeight, centerY - newHeight / 2))

                    val finalWidth = min(newWidth, width - newX)
                    val finalHeight = min(newHeight, height - newY)

                    element.updateConfig(original.copy(x = newX, y = newY, width = finalWidth, height = finalHeight))
                }
                return true
            }
        } else if (button == 0 && selectedElement != null && !fullHudEditor) {
            if (isDragging) {
                val element = selectedElement!!
                val newX = max(0, min(width - element.config.width, mouseX.toInt() - dragOffsetX))
                val newY = max(0, min(height - element.config.height, mouseY.toInt() - dragOffsetY))

                element.updateConfig(element.config.copy(x = newX, y = newY))
                return true
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_R && selectedElement != null) {
            selectedElement!!.resetToDefault()
            return true
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            HudManager.saveConfig()
            client?.setScreen(null)
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun findElementAt(mouseX: Int, mouseY: Int): HudElement? {
        return HudManager.getElements().lastOrNull { it.config.enabled && it.isPointInside(mouseX, mouseY) }
    }

    override fun close() {
        HudManager.saveConfig()
        super.close()
    }
}
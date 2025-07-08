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
    private var originalConfig: HudElementConfig? = null
    private var isDragging = false
    private var isResizing = false
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private var resizeHandle: HudElement.ResizeHandle? = null
    private var showCenterTooltip = false
    private var showInfoBoxTooltip = false
    private val fullHudEditor = SkyFall.feature.gui.fullHudEditor

    private companion object {
        private val logger = LoggerProvider.getLogger("HudEditor")
        private val tooltipShownFile = File(MinecraftClient.getInstance().runDirectory, "config/skyfall/tooltip_shown.flag")

        private const val INFO_BOX_SIZE = 20
        private const val INFO_BOX_MARGIN = 10

        private const val COLOR_SELECTION_BORDER = 0xFF00FF00.toInt()
        private const val COLOR_HANDLE = 0xFFFFFFFF.toInt()
        private const val HANDLE_SIZE = 6

        private fun hasTooltipBeenShown(): Boolean = tooltipShownFile.exists()

        private fun markTooltipAsShown() {
            try {
                tooltipShownFile.parentFile.mkdirs()
                tooltipShownFile.createNewFile()
            } catch (e: Exception) {
                logger.error("Failed to mark tooltip as shown: ${e.message}", e)
            }
        }
    }

    private data class TooltipLayout(val x: Int, val y: Int, val width: Int, val height: Int, val buttonX: Int, val buttonY: Int, val buttonWidth: Int, val buttonHeight: Int)

    override fun init() {
        super.init()
        showCenterTooltip = !hasTooltipBeenShown()
    }

    override fun render(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        drawContext.fill(0, 0, width, height, 0x90000000.toInt())
        super.render(drawContext, mouseX, mouseY, delta)

        if (selectedElement != null && HudManager.getElement(selectedElement!!.id) == null) {
            deselectElement()
        }

        HudManager.getElements().forEach { element ->
            if (element.config.enabled) {
                element.render(drawContext, client!!.renderTickCounter)

                if (element == selectedElement) {
                    drawSelectionOutline(drawContext, element)
                    if (fullHudEditor && element.advancedSizing) {
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
            val infoText = "Selected: ${element.name} (${element.config.x}, ${element.config.y}) ${element.config.width}x${element.config.height}"
            drawContext.drawTextWithShadow(textRenderer, infoText, 10, height - 20, 0xFF00FF00.toInt())}
    }

    private fun drawSelectionOutline(drawContext: DrawContext, element: HudElement) {
        val config = element.config
        drawContext.drawBorder(config.x - 1, config.y - 1, config.width + 2, config.height + 2, COLOR_SELECTION_BORDER)
    }

    private fun drawResizeHandles(drawContext: DrawContext, element: HudElement) {
        val config = element.config
        val x = config.x
        val y = config.y
        val w = config.width
        val h = config.height
        val hs = HANDLE_SIZE / 2

        drawContext.fill(x - hs, y - hs, x + hs, y + hs, COLOR_HANDLE)
        drawContext.fill(x + w - hs, y - hs, x + w + hs, y + hs, COLOR_HANDLE)
        drawContext.fill(x - hs, y + h - hs, x + hs, y + h + hs, COLOR_HANDLE)
        drawContext.fill(x + w - hs, y + h - hs, x + w + hs, y + h + hs, COLOR_HANDLE)
    }

    private fun drawInfoBox(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
        val infoBoxX = width - INFO_BOX_SIZE - INFO_BOX_MARGIN
        val infoBoxY = INFO_BOX_MARGIN

        val isHovering = mouseX in infoBoxX..(infoBoxX + INFO_BOX_SIZE) && mouseY in infoBoxY..(infoBoxY + INFO_BOX_SIZE)
        showInfoBoxTooltip = isHovering

        val backgroundColor = if (isHovering) 0xCC444444.toInt() else 0xCC333333.toInt()
        drawContext.fill(infoBoxX, infoBoxY, infoBoxX + INFO_BOX_SIZE, infoBoxY + INFO_BOX_SIZE, backgroundColor)
        drawContext.drawBorder(infoBoxX, infoBoxY, INFO_BOX_SIZE, INFO_BOX_SIZE, if (isHovering) 0xFF888888.toInt() else 0xFF666666.toInt())

        val infoText = "?"
        val textWidth = textRenderer.getWidth(infoText)
        drawContext.drawTextWithShadow(textRenderer, infoText, infoBoxX + (INFO_BOX_SIZE - textWidth) / 2, infoBoxY + (INFO_BOX_SIZE - textRenderer.fontHeight) / 2, 0xFFFFFFFF.toInt())
    }

    private fun drawInfoBoxTooltip(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
        val tooltipText = getHelpTooltipLines()
        val padding = 6
        val lineHeight = textRenderer.fontHeight + 2
        val maxWidth = tooltipText.maxOfOrNull { textRenderer.getWidth(it) } ?: 0
        val tooltipWidth = maxWidth + padding * 2
        val tooltipHeight = tooltipText.size * lineHeight + padding * 2

        val infoBoxX = width - INFO_BOX_SIZE - INFO_BOX_MARGIN
        val tooltipX = infoBoxX - tooltipWidth - 10
        val tooltipY = INFO_BOX_MARGIN

        drawContext.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xCC000000.toInt())
        drawContext.drawBorder(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 0xFF666666.toInt())

        tooltipText.forEachIndexed { index, line ->
            drawContext.drawTextWithShadow(textRenderer, line, tooltipX + padding, tooltipY + padding + index * lineHeight, 0xFFFFFFFF.toInt())
        }
    }

    private fun drawCenterTooltip(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
        val layout = calculateCenterTooltipLayout() ?: return
        val tooltipText = getHelpTooltipLines()

        drawContext.fill(layout.x, layout.y, layout.x + layout.width, layout.y + layout.height, 0xDD000000.toInt())
        drawContext.drawBorder(layout.x, layout.y, layout.width, layout.height, 0xFF888888.toInt())

        tooltipText.forEachIndexed { index, line ->
            val textX = layout.x + (layout.width - textRenderer.getWidth(line)) / 2
            drawContext.drawTextWithShadow(textRenderer, line, textX, layout.y + 8 + index * (textRenderer.fontHeight + 3), 0xFFFFFFFF.toInt())
        }

        val isButtonHovered = mouseX in layout.buttonX..(layout.buttonX + layout.buttonWidth) && mouseY in layout.buttonY..(layout.buttonY + layout.buttonHeight)
        val buttonColor = if (isButtonHovered) 0xFF444444.toInt() else 0xFF333333.toInt()
        val buttonBorderColor = if (isButtonHovered) 0xFFAAAAAA.toInt() else 0xFF666666.toInt()
        val buttonText = "Understood"

        drawContext.fill(layout.buttonX, layout.buttonY, layout.buttonX + layout.buttonWidth, layout.buttonY + layout.buttonHeight, buttonColor)
        drawContext.drawBorder(layout.buttonX, layout.buttonY, layout.buttonWidth, layout.buttonHeight, buttonBorderColor)
        drawContext.drawTextWithShadow(textRenderer, buttonText, layout.buttonX + (layout.buttonWidth - textRenderer.getWidth(buttonText)) / 2, layout.buttonY + (layout.buttonHeight - textRenderer.fontHeight) / 2, 0xFFFFFFFF.toInt())
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        val mouseXInt = mouseX.toInt()
        val mouseYInt = mouseY.toInt()

        if (showCenterTooltip && isClickOnUnderstoodButton(mouseXInt, mouseYInt)) {
            showCenterTooltip = false
            markTooltipAsShown()
            return true
        }

        if (fullHudEditor && selectedElement != null) {
            val handle = selectedElement!!.getResizeHandle(mouseXInt, mouseYInt)
            if (handle != null) {
                isResizing = true
                resizeHandle = handle
                originalConfig = selectedElement!!.config.copy()
                return true
            }
        }

        val clickedElement = findElementAt(mouseXInt, mouseYInt)
        if (clickedElement != null) {
            selectedElement = clickedElement
            isDragging = true
            dragOffsetX = mouseXInt - clickedElement.config.x
            dragOffsetY = mouseYInt - clickedElement.config.y
            originalConfig = clickedElement.config.copy()
        } else {
            deselectElement()
        }

        if (selectedElement != null && fullHudEditor && selectedElement!!.advancedSizing) {
            val handle = selectedElement!!.getResizeHandle(mouseXInt, mouseYInt)
            if (handle != null) {
                isResizing = true
                resizeHandle = handle
                originalConfig = selectedElement!!.config.copy()
                return true
            }
        }
        return true
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
        val hoveredElement = findElementAt(mouseX.toInt(), mouseY.toInt()) ?: return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

        val scaleFactor = if (verticalAmount > 0) 1.1f else 1 / 1.1f
        val currentConfig = hoveredElement.config

        val newWidth = (currentConfig.width * scaleFactor).toInt()
        val newHeight = (currentConfig.height * scaleFactor).toInt()

        val clampedWidth = max(hoveredElement.getMinWidth(), newWidth)
        val clampedHeight = max(hoveredElement.getMinHeight(), newHeight)

        hoveredElement.updateConfig(currentConfig.copy(width = clampedWidth, height = clampedHeight))
        selectedElement = hoveredElement

        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (button != 0 || selectedElement == null) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)

        val element = selectedElement!!
        val mouseXInt = mouseX.toInt()
        val mouseYInt = mouseY.toInt()

        if (isDragging) {
            val newX = mouseXInt - dragOffsetX
            val newY = mouseYInt - dragOffsetY
            val clampedX = max(0, min(width - element.config.width, newX))
            val clampedY = max(0, min(height - element.config.height, newY))
            element.updateConfig(element.config.copy(x = clampedX, y = clampedY))
            return true
        }

        if (isResizing && fullHudEditor && resizeHandle != null) {
            val original = originalConfig ?: element.config
            val allowFreeResize = (GLFW.glfwGetKey(client!!.window.handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(client!!.window.handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) && element.advancedSizing

            if (allowFreeResize) {
                handleFreeResize(element, original, mouseXInt, mouseYInt)
            } else {
                handleAspectRatioResize(element, original, mouseXInt, mouseYInt)
            }
            return true
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_R && selectedElement != null) {
            selectedElement!!.resetToDefault()
            return true
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun close() {
        HudManager.saveConfig()
        super.close()
    }

    private fun handleFreeResize(element: HudElement, original: HudElementConfig, mouseX: Int, mouseY: Int) {
        var newX = original.x
        var newY = original.y
        var newWidth = original.width
        var newHeight = original.height
        val minWidth = element.getMinWidth()
        val minHeight = element.getMinHeight()

        when (resizeHandle) {
            HudElement.ResizeHandle.BOTTOM_RIGHT -> {
                newWidth = max(minWidth, mouseX - original.x)
                newHeight = max(minHeight, mouseY - original.y)
            }
            HudElement.ResizeHandle.BOTTOM_LEFT -> {
                newX = min(mouseX, original.x + original.width - minWidth)
                newWidth = original.x + original.width - newX
                newHeight = max(minHeight, mouseY - original.y)
            }
            HudElement.ResizeHandle.TOP_RIGHT -> {
                newY = min(mouseY, original.y + original.height - minHeight)
                newWidth = max(minWidth, mouseX - original.x)
                newHeight = original.y + original.height - newY
            }
            HudElement.ResizeHandle.TOP_LEFT -> {
                newX = min(mouseX, original.x + original.width - minWidth)
                newY = min(mouseY, original.y + original.height - minHeight)
                newWidth = original.x + original.width - newX
                newHeight = original.y + original.height - newY
            }
            else -> {}
        }
        element.updateConfig(original.copy(x = newX, y = newY, width = newWidth, height = newHeight))
    }

    private fun handleAspectRatioResize(element: HudElement, original: HudElementConfig, mouseX: Int, mouseY: Int) {
        val centerX = original.x + original.width / 2
        val centerY = original.y + original.height / 2
        val originalDistX = kotlin.math.abs(original.width / 2.0)
        val originalDistY = kotlin.math.abs(original.height / 2.0)
        val newDistX = kotlin.math.abs(mouseX - centerX)
        val newDistY = kotlin.math.abs(mouseY - centerY)
        val scaleX = if (originalDistX > 0) newDistX / originalDistX else 1.0
        val scaleY = if (originalDistY > 0) newDistY / originalDistY else 1.0
        val scale = max(scaleX, scaleY)
        val newWidth = max(element.getMinWidth(), (original.width * scale).toInt())
        val newHeight = max(element.getMinHeight(), (original.height * scale).toInt())
        val newX = centerX - newWidth / 2
        val newY = centerY - newHeight / 2
        val clampedX = max(0, min(width - newWidth, newX))
        val clampedY = max(0, min(height - newHeight, newY))
        val finalWidth = min(newWidth, width - clampedX)
        val finalHeight = min(newHeight, height - clampedY)
        element.updateConfig(original.copy(x = clampedX, y = clampedY, width = finalWidth, height = finalHeight))
    }

    private fun findElementAt(mouseX: Int, mouseY: Int): HudElement? {
        return HudManager.getElements().lastOrNull { it.config.enabled && it.isPointInside(mouseX, mouseY) }
    }

    private fun deselectElement() {
        selectedElement = null
        isDragging = false
        isResizing = false
        originalConfig = null
        resizeHandle = null
    }

    private fun getHelpTooltipLines(): List<String> {
        return if (fullHudEditor) {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over an element to resize",
                "Drag corners to resize (hold Ctrl for free resize)",
                "Press R to reset the selected element"
            )
        } else {
            listOf(
                "Click and drag elements to move them",
                "Scroll wheel over an element to resize",
                "Press R to reset the selected element"
            )
        }
    }

    private fun calculateCenterTooltipLayout(): TooltipLayout? {
        val tooltipText = getHelpTooltipLines()
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

        return TooltipLayout(tooltipX, tooltipY, tooltipWidth, tooltipHeight, buttonX, buttonY, buttonWidth, buttonHeight)
    }

    private fun isClickOnUnderstoodButton(mouseX: Int, mouseY: Int): Boolean {
        if (!showCenterTooltip) return false
        val layout = calculateCenterTooltipLayout() ?: return false
        return mouseX in layout.buttonX..(layout.buttonX + layout.buttonWidth) &&
                mouseY in layout.buttonY..(layout.buttonY + layout.buttonHeight)
    }
}
package io.github.frostzie.skyfall.features.inventory.attribute

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.events.TooltipEvents
import io.github.frostzie.skyfall.utils.item.CustomLoreUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object HideDescription {
    private val logger = LoggerProvider.getLogger("HideDescription")
    private val config get() = SkyFall.Companion.feature.inventory.attributeMenu

    fun init() {
        registerTooltipEvent()
    }

    private fun registerTooltipEvent() {
        TooltipEvents.register { stack, lines ->
            onTooltipRender(stack, lines)
        }
    }

    fun onTooltipRender(stack: ItemStack, lines: MutableList<Text>) {
        if (!config.hideDescription || stack.isEmpty) {
            return
        }

        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen !is HandledScreen<*>) {
            return
        }
        val screenTitle = currentScreen.title.string
        if (!screenTitle.equals("Hunting Box", ignoreCase = true)) { // Changed to ignoreCase = true for robustness
            return
        }

        hideDescriptionLines(lines)
    }

    /**
     * Recursively checks if a Text component or any of its siblings are explicitly styled as italic.
     * This is necessary because a single line of text can be composed of multiple
     * parts with different styles.
     *
     * @param text The Text component to check.
     * @return `true` if any part of the text is explicitly styled as italic, `false` otherwise.
     */
    private fun isTextItalic(text: Text): Boolean {
        if (text.style.isItalic == true) {
            return true
        }
        for (sibling in text.siblings) {
            if (isTextItalic(sibling)) {
                return true
            }
        }
        return false
    }

    private fun hideDescriptionLines(lines: MutableList<Text>) {
        try {
            CustomLoreUtils.removeLoreIf(lines) { line ->
                isTextItalic(line)
            }
        } catch (e: Exception) {
            logger.error("Failed to hide description lines", e)
        }
    }
}
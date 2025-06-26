package io.github.frostzie.skyfall.features.inventory.attribute

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.item.StackCountRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.Slot

object LevelNumber {
    private val logger = LoggerProvider.getLogger("LevelNumber")
    private val config get() = SkyFall.feature.inventory.attributeMenu

    private val validSlotRanges = setOf(
        10..16, 19..25, 28..34, 37..43
    )

    private val romanNumerals = mapOf(
        "I" to 1, "II" to 2, "III" to 3, "IV" to 4, "V" to 5,
        "VI" to 6, "VII" to 7, "VIII" to 8, "IX" to 9, "X" to 10
    )

    fun init() {
        StackCountRenderer.registerProvider { slot ->
            if (!config.levelNumber) {
                return@registerProvider null
            }

            val currentScreen = MinecraftClient.getInstance().currentScreen
            if (currentScreen !is HandledScreen<*> || !isAttributeMenu(currentScreen)) {
                return@registerProvider null
            }

            if (!isSlotInChestInventory(slot) || !validSlotRanges.any { slot.index in it } || slot.stack.isEmpty) {
                return@registerProvider null
            }

            extractRomanNumeral(slot.stack.name.string)
        }
    }

    private fun extractRomanNumeral(itemName: String): Int? {
        try {
            val cleanName = ColorUtils.stripColorCodes(itemName).trim()
            val pattern = Regex("\\b([IVX]{1,4})\\s*$")
            val match = pattern.find(cleanName)

            if (match != null) {
                val roman = match.groupValues[1]
                val level = romanNumerals[roman]
                if (level != null) {
                    logger.debug("Found roman numeral '$roman' (level $level) in item: '$cleanName'")
                    return level
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to extract roman numeral from item name: '$itemName'", e)
        }
        return null
    }

    private fun isAttributeMenu(screen: HandledScreen<*>): Boolean {
        return screen.title.string.equals("Attribute Menu", ignoreCase = true)
    }

    private fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
    }
}
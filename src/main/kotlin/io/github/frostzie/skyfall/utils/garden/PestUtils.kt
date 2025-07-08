package io.github.frostzie.skyfall.utils.garden

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.processors.ScoreboardProcessor
import io.github.frostzie.skyfall.utils.processors.TabListProcessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
//TODO: Rework and clean up
/**
 * A data class to hold structured information about a group of pests.
 *
 * @property name The name of the pest (e.g., "Mite").
 * @property alive The total number of pests alive (from tab list, -1 if unknown).
 * @property plots A map of plot numbers to their specific pest count. A count of -1 indicates an unknown number of pests on that plot.
 */
data class PestInfo(val name: String, val alive: Int, val plots: Map<Int, Int>)

/**
 * A utility object for parsing information about active pests from the tab list, scoreboard, and inventory menus
 * for the Garden island. This provides raw data extraction without caching - caching is handled by PestDetector.
 */
object PestUtils {
    private val logger = LoggerProvider.getLogger("PestUtils")

    private val ALIVE_COUNT_REGEX = "Alive:\\s*(\\d+)".toRegex()
    private val PLOTS_REGEX = "Plots:\\s*(.*)".toRegex()
    private val SCOREBOARD_PEST_REGEX = "Plot - (\\d+)\\s*ൠ\\s*x(\\d+)".toRegex()
    private val MENU_PLOT_NAME_REGEX = "Plot - (\\d+)".toRegex()
    private val MENU_PLOT_LORE_REGEX = "This plot has (\\d+) Pest(s)?!".toRegex()

    private const val CONFIGURE_PLOTS_TITLE = "Configure Plots"
    private val PLOT_MENU_SLOT_RANGES = setOf(
        2..6, 11..15, 20..24, 29..33, 38..42
    )

    /**
     * Scans all sources for pest data and returns the current state.
     * This method performs the actual data extraction without caching.
     *
     * @return A list of PestInfo objects representing current pest state
     */
    fun getActivePests(): List<PestInfo> {
        val client = MinecraftClient.getInstance()
        val screen = client.currentScreen

        val menuIsOpen = (screen as? HandledScreen<*>)?.let {
            ColorUtils.stripColorCodes(it.title.string) == CONFIGURE_PLOTS_TITLE
        } == true

        if (menuIsOpen) {
            return getActivePestsFromMenu()
        } else {
            return getActivePestsFromTabAndScoreboard()
        }
    }

    /**
     * Gets pest data when the Configure Plots menu is open (definitive source).
     */
    private fun getActivePestsFromMenu(): List<PestInfo> {
        val menuPlotCounts = parsePestMenu()
        val totalAliveFromTab = getAliveCountFromTab()

        return if (menuPlotCounts.any { it.value > 0 }) {
            listOf(PestInfo(name = "Pest", alive = totalAliveFromTab, plots = menuPlotCounts))
        } else {
            emptyList()
        }
    }

    /**
     * Gets pest data from tab list and scoreboard when menu is closed.
     */
    private fun getActivePestsFromTabAndScoreboard(): List<PestInfo> {
        val totalAlive = getAliveCountFromTab()
        val tabPestPlots = getPestPlotsFromTab()

        if (tabPestPlots.isEmpty()) {
            return emptyList()
        }

        val plotCounts = tabPestPlots.associateWith { -1 }.toMutableMap()

        val scoreboardPlotData = getCurrentScoreboardPlot()
        scoreboardPlotData?.let { (plotId, pestCount) ->
            plotCounts[plotId] = pestCount
        }

        return listOf(PestInfo(name = "Pest", alive = totalAlive, plots = plotCounts))
    }

    /**
     * Extracts the total alive count from the tab list.
     */
    private fun getAliveCountFromTab(): Int {
        return TabListProcessor.getTabList()
            .firstNotNullOfOrNull {
                ALIVE_COUNT_REGEX.find(ColorUtils.stripColorCodes(it))
            }
            ?.groupValues?.get(1)?.toIntOrNull() ?: -1
    }

    /**
     * Extracts the list of pest plots from the tab list.
     */
    private fun getPestPlotsFromTab(): List<Int> {
        return TabListProcessor.getTabList()
            .firstNotNullOfOrNull {
                PLOTS_REGEX.find(ColorUtils.stripColorCodes(it))
            }
            ?.groupValues?.get(1)
            ?.split(',')
            ?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
    }

    /**
     * Gets the current pest plot information from the scoreboard.
     */
    private fun getCurrentScoreboardPlot(): Pair<Int, Int>? {
        for (rawLine in ScoreboardProcessor.getScoreboard()) {
            val cleanLine = ColorUtils.stripColorCodes(rawLine)
            val match = SCOREBOARD_PEST_REGEX.find(cleanLine)
            if (match != null) {
                val plotId = match.groupValues[1].toIntOrNull()
                val pestCount = match.groupValues[2].toIntOrNull()
                if (plotId != null && pestCount != null) {
                    return plotId to pestCount
                }
            }
        }
        return null
    }

    /**
     * Parses the "Configure Plots" menu for specific pest counts.
     * @return A map of Plot ID to Pest Count, or an empty map if the menu isn't open.
     */
    private fun parsePestMenu(): Map<Int, Int> {
        val client = MinecraftClient.getInstance()
        val screen = client.currentScreen
        if (screen !is HandledScreen<*> || ColorUtils.stripColorCodes(screen.title.string) != CONFIGURE_PLOTS_TITLE) {
            return emptyMap()
        }

        val plotCounts = mutableMapOf<Int, Int>()
        val slots = screen.screenHandler.slots

        for (slot in slots) {
            if (slot.inventory is PlayerInventory || !PLOT_MENU_SLOT_RANGES.any { slot.index in it }) {
                continue
            }

            val stack = slot.stack
            if (stack.isEmpty) {
                continue
            }

            val itemName = ColorUtils.stripColorCodes(stack.name.string)
            val plotIdMatch = MENU_PLOT_NAME_REGEX.find(itemName)
            val plotId = plotIdMatch?.groupValues?.get(1)?.toIntOrNull() ?: continue

            plotCounts[plotId] = 0

            val tooltipLines = getLoreFromItemStack(stack)
            for (line in tooltipLines) {
                val cleanLoreLine = ColorUtils.stripColorCodes(line)
                val pestCountMatch = MENU_PLOT_LORE_REGEX.find(cleanLoreLine)
                if (pestCountMatch != null) {
                    val pestCount = pestCountMatch.groupValues[1].toIntOrNull()
                    if (pestCount != null) {
                        plotCounts[plotId] = pestCount
                        break
                    }
                }
            }
        }
        return plotCounts
    }

    /**
     * Checks if the Configure Plots menu is currently open.
     */
    fun isMenuOpen(): Boolean {
        val client = MinecraftClient.getInstance()
        val screen = client.currentScreen
        return (screen as? HandledScreen<*>)?.let {
            ColorUtils.stripColorCodes(it.title.string) == CONFIGURE_PLOTS_TITLE
        } == true
    }

    /**
     * Extracts lore from an ItemStack by encoding it to JSON and parsing the raw lore component.
     * This is more reliable than getTooltip for heavily modified items.
     */
    private fun getLoreFromItemStack(itemStack: ItemStack): List<String> {
        try {
            val client = MinecraftClient.getInstance()
            val player = client.player ?: return emptyList()
            val registryManager = player.registryManager ?: return emptyList()
            val jsonElement = ItemStack.CODEC.encodeStart(registryManager.getOps(JsonOps.INSTANCE), itemStack)
                .getOrThrow { error -> RuntimeException("Failed to encode ItemStack: $error") }
            val jsonObject = jsonElement.asJsonObject
            if (jsonObject.has("components")) {
                val components = jsonObject.getAsJsonObject("components")
                if (components.has("minecraft:lore")) {
                    val loreArray = components.getAsJsonArray("minecraft:lore")
                    val formattedLore = formatMinecraftLore(loreArray)
                    return formattedLore.map { it.asString }
                }
            }
        } catch (e: Exception) {
            logger.error("Error extracting lore from item: ${e.message}", e)
        }
        return emptyList()
    }

    /**
     * Reconstructs colored lore strings from their raw JSON representation.
     */
    private fun formatMinecraftLore(loreArray: JsonArray): List<JsonElement> {
        val result = mutableListOf<JsonElement>()
        try {
            loreArray.forEach { element ->
                if (element?.isJsonObject == true) {
                    val obj = element.asJsonObject
                    val extra = obj.getAsJsonArray("extra")
                    if (extra != null) {
                        val lineText = StringBuilder()
                        extra.forEach { extraElement ->
                            try {
                                if (extraElement?.isJsonObject == true) {
                                    val extraObj = extraElement.asJsonObject
                                    val text = extraObj.get("text")?.asString ?: ""
                                    val color = extraObj.get("color")?.asString ?: ""
                                    val colorCode = ColorUtils.getColorCode(color)
                                    val formatCodes = ColorUtils.formatCodes(extraObj)
                                    lineText.append(colorCode).append(formatCodes).append(text)
                                } else if (extraElement?.isJsonPrimitive == true) {
                                    lineText.append(extraElement.asString)
                                }
                            } catch (e: Exception) {
                                logger.warn("Error processing extra element: ${e.message}")
                            }
                        }
                        if (lineText.isNotEmpty()) {
                            result.add(JsonPrimitive(lineText.toString()))
                        }
                    } else {
                        val text = obj.get("text")?.asString ?: ""
                        if (text.isNotEmpty()) {
                            result.add(JsonPrimitive(text))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing lore array: ${e.message}", e)
            return loreArray.toList()
        }
        return result
    }
}
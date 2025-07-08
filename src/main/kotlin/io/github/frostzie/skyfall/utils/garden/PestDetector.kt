package io.github.frostzie.skyfall.utils.garden

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.events.EventBus
import io.github.frostzie.skyfall.utils.events.IslandChangeEvent
import io.github.frostzie.skyfall.utils.events.TabListUpdateEvent

/**
 * A simple data class to hold pest information derived only from the tab list.
 */
data class PestData(
    val totalAlive: String,
    val infestedPlots: Set<Int>
) {
    companion object {
        val NONE = PestData("None", emptySet())
    }
}

/**
 * A simplified utility to detect pest information *only* from the tab list.
 * This is now event-driven and caches its data for high performance, reacting
 * to events from the new processors.
 */
object PestDetector {
    private val logger = LoggerProvider.getLogger("PestDetector")


    private val PLOTS_REGEX = "Plots:\\s*(.*)".toRegex()
    private val ALIVE_COUNT_REGEX = "Alive:\\s*(\\d+)".toRegex()

    private var cachedPestData: PestData = PestData.NONE
    private var onGarden: Boolean = false

    /**
     * Initializes the detector by listening for island and tab list updates.
     */
    fun init() {
        EventBus.listen(IslandChangeEvent::class.java) { event ->
            onGarden = event.newIsland == IslandType.GARDEN

            if (!onGarden) {
                cachedPestData = PestData.NONE
            }
        }

        EventBus.listen(TabListUpdateEvent::class.java) { event ->
            if (onGarden) {
                recalculatePestData(event.tabList)
            }
        }
    }

    /**
     * Gets the cached pest data. This is now extremely fast as it just returns a variable.
     * @return The most recently calculated [PestData].
     */
    fun getPestData(): PestData {
        return cachedPestData
    }

    /**
     * Recalculates pest data from the provided tab list lines.
     * This logic is now more robust to handle the "no pests" state correctly.
     * @param tabLines The raw list of strings from the tab list.
     */
    private fun recalculatePestData(tabLines: List<String>) {
        try {
            val cleanLines = tabLines.map { ColorUtils.stripColorCodes(it) }

            val plotsString = cleanLines.firstNotNullOfOrNull { line ->
                PLOTS_REGEX.find(line)?.groupValues?.getOrNull(1)
            }

            if (plotsString == null) {
                cachedPestData = PestData.NONE
                return
            }

            val infestedPlots = plotsString
                .split(',')
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()

            val totalAlive = cleanLines.firstNotNullOfOrNull { line ->
                ALIVE_COUNT_REGEX.find(line)?.groupValues?.getOrNull(1)
            } ?: "N/A"

            cachedPestData = PestData(totalAlive, infestedPlots)
        } catch (e: Exception) {
            logger.error("Error recalculating pest data: ${e.message}", e)
            cachedPestData = PestData.NONE
        }
    }
}
package io.github.frostzie.skyfall.utils.garden

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.IslandChangeEvent
import io.github.frostzie.skyfall.events.TabListUpdateEvent

/**
 * A simple data class to hold the current visitor count.
 */
data class VisitorData(
    val count: Int
) {
    companion object {
        val NONE = VisitorData(0)
    }
}

/**
 * An event-driven utility that detects and caches the number of active visitors
 * at the Garden's main desk.
 */
object VisitorUtils {
    private var cachedVisitorData: VisitorData = VisitorData.NONE
    private var onGarden: Boolean = false

    fun init() {
        EventBus.listen(IslandChangeEvent::class.java) { event ->
            onGarden = event.newIsland == IslandType.GARDEN
            if (!onGarden) {
                cachedVisitorData = VisitorData.NONE
            }
        }

        EventBus.listen(TabListUpdateEvent::class.java) { event ->
            if (onGarden) {
                recalculateVisitorData(event.tabList)
            }
        }
    }

    fun getVisitorData(): VisitorData {
        return cachedVisitorData
    }

    private fun recalculateVisitorData(tabLines: List<String>) {
        var visitorCount: Int? = null
        val cleanLines = tabLines.map { ColorUtils.stripColorCodes(it) }
        for (line in cleanLines) {
            val match = "§b§lVisitors: §r§f\\((.*)\\)".toRegex().find(line)
            if (match != null) {
                val countInfo = match.groupValues[1]
                visitorCount = when {
                    countInfo.contains("Queue Full") -> 5
                    countInfo.toIntOrNull() != null -> countInfo.toInt()
                    else -> null
                }
                if (visitorCount != null) break
            }
        }

        if (visitorCount != null) {
            if (cachedVisitorData.count != visitorCount) {
                cachedVisitorData = VisitorData(visitorCount)

            }
        }
    }
}
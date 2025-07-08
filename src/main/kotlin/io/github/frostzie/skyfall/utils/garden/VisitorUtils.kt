package io.github.frostzie.skyfall.utils.garden

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.events.EventBus
import io.github.frostzie.skyfall.utils.events.IslandChangeEvent
import io.github.frostzie.skyfall.utils.events.TabListUpdateEvent

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

    private val logger = LoggerProvider.getLogger("VisitorUtils")

    private val VISITOR_PATTERNS = listOf(
        "§b§lVisitors: §r§f\\((.*)\\)".toRegex(),
        "Visitors: \\((.*)\\)".toRegex(),
    )

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

        for (line in tabLines) {
            for (pattern in VISITOR_PATTERNS) {
                val match = pattern.find(line)
                if (match != null) {
                    val countInfo = match.groupValues[1]
                    visitorCount = when {
                        countInfo.contains("Queue Full") || countInfo.contains("§r§c§lQueue Full!§r§f") -> 5
                        countInfo.toIntOrNull() != null -> countInfo.toInt()
                        else -> null
                    }
                    if (visitorCount != null) break
                }
            }
            if (visitorCount != null) break
        }

        if (visitorCount == null) {
            val cleanLines = tabLines.map { ColorUtils.stripColorCodes(it) }
            for (line in cleanLines) {
                for (pattern in VISITOR_PATTERNS) {
                    val match = pattern.find(line)
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
                if (visitorCount != null) break
            }
        }

        cachedVisitorData = if (visitorCount != null) VisitorData(visitorCount) else VisitorData.NONE
    }
}
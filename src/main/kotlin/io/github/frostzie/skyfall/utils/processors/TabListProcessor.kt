package io.github.frostzie.skyfall.utils.processors

import io.github.frostzie.skyfall.mixin.accessor.TabListAccessor
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.events.TabListUpdateEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import java.util.concurrent.ConcurrentHashMap

/**
 * Event-driven tab list processor that fires events when tab list state changes
 */
object TabListProcessor {
    private val logger = LoggerProvider.getLogger("TabListProcessor")
    private val tabList = mutableListOf<String>()
    private val errorCount = ConcurrentHashMap<String, Int>()

    private var currentArea: String? = null
    private var currentProfile: String? = null
    private val currentValues = HashMap<String, String?>()

    /**
     * Processes tab list and fires events when state changes
     */
    fun processTabList() {
        try {
            val client = MinecraftClient.getInstance()
            val networkHandler = client.networkHandler ?: return
            val players = try {
                (networkHandler as TabListAccessor).playerList
            } catch (e: Exception) {
                logger.error("Failed to access player list: ${e.message}")
                return
            }

            val newTabList = mutableListOf<String>()
            players.forEach { player ->
                processPlayerEntry(player, newTabList)
            }

            if (newTabList.sorted() == tabList.sorted()) {
                return
            }

            tabList.clear()
            tabList.addAll(newTabList)

            processTabListChanges()

        } catch (e: Exception) {
            logger.error("Error in processTabList: ${e.message}")
        }
    }

    private fun processTabListChanges() {
        currentValues.clear()
        val keyValuePattern = "(.+?):\\s*(.+)".toRegex()

        for (line in tabList) {
            val cleanLine = ColorUtils.stripColorCodes(line)
            keyValuePattern.find(cleanLine)?.let { matchResult ->
                val key = matchResult.groupValues[1].trim()
                val value = matchResult.groupValues[2].trim()
                currentValues[key] = value
            }
        }

        currentArea = tabList.find { it.contains("Area:") || it.contains("Dungeon:") }
        currentProfile = tabList.find { it.contains("Profile:") }

        TabListUpdateEvent(
            tabList.toList(),
            currentArea,
            currentProfile,
            currentValues.toMap()
        ).post()
    }

    private fun processPlayerEntry(player: PlayerListEntry, targetList: MutableList<String>) {
        try {
            val displayName = try {
                player.displayName?.string ?: player.profile.name
            } catch (e: Exception) {
                try {
                    player.profile.name
                } catch (e2: Exception) {
                    val identifier = try {
                        "Player-${player.hashCode()}"
                    } catch (e3: Exception) {
                        "Unknown-${System.nanoTime()}"
                    }

                    val errorKey = "player-${identifier}"
                    val count = errorCount.compute(errorKey) { _, v -> (v ?: 0) + 1 } ?: 1

                    if (count <= 3) {
                        logger.debug("Could not get display name for player (attempt $count): ${e.message}")
                    }

                    "Unknown Player"
                }
            }
            targetList.add(displayName)
        } catch (e: Exception) {
            logger.debug("Error processing player in tab list: ${e.message}")
        }
    }

    /**
     * Gets a specific value from the tab list by label
     *
     * @param label The label to search for (without colon)
     * @return The value, or null if not found
     */
    fun getValue(label: String): String? {
        return currentValues[label]
    }

    /**
     * Gets all extracted values from the tab list
     *
     * @return Map of all extracted label-value pairs
     */
    fun getAllValues(): Map<String, String?> {
        return currentValues.toMap()
    }

    fun getArea(): String = currentArea ?: "§cNo Area Found!"
    fun getProfile(): String = currentProfile ?: "§cNo Profile Found!"
    fun getTabList(): List<String> = tabList
}
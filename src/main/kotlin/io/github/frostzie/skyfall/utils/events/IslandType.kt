package io.github.frostzie.skyfall.utils.events
//TODO: change name
import io.github.frostzie.skyfall.data.IslandType

/**
 * Event fired when scoreboard data changes
 */
class ScoreboardUpdateEvent(
    val scoreboard: List<String>,
    val isOnSkyblock: Boolean,
    val isInRift: Boolean,
    val currentIsland: IslandType,
    val region: String?
) : Event()

/**
 * Event fired when tab list data changes
 */
class TabListUpdateEvent(
    val tabList: List<String>,
    val area: String?,
    val profile: String?,
    val allValues: Map<String, String?>
) : Event()

/**
 * Event fired when island changes (replaces IslandChangeEvent)
 */
class IslandChangeEvent(
    val oldIsland: IslandType,
    val newIsland: IslandType
) : Event()

/**
 * Event fired when slayer quest state changes
 */
class SlayerQuestEvent(
    val isOnQuest: Boolean,
    val bossSpawned: Boolean
) : Event()

/**
 * Event fired when player enters/leaves Skyblock
 */
class SkyblockStateEvent(
    val isOnSkyblock: Boolean,
    val wasOnSkyblock: Boolean
) : Event()

/**
 * Event fired when player enters/leaves Rift
 */
class RiftStateEvent(
    val isInRift: Boolean,
    val wasInRift: Boolean
) : Event()
package io.github.frostzie.skyfall.data

// Taken from Skyhanni
enum class IslandType(val displayName: String) {
    PRIVATE_ISLAND("Private Island"),
    GARDEN("Garden"),
    SPIDER_DEN("Spider's Den"),
    CRIMSON_ISLE("Crimson Isle"),
    THE_END("The End"),
    GOLD_MINE("Gold Mine"),
    DEEP_CAVERNS("Deep Caverns"),
    DWARVEN_MINES("Dwarven Mines"),
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    FARMING_ISLANDS("The Farming Islands"),
    THE_PARK("The Park"),
    GALATEA("Galatea"),
    CATACOMBS("Catacombs"),
    DUNGEON_HUB("Dungeon Hub"),
    HUB("Hub"),
    DARK_AUCTION("Dark Auction"),
    JERRY_WORKSHOP("Jerry's Workshop"),
    KUUDRA("Kuudra"),
    MINESHAFT("Mineshaft"),
    RIFT("The Rift"),
    BACKWATER_BAYOU("Backwater Bayou"),
    UNKNOWN("Unknown");

    fun isOneOf(vararg types: IslandType): Boolean {
        return this in types
    }

    companion object {
        fun fromDisplayName(name: String): IslandType {
            return entries.firstOrNull {
                name.contains(it.displayName, ignoreCase = true)
            } ?: UNKNOWN
        }
    }
}
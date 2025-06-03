package io.github.frostzie.skyfall.data

/**
 * Enum representing different types of Skyblock farming tools.
 *
 * @property baseSkyblockId The core Skyblock ID string. For leveled items, this is the ID prefix without "_1", "_2", etc.
 * @property hasLevels Indicates if this tool type can have different levels (e.g., "_1", "_2", "_3").
 * @property maxLevel If the tool has levels, this specifies the maximum expected level.
 */
enum class FarmingToolTypes(val baseSkyblockId: String, val hasLevels: Boolean = false, val maxLevel: Int = 1) {
        ADVANCED_GARDENING_AXE("ADVANCED_GARDENING_AXE"),
        BASIC_GARDENING_AXE("BASIC_GARDENING_AXE"),
        ADVANCED_GARDENING_HOE("ADVANCED_GARDENING_HOE"),
        BASIC_GARDENING_HOE("BASIC_GARDENING_HOE"),
        CACTUS_KNIFE("CACTUS_KNIFE"),
        COCO_CHOPPER("COCO_CHOPPER"),
        FUNGI_CUTTER("FUNGI_CUTTER"),

        THEORETICAL_HOE_WHEAT("THEORETICAL_HOE_WHEAT", true, 3),
        THEORETICAL_HOE_WARTS("THEORETICAL_HOE_WARTS", true, 3),
        THEORETICAL_HOE_POTATO("THEORETICAL_HOE_POTATO", true, 3),
        THEORETICAL_HOE_CARROT("THEORETICAL_HOE_CARROT", true, 3),
        THEORETICAL_HOE_CANE("THEORETICAL_HOE_CANE", true, 3),
        PUMPKIN_DICER("PUMPKIN_DICER", true, 3),
        MELON_DICER("MELON_DICER", true, 3);

    /**
     * Checks if the given actual Skyblock ID matches this farming tool type.
     * For leveled tools, it checks if the ID starts with the base ID and ends with a valid level suffix.
     *
     * @param actualSkyblockId The Skyblock ID string from an item.
     * @return True if it matches this tool type, false otherwise.
     */
    fun matches(actualSkyblockId: String?): Boolean {
        if (actualSkyblockId.isNullOrEmpty()) {
            return false
        }

        return if (hasLevels) {
            if (actualSkyblockId.startsWith(this.baseSkyblockId + "_")) {
                val levelString = actualSkyblockId.substringAfterLast('_')
                val level = levelString.toIntOrNull()
                level != null && level in 1..this.maxLevel
            } else {
                false
            }
        } else {
            actualSkyblockId == this.baseSkyblockId
        }
    }

    companion object {
        /**
         * Determines the [FarmingToolTypes] type from a given Skyblock ID string.
         *
         * @param actualSkyblockId The Skyblock ID string from an item.
         * @return The matching [FarmingToolTypes] enum constant, or null if no match is found.
         */
        fun getToolType(actualSkyblockId: String?): FarmingToolTypes? {
            if (actualSkyblockId.isNullOrEmpty()) {
                return null
            }
            return entries.find { it.matches(actualSkyblockId) }
        }
    }
}
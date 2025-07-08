package io.github.frostzie.skyfall.data

data class Vec2i(val x: Int, val z: Int)

/**
 * Represents a single plot in the Garden, including its ID and 2D boundaries.
 *
 * @property id The official number of the plot (e.g., 1, 2, ... 24).
 * @property min The minimum corner of the plot's bounding box (inclusive).
 * @property max The maximum corner of the plot's bounding box (inclusive).
 */
data class GardenPlot(val id: Int, val min: Vec2i, val max: Vec2i) {
    /**
     * Checks if a given 2D point is within this plot's boundaries.
     * @param point The point to check.
     * @return True if the point is inside the plot, false otherwise.
     */
    fun contains(point: Vec2i): Boolean {
        return point.x >= min.x && point.x <= max.x && point.z >= min.z && point.z <= max.z
    }
}

/**
 * A singleton object that holds data and utility functions for all Garden plots.
 * It now uses a manually defined map for plot boundaries to ensure accuracy.
 */
object GardenPlots {

    private val MANUAL_PLOT_BOUNDS = mapOf(
        0 to (Vec2i(-48, -48) to Vec2i(47, 47)),
        1 to (Vec2i(-48, -144) to Vec2i(47, -49)),
        2 to (Vec2i(-144, -48) to Vec2i(-49, 47)),
        3 to (Vec2i(48, -48) to Vec2i(143, 47)),
        4 to (Vec2i(-48, 48) to Vec2i(47, 143)),
        5 to (Vec2i(-144, -144) to Vec2i(-49, -49)),
        6 to (Vec2i(48, -144) to Vec2i(143, -49)),
        7 to (Vec2i(-144, 48) to Vec2i(-49, 143)),
        8 to (Vec2i(48, 48) to Vec2i(143, 143)),
        9 to (Vec2i(-48, -240) to Vec2i(47, -145)),
        10 to (Vec2i(-240, -48) to Vec2i(-145, 47)),
        11 to (Vec2i(144, -48) to Vec2i(239, 47)),
        12 to (Vec2i(-48, 144) to Vec2i(47, 239)),
        13 to (Vec2i(-144, -240) to Vec2i(-49, -145)),
        14 to (Vec2i(48, -240) to Vec2i(143, -145)),
        15 to (Vec2i(-240, -144) to Vec2i(-145, -49)),
        16 to (Vec2i(144, -144) to Vec2i(239, -49)),
        17 to (Vec2i(-240, 48) to Vec2i(-145, 143)),
        18 to (Vec2i(144, 48) to Vec2i(239, 143)),
        19 to (Vec2i(-144, 144) to Vec2i(-49, 239)),
        20 to (Vec2i(48, 144) to Vec2i(143, 239)),
        21 to (Vec2i(-240, -240) to Vec2i(-145, -145)),
        22 to (Vec2i(144, -240) to Vec2i(239, -145)),
        23 to (Vec2i(-240, 144) to Vec2i(-145, 239)),
        24 to (Vec2i(144, 144) to Vec2i(239, 239))
    )

    /**
     * A list of all defined GardenPlot objects, now created from the manual map.
     */
    val allPlots: List<GardenPlot> by lazy {
        MANUAL_PLOT_BOUNDS.map { (id, bounds) ->
            GardenPlot(id, bounds.first, bounds.second)
        }
    }

    /**
     * Finds which plot the player is currently in based on their coordinates.
     * This function does not need to change.
     *
     * @param playerX The player's current X coordinate.
     * @param playerZ The player's current Z coordinate.
     * @return The [GardenPlot] the player is in, or null if they are not in any defined plot.
     */
    fun getPlotAt(playerX: Double, playerZ: Double): GardenPlot? {
        val playerPos = Vec2i(playerX.toInt(), playerZ.toInt())
        return allPlots.find { it.contains(playerPos) }
    }
}
package io.github.frostzie.skyfall.waypoints

import io.github.frostzie.skyfall.utils.ColorUtils.renderBlock
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.util.math.Vec3d

object WaypointFeature {
    private val WAYPOINT_POS = Vec3d(0.0, -58.0, 0.0)

    fun init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(::renderWaypoints)
    }

    private const val COLOR = 0x00FFFF
    private const val ALPHA = 0.1f

    private fun renderWaypoints(context: WorldRenderContext) {
        Renderer.renderBlock(
            context,
            WAYPOINT_POS,
            COLOR,
            ALPHA,
            throughWalls = true,
            useBlockShape = true
        )
    }
}
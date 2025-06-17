package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.waypoints.Renderer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.util.math.Vec3d

object ColorUtils {
    /**
     * Converts a hexadecimal integer color code to RGB float values (0-255 range)
     * @param hexColor The hexadecimal color as an integer (e.g., 0x8000FF for purple)
     * @return Triple containing (red, green, blue) as Float values in 0-255 range
     */
    fun hexToRgb(hexColor: Int): Triple<Float, Float, Float> {
        val red = ((hexColor shr 16) and 0xFF).toFloat()
        val green = ((hexColor shr 8) and 0xFF).toFloat()
        val blue = (hexColor and 0xFF).toFloat()
        return Triple(red, green, blue)
    }

    /**
     * Extension function for Renderer.renderBlock that accepts hexadecimal color
     */
    fun Renderer.renderBlock(
        context: WorldRenderContext,
        pos: Vec3d,
        color: Int,
        alpha: Float,
        throughWalls: Boolean = true,
        useBlockShape: Boolean = false
    ) {
        val (red, green, blue) = hexToRgb(color)
        this.renderBlock(context, pos, red, green, blue, alpha, throughWalls, useBlockShape)
    }

    /**
     * Extension function for Renderer.renderArea that accepts hexadecimal color
     */
    fun Renderer.renderArea(
        context: WorldRenderContext,
        startPos: Vec3d,
        endPos: Vec3d,
        color: Int,
        alpha: Float,
        throughWalls: Boolean = true
    ) {
        val (red, green, blue) = hexToRgb(color)
        this.renderArea(context, startPos, endPos, red, green, blue, alpha, throughWalls)
    }
}
package io.github.frostzie.skyfall.utils

import com.google.gson.JsonObject
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

    fun getColorCode(color: String?): String {
        return when (color) {
            "dark_red" -> "§4"
            "red" -> "§c"
            "gold" -> "§6"
            "yellow" -> "§e"
            "dark_green" -> "§2"
            "green" -> "§a"
            "aqua" -> "§b"
            "dark_aqua" -> "§3"
            "dark_blue" -> "§1"
            "blue" -> "§9"
            "light_purple" -> "§d"
            "dark_purple" -> "§5"
            "white" -> "§f"
            "gray" -> "§7"
            "dark_gray" -> "§8"
            "black" -> "§0"
            else -> ""
        }
    }

    fun formatCodes(extraObj: JsonObject): String {
        val sb = StringBuilder()
        if (extraObj.get("bold")?.asBoolean == true) sb.append("§l")
        if (extraObj.get("italic")?.asBoolean == true) sb.append("§o")
        if (extraObj.get("underlined")?.asBoolean == true) sb.append("§n")
        if (extraObj.get("strikethrough")?.asBoolean == true) sb.append("§m")
        if (extraObj.get("obfuscated")?.asBoolean == true) sb.append("§k")
        return sb.toString()
    }

    /**
     * Strips Minecraft formatting codes from a given text.
     * @param text The input text containing color codes.
     * @return The text with all color codes removed.
     */
    fun stripColorCodes(text: String): String {
        return text.replace("§[0-9a-fk-or]".toRegex(), "").trim()
    }
}
package io.github.frostzie.skyfall.utils

import com.google.gson.JsonObject
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.awt.Color

object ColorUtils {
    private val logger = LoggerProvider.getLogger("ColorUtils")

    fun skyFallPrefixChat(): MutableText {
        val text = "SkyFall"
        val colors = listOf(
            0x00D0DD, // S
            0x00BFD8, // k
            0x00AED2, // y
            0x009DCD, // F
            0x008CC7, // a
            0x007BC2, // l
            0x006ABC  // l
        )
        val result = Text.empty()
        for (i in text.indices) {
            result.append(
                Text.literal(text[i].toString())
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colors[i])))
            )
        }
        return result
    }

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

    /**
     * Parses a color string from the config to an ARGB integer.
     * The format is assumed to be "chroma:alpha:red:green:blue".
     */
    fun parseColorString(colorString: String): Int {
        try {
            val parts = colorString.split(":")
            if (parts.size < 5) return 0xFFFFFFFF.toInt()

            val chroma = parts[0].toInt()
            val a = parts[1].toInt().coerceIn(0, 255)
            val r = parts[2].toInt().coerceIn(0, 255)
            val g = parts[3].toInt().coerceIn(0, 255)
            val b = parts[4].toInt().coerceIn(0, 255)

            return if (chroma != 0) {
                val invertedChroma = (256 - chroma).coerceIn(1, 255)

                val periodInMillis = (invertedChroma / 255.0) * 60000.0
                if (periodInMillis <= 0) {
                    return (a shl 24) or (r shl 16) or (g shl 8) or b
                }

                val hue = (System.currentTimeMillis() % periodInMillis.toLong()) / periodInMillis.toFloat()

                val rainbowRgb = Color.HSBtoRGB(hue, 1.0f, 1.0f)
                (a shl 24) or (rainbowRgb and 0x00FFFFFF)
            } else {
                (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        } catch (e: NumberFormatException) {
            logger.error("Failed to parse color string: $colorString", e)
            ChatUtils.error("Failed to parse color string: $colorString, report in the discord!")
            return 0xFFFFFFFF.toInt()
        }
    }
}
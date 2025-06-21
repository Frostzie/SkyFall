package io.github.frostzie.skyfall.utils.item

import com.google.gson.JsonArray
import com.mojang.serialization.JsonOps
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack

object TooltipUtils {
    private val logger = LoggerProvider.getLogger("TooltipUtils")

    /**
     * Extracts lore from an ItemStack as a list of formatted strings
     * @param itemStack The ItemStack to extract lore from
     * @return List of lore lines as strings with color codes, empty list if no lore
     */
    fun getLoreAsStrings(itemStack: ItemStack): List<String> {
        if (itemStack.isEmpty) {
            return emptyList()
        }

        return try {
            val rawLore = extractRawLore(itemStack)
            formatLoreToStrings(rawLore)
        } catch (e: Exception) {
            logger.error("Error extracting lore from ItemStack: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Extracts lore from an ItemStack as a list of clean strings (no color codes)
     * @param itemStack The ItemStack to extract lore from
     * @return List of lore lines as clean strings, empty list if no lore
     */
    fun getCleanLoreAsStrings(itemStack: ItemStack): List<String> {
        return getLoreAsStrings(itemStack).map { ColorUtils.stripColorCodes(it) }
    }

    /**
     * Gets a specific lore line by index
     * @param itemStack The ItemStack to extract lore from
     * @param index The index of the lore line (0-based)
     * @param clean Whether to strip color codes (default: false)
     * @return The lore line at the specified index, or null if not found
     */
    fun getLoreLine(itemStack: ItemStack, index: Int, clean: Boolean = false): String? {
        val lore = if (clean) getCleanLoreAsStrings(itemStack) else getLoreAsStrings(itemStack)
        return lore.getOrNull(index)
    }

    /**
     * Searches for lore lines containing specific text
     * @param itemStack The ItemStack to search in
     * @param searchText The text to search for
     * @param ignoreCase Whether to ignore case (default: true)
     * @param clean Whether to strip color codes before searching (default: true)
     * @return List of matching lore lines
     */
    fun findLoreContaining(
        itemStack: ItemStack,
        searchText: String,
        ignoreCase: Boolean = true,
        clean: Boolean = true
    ): List<String> {
        val lore = if (clean) getCleanLoreAsStrings(itemStack) else getLoreAsStrings(itemStack)
        return lore.filter { line ->
            line.contains(searchText, ignoreCase = ignoreCase)
        }
    }

    /**
     * Gets the first lore line that matches a regex pattern
     * @param itemStack The ItemStack to search in
     * @param pattern The regex pattern to match
     * @param clean Whether to strip color codes before matching (default: true)
     * @return The first matching lore line, or null if not found
     */
    fun findFirstLoreMatching(itemStack: ItemStack, pattern: Regex, clean: Boolean = true): String? {
        val lore = if (clean) getCleanLoreAsStrings(itemStack) else getLoreAsStrings(itemStack)
        return lore.firstOrNull { line ->
            pattern.containsMatchIn(line)
        }
    }

    /**
     * Gets all lore lines that match a regex pattern
     * @param itemStack The ItemStack to search in
     * @param pattern The regex pattern to match
     * @param clean Whether to strip color codes before matching (default: true)
     * @return List of matching lore lines
     */
    fun findAllLoreMatching(itemStack: ItemStack, pattern: Regex, clean: Boolean = true): List<String> {
        val lore = if (clean) getCleanLoreAsStrings(itemStack) else getLoreAsStrings(itemStack)
        return lore.filter { line ->
            pattern.containsMatchIn(line)
        }
    }

    /**
     * Extracts a value from lore using a regex with capture groups
     * @param itemStack The ItemStack to extract from
     * @param pattern The regex pattern with capture groups
     * @param groupIndex The capture group index to extract (default: 1)
     * @param clean Whether to strip color codes before matching (default: true)
     * @return The extracted value, or null if not found
     */
    fun extractFromLore(
        itemStack: ItemStack,
        pattern: Regex,
        groupIndex: Int = 1,
        clean: Boolean = true
    ): String? {
        val lore = if (clean) getCleanLoreAsStrings(itemStack) else getLoreAsStrings(itemStack)
        for (line in lore) {
            val match = pattern.find(line)
            if (match != null && match.groupValues.size > groupIndex) {
                return match.groupValues[groupIndex]
            }
        }
        return null
    }

    /**
     * Gets the total number of lore lines
     * @param itemStack The ItemStack to count lore lines for
     * @return The number of lore lines
     */
    fun getLoreLineCount(itemStack: ItemStack): Int {
        return getLoreAsStrings(itemStack).size
    }

    /**
     * Checks if the item has any lore
     * @param itemStack The ItemStack to check
     * @return True if the item has lore, false otherwise
     */
    fun hasLore(itemStack: ItemStack): Boolean {
        return getLoreLineCount(itemStack) > 0
    }

    /**
     * Gets lore as a single concatenated string
     * @param itemStack The ItemStack to extract lore from
     * @param separator The separator between lore lines (default: " ")
     * @param clean Whether to strip color codes (default: false)
     * @return All lore lines joined as a single string
     */
    fun getLoreAsText(itemStack: ItemStack, separator: String = " ", clean: Boolean = false): String {
        val lore = if (clean) getCleanLoreAsStrings(itemStack) else getLoreAsStrings(itemStack)
        return lore.joinToString(separator)
    }

    private fun extractRawLore(itemStack: ItemStack): JsonArray? {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return null
        val registryManager = player.registryManager ?: return null

        val jsonElement = ItemStack.CODEC.encodeStart(
            registryManager.getOps(JsonOps.INSTANCE),
            itemStack
        ).getOrThrow { error ->
            RuntimeException("Failed to encode ItemStack: $error")
        }

        val jsonObject = jsonElement.asJsonObject
        val components = jsonObject.getAsJsonObject("components") ?: return null
        return components.getAsJsonArray("minecraft:lore")
    }

    private fun formatLoreToStrings(loreArray: JsonArray?): List<String> {
        if (loreArray == null) return emptyList()

        val result = mutableListOf<String>()

        try {
            loreArray.forEach { element ->
                if (element?.isJsonObject == true) {
                    val obj = element.asJsonObject
                    val extra = obj.getAsJsonArray("extra")

                    if (extra != null) {
                        val lineText = buildFormattedLine(extra)
                        if (lineText.isNotEmpty()) {
                            result.add(lineText)
                        }
                    } else {
                        val text = obj.get("text")?.asString ?: ""
                        if (text.isNotEmpty()) {
                            result.add(text)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing lore array: ${e.message}", e)
        }

        return result
    }

    private fun buildFormattedLine(extra: JsonArray): String {
        val lineText = StringBuilder()

        extra.forEach { extraElement ->
            try {
                when {
                    extraElement?.isJsonObject == true -> {
                        val extraObj = extraElement.asJsonObject
                        val text = extraObj.get("text")?.asString ?: ""
                        val color = extraObj.get("color")?.asString
                        val colorCode = ColorUtils.getColorCode(color)
                        val formatCodes = ColorUtils.formatCodes(extraObj)
                        lineText.append(colorCode).append(formatCodes).append(text)
                    }
                    extraElement?.isJsonPrimitive == true -> {
                        lineText.append(extraElement.asString)
                    }
                }
            } catch (e: Exception) {
                logger.warn("Error processing extra element: ${e.message}")
            }
        }
        return lineText.toString()
    }
}
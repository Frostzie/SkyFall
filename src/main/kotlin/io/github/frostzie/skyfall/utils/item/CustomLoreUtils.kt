package io.github.frostzie.skyfall.utils.item

import io.github.frostzie.skyfall.utils.ColorUtils
import net.minecraft.text.Text

object CustomLoreUtils {

    /**
     * Private helper function to get a mutable view of only the lore lines (excluding the item name).
     * Returns null if there are no lore lines (i.e., only the item name or an empty list).
     */
    private fun getLoreOnly(lines: MutableList<Text>): MutableList<Text>? {
        return if (lines.size <= 1) null else lines.subList(1, lines.size)
    }

    /**
     * Inserts a custom lore line after the first matching line in the tooltip.
     * Operates only on lore lines, skipping the item name.
     */
    fun insertAfterMatchingLine(
        lines: MutableList<Text>,
        match: String,
        newLore: Text,
        ignoreCase: Boolean = true
    ) {
        val loreOnly = getLoreOnly(lines) ?: return

        val indexInLore = loreOnly.indexOfFirst {
            ColorUtils.stripColorCodes(it.string).contains(match, ignoreCase)
        }

        if (indexInLore != -1 && loreOnly.none { it.string == newLore.string }) {
            loreOnly.add(indexInLore + 1, newLore)
        }
    }

    /**
     * Inserts a custom lore line at the top of the tooltip, right after the item name.
     * This function explicitly deals with the boundary between the item name and lore.
     */
    fun insertAtTop(lines: MutableList<Text>, lore: Text) {
        if (lines.none { it.string == lore.string }) {
            lines.add(1, lore)
        }
    }

    /**
     * Replaces all lore lines that match the given text with a new lore line.
     * Operates only on lore lines, skipping the item name.
     */
    fun overrideMatchingLore(
        lines: MutableList<Text>,
        matchText: String,
        newLore: Text,
        ignoreCase: Boolean = true
    ) {
        val loreOnly = getLoreOnly(lines) ?: return

        loreOnly.replaceAll {
            if (ColorUtils.stripColorCodes(it.string).contains(matchText, ignoreCase)) newLore else it
        }
    }

    /**
     * Removes all lore lines that match the given text (case insensitive by default).
     * Operates only on lore lines, skipping the item name.
     */
    fun removeLoreMatchingText(
        lines: MutableList<Text>,
        matchText: String,
        ignoreCase: Boolean = true
    ) {
        getLoreOnly(lines)?.removeIf { ColorUtils.stripColorCodes(it.string).contains(matchText, ignoreCase) }
    }

    /**
     * Removes an exact lore line.
     * Operates only on lore lines, skipping the item name.
     */
    fun removeExactLoreLine(
        lines: MutableList<Text>,
        targetLore: Text
    ) {
        getLoreOnly(lines)?.removeIf { it.string == targetLore.string }
    }

    /**
     * Removes lore lines from a list based on a custom predicate.
     * Operates only on lore lines, skipping the item name.
     */
    fun removeLoreIf(
        lines: MutableList<Text>,
        predicate: (Text) -> Boolean
    ) {
        getLoreOnly(lines)?.removeIf(predicate)
    }

    /**
     * Removes a range of lines between two matching patterns (exclusive).
     * Operates only on lore lines, skipping the item name.
     */
    fun removeLinesBetween(
        lines: MutableList<Text>,
        startMatch: String,
        endMatch: String,
        ignoreCase: Boolean = true
    ): Boolean {
        val loreOnly = getLoreOnly(lines) ?: return false

        val startIndex = loreOnly.indexOfFirst { ColorUtils.stripColorCodes(it.string).contains(startMatch, ignoreCase) }
        val endIndex = loreOnly.indexOfFirst { ColorUtils.stripColorCodes(it.string).contains(endMatch, ignoreCase) }

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            if (startIndex + 1 < endIndex) {
                loreOnly.subList(startIndex + 1, endIndex).clear()
            }
            return true
        }
        return false
    }

    /**
     * Replaces a source line with multiple new lines.
     * Operates only on lore lines, skipping the item name.
     */
    fun replaceSourceLine(
        lines: MutableList<Text>,
        sourceMatch: String,
        newLines: List<Text>,
        ignoreCase: Boolean = true
    ): Boolean {
        val loreOnly = getLoreOnly(lines) ?: return false

        val sourceIndex = loreOnly.indexOfFirst {
            ColorUtils.stripColorCodes(it.string).contains(sourceMatch, ignoreCase)
        }

        if (sourceIndex != -1) {
            loreOnly.removeAt(sourceIndex)
            loreOnly.addAll(sourceIndex, newLines)
            return true
        }
        return false
    }

    /**
     * Checks if a line already exists in the tooltip to prevent duplicates.
     * Operates only on lore lines, skipping the item name.
     */
    fun lineExists(lines: List<Text>, targetLine: Text): Boolean {
        if (lines.size <= 1) return false
        return lines.subList(1, lines.size).any { it.string == targetLine.string }
    }

    /**
     * Checks if a line matching the pattern exists in the tooltip.
     * Operates only on lore lines, skipping the item name.
     */
    fun lineMatchingExists(lines: List<Text>, pattern: String, ignoreCase: Boolean = true): Boolean {
        if (lines.size <= 1) return false
        return lines.subList(1, lines.size).any {
            ColorUtils.stripColorCodes(it.string).contains(pattern, ignoreCase)
        }
    }
}
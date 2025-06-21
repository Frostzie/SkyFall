package io.github.frostzie.skyfall.utils.item

import io.github.frostzie.skyfall.utils.ColorUtils
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object CustomLoreUtils {

    /**
     * Inserts a custom lore line after the first matching line in the tooltip.
     *
     * @param stack The ItemStack being analyzed.
     * @param lines The mutable list of tooltip lines.
     * @param match The string to match in existing lore lines.
     * @param newLore The new lore Text to insert.
     * @param ignoreCase Whether to ignore case when matching the string (default: true).
     */
    fun insertAfterMatchingLine(
        stack: ItemStack,
        lines: MutableList<Text>,
        match: String,
        newLore: Text,
        ignoreCase: Boolean = true
    ) {
        val cleanLoreLines = TooltipUtils.getCleanLoreAsStrings(stack)
        val index = cleanLoreLines.indexOfFirst { it.contains(match, ignoreCase) }

        if (index != -1) {
            val insertAt = lines.indexOfFirst {
                ColorUtils.stripColorCodes(it.string).contains(match, ignoreCase)
            }
            if (insertAt != -1 && !lines.any { it.string == newLore.string }) {
                lines.add(insertAt + 1, newLore)
            }
        }
    }

    /**
     * Inserts a custom lore line at the top of the tooltip, right after the item name.
     *
     * @param lines The mutable list of tooltip lines to modify.
     * @param lore The Text object representing the lore to insert.
     */
    fun insertAtTop(lines: MutableList<Text>, lore: Text) {
        if (lines.none { it.string == lore.string }) {
            lines.add(1, lore) // Right after item name
        }
    }

    /**
     * Inserts a custom lore line immediately after the first matching lore line based on string content.
     *
     * @param stack The ItemStack being analyzed.
     * @param lines The mutable list of tooltip lines.
     * @param matchText The string content to match in existing lore.
     * @param newLore The new lore Text to insert.
     * @param ignoreCase Whether to ignore case when matching the string (default: true).
     */
    fun insertAfterMatchingString(
        stack: ItemStack,
        lines: MutableList<Text>,
        matchText: String,
        newLore: Text,
        ignoreCase: Boolean = true
    ) {
        val loreLines = TooltipUtils.getCleanLoreAsStrings(stack)
        val matchIndex = loreLines.indexOfFirst { it.contains(matchText, ignoreCase = ignoreCase) }

        if (matchIndex != -1) {
            val actualIndex = lines.indexOfFirst {
                it.string.contains(matchText, ignoreCase = ignoreCase)
            }

            if (actualIndex != -1 && lines.none { it.string == newLore.string }) {
                lines.add(actualIndex + 1, newLore)
            }
        }
    }

    /**
     * Replaces all lore lines that match the given text with a new lore line.
     */
    fun overrideMatchingLore(
        lines: MutableList<Text>,
        matchText: String,
        newLore: Text,
        ignoreCase: Boolean = true
    ) {
        val iterator = lines.listIterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (current.string.contains(matchText, ignoreCase = ignoreCase)) {
                iterator.set(newLore)
            }
        }
    }

    /**
     * Removes all lore lines that match the given text (case insensitive by default).
     */
    fun removeLoreMatchingText(
        lines: MutableList<Text>,
        matchText: String,
        ignoreCase: Boolean = true
    ) {
        lines.removeIf { it.string.contains(matchText, ignoreCase = ignoreCase) }
    }

    /**
     * Removes an exact lore line.
     */
    fun removeExactLoreLine(
        lines: MutableList<Text>,
        targetLore: Text
    ) {
        lines.removeIf { it.string == targetLore.string }
    }

    /**
     * Removes a range of lines between two matching patterns (exclusive)
     * Useful for replacing sections of tooltip content
     */
    fun removeLinesBetween(
        lines: MutableList<Text>,
        startMatch: String,
        endMatch: String,
        ignoreCase: Boolean = true
    ): Boolean {
        val startIndex = lines.indexOfFirst {
            ColorUtils.stripColorCodes(it.string).contains(startMatch, ignoreCase)
        }
        val endIndex = lines.indexOfFirst {
            ColorUtils.stripColorCodes(it.string).contains(endMatch, ignoreCase)
        }

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            val linesToRemove = mutableListOf<Int>()
            for (i in (startIndex + 1) until endIndex) {
                linesToRemove.add(i)
            }

            linesToRemove.reversed().forEach { index ->
                lines.removeAt(index)
            }
            return true
        }
        return false
    }

    /**
     * Replaces a source line with multiple new lines
     * Useful for replacing single line with multiple obtain sources
     */
    fun replaceSourceLine(
        lines: MutableList<Text>,
        sourceMatch: String,
        newLines: List<Text>,
        ignoreCase: Boolean = true
    ): Boolean {
        val sourceIndex = lines.indexOfFirst {
            it.string.contains(sourceMatch, ignoreCase = ignoreCase)
        }

        if (sourceIndex != -1) {
            lines.removeAt(sourceIndex)
            newLines.forEachIndexed { index, newLine ->
                lines.add(sourceIndex + index, newLine)
            }
            return true
        }
        return false
    }

    /**
     * Checks if a line already exists in the tooltip to prevent duplicates
     */
    fun lineExists(lines: List<Text>, targetLine: Text): Boolean {
        return lines.any { it.string == targetLine.string }
    }

    /**
     * Checks if a line matching the pattern exists in the tooltip
     */
    fun lineMatchingExists(lines: List<Text>, pattern: String, ignoreCase: Boolean = true): Boolean {
        return lines.any {
            ColorUtils.stripColorCodes(it.string).contains(pattern, ignoreCase)
        }
    }
}
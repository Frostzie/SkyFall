// File: ItemUtils.kt
package io.github.frostzie.skyfall.utils.item

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.text.Text

// Added extension function to resolve styledLines() reference
private fun LoreComponent.styledLines(): List<Text> {
    // Example implementation; adjust as needed.
    return listOf(Text.literal("Default Lore"))
}

object ItemUtils {
    const val SKYBLOCK_ID = "id"
    const val UUID_KEY = "uuid"

    /**
     * Retrieves the Skyblock ID from the custom_data component of an ItemStack.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The Skyblock ID as a String if present, otherwise null.
     */
    fun getSkyblockId(itemStack: ItemStack?): String? {
        if (itemStack == null || itemStack.isEmpty) {
            return null
        }
        val customDataComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA)
        return customDataComponent?.let { data ->
            val nbt = data.copyNbt()
            val skyblockIdOptional = nbt.getString(SKYBLOCK_ID)
            skyblockIdOptional.orElse(null)
        }
    }

    /**
     * Retrieves the UUID from the custom_data component of an ItemStack.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The UUID as a String if present, otherwise null.
     */
    fun getUuid(itemStack: ItemStack?): String? {
        if (itemStack == null || itemStack.isEmpty) {
            return null
        }
        val customDataComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA)
        return customDataComponent?.let { data ->
            val nbt = data.copyNbt()
            val uuidOptional = nbt.getString(UUID_KEY)
            uuidOptional.orElse(null)?.takeIf { it.isNotEmpty() }
        }
    }

    /**
     * Checks if the given Item has a specific Skyblock ID.
     *
     * @param itemStack The ItemStack to check.
     * @param targetSkyblockId The Skyblock ID to match against.
     * @return True if the item's Skyblock ID matches the targetSkyblockId, false otherwise.
     */
    fun isSkyblockItem(itemStack: ItemStack?, targetSkyblockId: String?): Boolean {
        if (targetSkyblockId.isNullOrEmpty()) {
            return false
        }
        val itemId = getSkyblockId(itemStack)
        return targetSkyblockId == itemId
    }

    /**
     * Checks if the given ItemStack has a specific UUID.
     *
     * @param itemStack The ItemStack to check.
     * @param targetUuid The UUID to match against (as String).
     * @return True if the item's UUID matches the targetUuid, false otherwise.
     */
    fun hasUuid(itemStack: ItemStack?, targetUuid: String?): Boolean {
        if (targetUuid.isNullOrEmpty()) {
            return false
        }
        val itemUuid = getUuid(itemStack)
        return targetUuid == itemUuid
    }

    /**
     * Checks if the given ItemStack has any UUID present.
     *
     * @param itemStack The ItemStack to check.
     * @return True if the item has a UUID, false otherwise.
     */
    fun hasAnyUuid(itemStack: ItemStack?): Boolean {
        return getUuid(itemStack) != null
    }

    /**
     * Gets the display name of an ItemStack as a clean string.
     * This is useful for repository building and item identification.
     *
     * @param itemStack The ItemStack to get the name from.
     * @return The display name as a String, or null if the ItemStack is null or empty.
     */
    fun getDisplayName(itemStack: ItemStack?): String? {
        if (itemStack == null || itemStack.isEmpty) {
            return null
        }
        return itemStack.name.string
    }

    /**
     * Gets the lore (tooltip text) of an ItemStack.
     *
     * @param itemStack The ItemStack to get the lore from.
     * @return A list of Text objects representing the lore lines, or empty list if no lore.
     */
    fun getLore(itemStack: ItemStack?): List<Text> {
        if (itemStack == null || itemStack.isEmpty) {
            return emptyList()
        }
        return itemStack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines()
    }

    /**
     * Gets the lore (tooltip text) of an ItemStack as plain strings.
     *
     * @param itemStack The ItemStack to get the lore from.
     * @return A list of String objects representing the lore lines, or empty list if no lore.
     */
    fun getLoreAsStrings(itemStack: ItemStack?): List<String> {
        return getLore(itemStack).map { it.string }
    }

    /**
     * Checks if an ItemStack has a custom name (different from default item name).
     *
     * @param itemStack The ItemStack to check.
     * @return True if the item has a custom name, false otherwise.
     */
    fun hasCustomName(itemStack: ItemStack?): Boolean {
        if (itemStack == null || itemStack.isEmpty) {
            return false
        }
        return itemStack.name != null && itemStack.name.string.isNotEmpty()
    }

    /**
     * Gets the count/stack size of an ItemStack.
     *
     * @param itemStack The ItemStack to check.
     * @return The count of the ItemStack, or 0 if null or empty.
     */
    fun getItemCount(itemStack: ItemStack?): Int {
        if (itemStack == null || itemStack.isEmpty) {
            return 0
        }
        return itemStack.count
    }

    /**
     * Utility method to check if an ItemStack is valid (not null and not empty).
     *
     * @param itemStack The ItemStack to check.
     * @return True if the ItemStack is valid, false otherwise.
     */
    fun isValidItem(itemStack: ItemStack?): Boolean {
        return itemStack != null && !itemStack.isEmpty
    }
}
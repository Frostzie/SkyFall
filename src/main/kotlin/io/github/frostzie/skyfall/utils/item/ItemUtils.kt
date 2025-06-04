package io.github.frostzie.skyfall.utils.item

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.component.DataComponentTypes

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
}
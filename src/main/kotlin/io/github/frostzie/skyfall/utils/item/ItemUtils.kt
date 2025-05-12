package io.github.frostzie.skyfall.utils.item

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.component.DataComponentTypes

object ItemUtils {
    const val SKYBLOCK_ID = "id"

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
            if (nbt.contains(SKYBLOCK_ID, NbtCompound.STRING_TYPE.toInt())) {
                nbt.getString(SKYBLOCK_ID)
            } else {
                null
            }
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
}
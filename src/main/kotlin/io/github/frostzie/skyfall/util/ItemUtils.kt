package io.github.frostzie.skyfall.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

object ItemUtils {

    fun customDataTag(stack: ItemStack): CompoundTag =
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()

    fun compareUUID(old: ItemStack, new: ItemStack) : Boolean {
        return old.get(DataComponents.CUSTOM_DATA)?.copyTag()?.get("uuid") == new.get(DataComponents.CUSTOM_DATA)?.copyTag()?.get("uuid")
    }
}
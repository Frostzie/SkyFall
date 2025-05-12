package io.github.frostzie.skyfall.utils.item

import net.minecraft.component.ComponentType
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/**
 * Utility object for detecting item types and their components in ItemStacks.
 * Designed for Fabric Minecraft 1.21.5+ using Kotlin.
 */
object SkyBlockItemData {

    /**
     * Checks if the given ItemStack is of the specified Item type.
     *
     * @param stack The ItemStack to check.
     * @param item The Item to compare against.
     * @return True if the ItemStack's item matches the specified Item, false otherwise.
     */
    fun isItem(stack: ItemStack?, item: Item): Boolean {
        return stack != null && !stack.isEmpty && stack.item == item
    }

    /**
     * Checks if the given ItemStack is of the specified Item type, identified by its Identifier string.
     * Useful when you have the item ID string (e.g., "minecraft:stone").
     *
     * @param stack The ItemStack to check.
     * @param itemId The string identifier of the item (e.g., "minecraft:red_stained_glass").
     * @return True if the ItemStack's item matches the item corresponding to the given ID, false otherwise or if the ID is invalid.
     */
    fun isItem(stack: ItemStack?, itemId: String): Boolean {
        if (stack == null || stack.isEmpty) {
            return false
        }
        val identifier = Identifier.tryParse(itemId)
        if (identifier == null) {
            return false
        }
        val item = Registries.ITEM.get(identifier)
        return stack.item == item
    }

    /**
     * Checks if the given ItemStack has the specified component attached to it.
     *
     * @param stack The ItemStack to check.
     * @param componentType The ComponentType to look for.
     * @return True if the ItemStack contains the specified component, false otherwise.
     */
    fun hasComponent(stack: ItemStack?, componentType: ComponentType<*>): Boolean {
        return stack != null && !stack.isEmpty && stack.contains(componentType)
    }

    /**
     * Retrieves the value of the specified component from the ItemStack.
     * Returns null if the component is not present or the stack is null/empty.
     *
     * @param T The type of the component's value.
     * @param stack The ItemStack to check.
     * @param componentType The ComponentType whose value is to be retrieved.
     * @return The value of the component if present, null otherwise.
     */
    fun <T> getComponentValue(stack: ItemStack?, componentType: ComponentType<T>): T? {
        if (stack == null || stack.isEmpty) {
            return null
        }
        return stack.get(componentType)
    }

    /**
     * Checks if the given ItemStack has the specified component and if its value matches the expected value.
     *
     * @param T The type of the component's value.
     * @param stack The ItemStack to check.
     * @param componentType The ComponentType to look for.
     * @param expectedValue The value to compare the component's value against.
     * @return True if the component exists and its value equals the expected value, false otherwise.
     */
    fun <T> checkComponentValue(stack: ItemStack?, componentType: ComponentType<NbtComponent?>?, expectedValue: T): Boolean {
        if (stack == null || stack.isEmpty) {
            return false
        }
        val actualValue = stack.get(componentType)
        return actualValue != null && actualValue == expectedValue
    }

    /**
     * Checks if the given ItemStack has a component of a specific type (e.g., a custom ID component)
     * and if its value (assuming it's a String) matches the expected string value.
     *
     * Example Usage: Checking for a component `custom_data:id` with value "special_sword"
     * Assuming `ModComponents.CUSTOM_ID` is your `ComponentType<String>`:
     * ItemDetection.checkComponentValue(stack, ModComponents.CUSTOM_ID, "special_sword")
     *
     * @param stack The ItemStack to check.
     * @param componentType The ComponentType<String> to look for.
     * @param expectedValue The String value to compare against.
     * @return True if the component exists and its string value equals the expected value, false otherwise.
     */
    fun checkStringComponentValue(stack: ItemStack?, componentType: ComponentType<NbtComponent?>?, expectedValue: String): Boolean {
        return checkComponentValue(stack, componentType, expectedValue)
    }
}
package io.github.frostzie.skyfall.utils.item

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.component.DataComponentTypes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.double

object PetUtils {
    const val PET_ID = "PET"
    const val PET_INFO_KEY = "petInfo"

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Checks if the given ItemStack is a pet item.
     *
     * @param itemStack The ItemStack to check.
     * @return True if the item is a pet, false otherwise.
     */
    fun isPet(itemStack: ItemStack?): Boolean {
        return ItemUtils.isSkyblockItem(itemStack, PET_ID)
    }

    /**
     * Retrieves the petInfo JSON string from the custom_data component of an ItemStack.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The petInfo JSON string if present, otherwise null.
     */
    private fun getPetInfoString(itemStack: ItemStack?): String? {
        if (itemStack == null || itemStack.isEmpty || !isPet(itemStack)) {
            return null
        }
        val customDataComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA)
        return customDataComponent?.let { data ->
            val nbt = data.copyNbt()
            val petInfoOptional = nbt.getString(PET_INFO_KEY)
            petInfoOptional.orElse(null)?.takeIf { it.isNotEmpty() }
        }
    }

    /**
     * Parses the petInfo JSON string into a JsonObject.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The parsed JsonObject if successful, otherwise null.
     */
    private fun getPetInfoJson(itemStack: ItemStack?): JsonObject? {
        val petInfoString = getPetInfoString(itemStack) ?: return null
        return try {
            json.parseToJsonElement(petInfoString) as? JsonObject
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retrieves the pet type from the petInfo.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The pet type as a String if present, otherwise null.
     */
    fun getPetType(itemStack: ItemStack?): String? {
        val petInfo = getPetInfoJson(itemStack) ?: return null
        return petInfo["type"]?.jsonPrimitive?.content
    }

    /**
     * Retrieves the active status from the petInfo.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The active status as a Boolean if present, otherwise null.
     */
    fun getPetActive(itemStack: ItemStack?): Boolean? {
        val petInfo = getPetInfoJson(itemStack) ?: return null
        return try {
            petInfo["active"]?.jsonPrimitive?.boolean
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retrieves the experience from the petInfo.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The experience as an Int if present, otherwise null.
     */
    fun getPetExp(itemStack: ItemStack?): Int? {
        val petInfo = getPetInfoJson(itemStack) ?: return null
        return try {
            val expElement = petInfo["exp"]?.jsonPrimitive
            when {
                expElement?.content?.contains('.') == true -> expElement.double.toInt()
                else -> expElement?.int
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retrieves the tier from the petInfo.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The tier as a String if present, otherwise null.
     */
    fun getPetTier(itemStack: ItemStack?): String? {
        val petInfo = getPetInfoJson(itemStack) ?: return null
        return petInfo["tier"]?.jsonPrimitive?.content
    }

    /**
     * Retrieves the held item from the petInfo.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The held item as a String if present, otherwise null.
     */
    fun getPetHeldItem(itemStack: ItemStack?): String? {
        val petInfo = getPetInfoJson(itemStack) ?: return null
        return petInfo["heldItem"]?.jsonPrimitive?.content
    }

    /**
     * Retrieves the candy used count from the petInfo.
     *
     * @param itemStack The ItemStack to inspect.
     * @return The candy used count as an Int if present, otherwise null.
     */
    fun getPetCandyUsed(itemStack: ItemStack?): Int? {
        val petInfo = getPetInfoJson(itemStack) ?: return null
        return try {
            petInfo["candyUsed"]?.jsonPrimitive?.int
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if the given ItemStack is a pet of the specified type.
     *
     * @param itemStack The ItemStack to check.
     * @param targetType The pet type to match against.
     * @return True if the pet's type matches the targetType, false otherwise.
     */
    fun isPetType(itemStack: ItemStack?, targetType: String?): Boolean {
        if (targetType.isNullOrEmpty()) {
            return false
        }
        val petType = getPetType(itemStack)
        return targetType == petType
    }

    /**
     * Checks if the given ItemStack is an active pet.
     *
     * @param itemStack The ItemStack to check.
     * @return True if the pet is active, false otherwise.
     */
    fun isActivePet(itemStack: ItemStack?): Boolean {
        return getPetActive(itemStack) == true
    }

    /**
     * Checks if the given ItemStack is a pet of the specified tier.
     *
     * @param itemStack The ItemStack to check.
     * @param targetTier The tier to match against.
     * @return True if the pet's tier matches the targetTier, false otherwise.
     */
    fun isPetTier(itemStack: ItemStack?, targetTier: String?): Boolean {
        if (targetTier.isNullOrEmpty()) {
            return false
        }
        val petTier = getPetTier(itemStack)
        return targetTier == petTier
    }

    /**
     * Checks if the given ItemStack is a pet holding the specified item.
     *
     * @param itemStack The ItemStack to check.
     * @param targetHeldItem The held item to match against.
     * @return True if the pet's held item matches the targetHeldItem, false otherwise.
     */
    fun isPetHoldingItem(itemStack: ItemStack?, targetHeldItem: String?): Boolean {
        if (targetHeldItem.isNullOrEmpty()) {
            return false
        }
        val heldItem = getPetHeldItem(itemStack)
        return targetHeldItem == heldItem
    }

    /**
     * Checks if the given ItemStack is a pet that has used any candy.
     *
     * @param itemStack The ItemStack to check.
     * @return True if the pet has used candy, false otherwise.
     */
    fun hasPetUsedCandy(itemStack: ItemStack?): Boolean {
        val candyUsed = getPetCandyUsed(itemStack) ?: return false
        return candyUsed > 0
    }
}
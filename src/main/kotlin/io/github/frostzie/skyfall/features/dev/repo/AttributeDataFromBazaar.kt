package io.github.frostzie.skyfall.features.dev.repo

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import com.mojang.serialization.JsonOps
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.SoundUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object AttributeDataFromBazaar {
    private val logger = LoggerProvider.getLogger("AttributeDataFromBazaar")
    private val repoFile = File("config/skyfall/AttributeMenuData.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private const val TARGET_SLOT = 13

    private var lastProcessedScreen: HandledScreen<*>? = null
    private var processingDelayTicks = 0
    private const val PROCESSING_DELAY = 20

    fun init() {
        registerTickHandler()
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!SkyFall.feature.dev.repo.attributeDataFromBazaar) {
                return@register
            }
            val screen = client.currentScreen
            if (screen is HandledScreen<*> && isShardsChest(screen)) {
                handleShardsChestScreen(screen)
            } else {
                lastProcessedScreen = null
                processingDelayTicks = 0
            }
        }
    }

    private fun isShardsChest(screen: HandledScreen<*>): Boolean {
        val title = screen.title.string
        val containsShards = title.contains("Shards", ignoreCase = true)
        val containsOddities = title.contains("Oddities", ignoreCase = true)
        return containsShards && !containsOddities
    }

    private fun handleShardsChestScreen(screen: HandledScreen<*>) {
        if (lastProcessedScreen != screen) {
            lastProcessedScreen = screen
            processingDelayTicks = 0
            logger.info("Detected shards chest for data extraction, starting delay...")
            return
        }
        processingDelayTicks++
        if (processingDelayTicks >= PROCESSING_DELAY) {
            processShardsChest(screen)
            processingDelayTicks = 0
        }
    }

    private fun processShardsChest(screen: HandledScreen<*>) {
        logger.info("Processing shards chest for data extraction...")
        val existingRepo = loadExistingRepository()
        if (existingRepo.isEmpty()) {
            ChatUtils.messageToChat("§cNo attribute repository found! Run the main repo builder first.").send()
            logger.warn("No existing attribute repository found")
            return
        }

        val targetSlot = getSlotByIndex(screen, TARGET_SLOT)
        if (targetSlot == null) {
            ChatUtils.messageToChat("§cTarget slot (13) not found in shards chest!").send()
            logger.warn("Missing target slot")
            return
        }

        if (targetSlot.stack.isEmpty) {
            ChatUtils.messageToChat("§cTarget slot is empty!").send()
            logger.warn("Target slot is empty")
            return
        }

        val shardData = extractShardData(targetSlot)
        if (shardData != null) {
            updateAttributeRepository(shardData, existingRepo)
        } else {
            ChatUtils.messageToChat("§cFailed to extract shard data!").send()
            logger.warn("Shard data extraction failed")
        }
    }

    private fun extractShardData(slot: Slot): ShardData? {
        val itemDisplayName = slot.stack.name.string
        val lore = getLoreFromItemStack(slot.stack)

        val shardName = ColorUtils.stripColorCodes(itemDisplayName).trim()
        val attributeName = findAttributeNameInLore(lore)
        if (attributeName == null) {
            logger.warn("Could not find attribute name in lore for shard: $shardName")
            return null
        }

        val statBoost = extractStatBoostFromLore(lore, attributeName)
        if (statBoost == null) {
            logger.warn("Could not extract stat boost for attribute: $attributeName")
            return null
        }

        logger.debug("Extracted shard data: name=\"$shardName\", attribute=\"$attributeName\", statBoost=\"$statBoost\"")
        return ShardData(shardName, attributeName, statBoost)
    }

    private fun findAttributeNameInLore(lore: List<String>): String? {
        val attributeNames = loadExistingRepository().keys
        if (attributeNames.isEmpty()) return null

        for (line in lore) {
            val cleanLine = ColorUtils.stripColorCodes(line).trim()
            val foundAttribute = attributeNames.find { attributeName ->
                cleanLine.contains(attributeName, ignoreCase = true)
            }
            if (foundAttribute != null) {
                return foundAttribute
            }
        }
        return null
    }

    private fun extractStatBoostFromLore(lore: List<String>, attributeName: String): String? {
        val attributeLineIndex = lore.indexOfFirst {
            ColorUtils.stripColorCodes(it).trim().contains(attributeName, ignoreCase = true)
        }

        if (attributeLineIndex == -1) {
            logger.warn("Attribute name '$attributeName' not found in lore for stat boost extraction.")
            return null
        }

        val subsequentLines = lore.drop(attributeLineIndex + 1)
        val stopMarker = "§7You can Syphon this shard from"
        val statBoostLines = subsequentLines.takeWhile { it.trim() != stopMarker }

        return if (statBoostLines.isNotEmpty()) {
            statBoostLines.joinToString(" ") { it.trim() }
        } else {
            logger.warn("No stat boost lines found for attribute '$attributeName'. The section might be empty or missing.")
            null
        }
    }

    private fun updateAttributeRepository(shardData: ShardData, existingRepo: Map<String, JsonObject>) {
        val updatedRepo = existingRepo.toMutableMap()
        val attributeKey = shardData.attributeName

        if (!updatedRepo.containsKey(attributeKey)) {
            ChatUtils.messageToChat("§cAttribute \"$attributeKey\" not found in repository!").send()
            logger.warn("Attribute \"$attributeKey\" not in repository")
            return
        }

        val existingData = updatedRepo[attributeKey]!!

        existingData.addProperty("bazaarName", shardData.shardName)
        existingData.addProperty("statBoost", shardData.statBoost)

        saveRepository(updatedRepo)
        ChatUtils.messageToChat("§aUpdated shard data for \"$attributeKey\"").send()
        logger.info("Updated repository with shard data for attribute \"$attributeKey\"")
        SoundUtils.playSound(
            SoundEvents.BLOCK_BELL_USE,
            SoundCategory.BLOCKS
        )
    }

    private fun saveRepository(attributeData: Map<String, JsonObject>) {
        val propertyOrder = listOf("bazaarName", "type", "rarity", "maxShards", "maxStatBoost", "statBoost", "wayToObtain")
        val reorderedData = attributeData.mapValues { (_, originalJson) ->
            val reorderedJson = JsonObject()

            propertyOrder.forEach { key ->
                if (originalJson.has(key)) {
                    reorderedJson.add(key, originalJson.get(key))
                }
            }

            originalJson.keySet().forEach { key ->
                if (!propertyOrder.contains(key)) {
                    reorderedJson.add(key, originalJson.get(key))
                }
            }
            reorderedJson
        }

        try {
            repoFile.parentFile.mkdirs()
            FileWriter(repoFile).use { writer ->
                gson.toJson(reorderedData, writer)
            }
            logger.info("Successfully saved attribute repository with ${reorderedData.size} entries")
        } catch (e: Exception) {
            logger.error("Failed to save attribute repository: ${e.message}", e)
            ChatUtils.messageToChat("§cFailed to save attribute repository!").send()
        }
    }

    private fun loadExistingRepository(): Map<String, JsonObject> {
        if (!repoFile.exists()) {
            return emptyMap()
        }
        return try {
            FileReader(repoFile).use { reader ->
                val type = object : TypeToken<Map<String, JsonObject>>() {}.type
                gson.fromJson<Map<String, JsonObject>>(reader, type) ?: emptyMap()
            }
        } catch (e: Exception) {
            logger.error("Error loading repository: ${e.message}")
            emptyMap()
        }
    }

    private fun getSlotByIndex(screen: HandledScreen<*>, index: Int): Slot? {
        return try {
            screen.screenHandler.slots.getOrNull(index)
        } catch (e: Exception) {
            logger.error("Error getting slot at index $index: ${e.message}")
            null
        }
    }

    private fun getLoreFromItemStack(itemStack: ItemStack): List<String> {
        try {
            val client = MinecraftClient.getInstance()
            val player = client.player ?: return emptyList()
            val registryManager = player.registryManager ?: return emptyList()
            val jsonElement = ItemStack.CODEC.encodeStart(registryManager.getOps(JsonOps.INSTANCE), itemStack)
                .getOrThrow { error -> RuntimeException("Failed to encode ItemStack: $error") }
            val jsonObject = jsonElement.asJsonObject
            if (jsonObject.has("components")) {
                val components = jsonObject.getAsJsonObject("components")
                if (components.has("minecraft:lore")) {
                    val loreArray = components.getAsJsonArray("minecraft:lore")
                    val formattedLore = formatMinecraftLore(loreArray)
                    return formattedLore.map { it.asString }
                }
            }
        } catch (e: Exception) {
            logger.error("Error extracting lore from item: ${e.message}", e)
        }
        return emptyList()
    }

    private fun formatMinecraftLore(loreArray: JsonArray): List<JsonElement> {
        val result = mutableListOf<JsonElement>()
        try {
            loreArray.forEach { element ->
                if (element?.isJsonObject == true) {
                    val obj = element.asJsonObject
                    val extra = obj.getAsJsonArray("extra")
                    if (extra != null) {
                        val lineText = StringBuilder()
                        extra.forEach { extraElement ->
                            try {
                                if (extraElement?.isJsonObject == true) {
                                    val extraObj = extraElement.asJsonObject
                                    val text = extraObj.get("text")?.asString ?: ""
                                    val color = extraObj.get("color")?.asString ?: ""
                                    val colorCode = ColorUtils.getColorCode(color)
                                    val formatCodes = ColorUtils.formatCodes(extraObj)
                                    lineText.append(colorCode).append(formatCodes).append(text)
                                } else if (extraElement?.isJsonPrimitive == true) {
                                    lineText.append(extraElement.asString)
                                }
                            } catch (e: Exception) {
                                logger.warn("Error processing extra element: ${e.message}")
                            }
                        }
                        if (lineText.isNotEmpty()) {
                            result.add(JsonPrimitive(lineText.toString()))
                        }
                    } else {
                        val text = obj.get("text")?.asString ?: ""
                        if (text.isNotEmpty()) {
                            result.add(JsonPrimitive(text))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing lore array: ${e.message}", e)
            return loreArray.toList()
        }
        return result
    }

    private data class ShardData(
        val shardName: String,
        val attributeName: String,
        val statBoost: String
    )
}
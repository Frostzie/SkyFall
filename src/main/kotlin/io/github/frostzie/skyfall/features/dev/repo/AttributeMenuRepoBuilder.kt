package io.github.frostzie.skyfall.features.dev.repo

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import com.mojang.serialization.JsonOps
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import java.io.File
import java.io.FileReader
import java.io.FileWriter

@Feature(name = "Attribute Menu Repo Builder")
object AttributeMenuRepoBuilder : IFeature {
    override var isRunning = false
    private val logger = LoggerProvider.getLogger("AttributeMenuRepoBuilder")
    private val repoFile = File("config/skyfall/AttributeMenuData.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val validSlotRanges = setOf(
        10..16,
        19..25,
        28..34,
        37..43
    )

    private var lastProcessedTime = 0L
    private const val COOLDOWN_MS = 1000L

    init {
        registerTickHandler()
    }

    override fun shouldLoad(): Boolean {
        return SkyFall.feature.dev.repo.attributeMenuRepoBuilder
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!isRunning) {
                return@register
            }
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastProcessedTime < COOLDOWN_MS) {
                return@register
            }
            val screen = client.currentScreen
            if (screen is HandledScreen<*> && isAttributeMenu(screen)) {
                buildDefaultRepository(screen)
                lastProcessedTime = currentTime
            }
        }
    }

    private fun isAttributeMenu(screen: HandledScreen<*>): Boolean {
        return screen.title.string.equals("Attribute Menu", ignoreCase = true)
    }

    private fun buildDefaultRepository(screen: HandledScreen<*>) {
        val attributeData = mutableMapOf<String, JsonObject>()
        val existingData = loadExistingRepository().toMutableMap()
        var hasNewEntries = false
        var hasUpdatedEntries = false

        validSlotRanges.forEach { range ->
            range.forEach { slotIndex ->
                val slot = getSlotByIndex(screen, slotIndex)
                if (slot != null && isSlotInChestInventory(slot) && !slot.stack.isEmpty) {
                    val rawName = slot.stack.name.string
                    val attributeName = stripRomanNumerals(rawName)
                    if (attributeName.isNotEmpty()) {
                        val lore = getLoreFromItemStack(slot.stack)
                        val type = extractTypeFromLore(lore)
                        val rarity = extractRarityFromLore(lore)

                        if (!existingData.containsKey(attributeName)) {
                            attributeData[attributeName] = JsonObject().apply {
                                addProperty("bazaarName", "")
                                addProperty("type", type)
                                addProperty("rarity", rarity)
                                addProperty("maxShards", 0)
                                addProperty("maxStatBoost", "")
                                addProperty("statBoost", "")
                                add("wayToObtain", gson.toJsonTree(emptyList<String>()))
                            }
                            hasNewEntries = true
                            logger.debug("Found new attribute \"$attributeName\" in slot $slotIndex with type \"$type\" and rarity \"$rarity\"")
                        } else {
                            val existingEntry = existingData[attributeName]!!
                            var entryWasModified = false

                            fun updateStringIfEmpty(key: String, newValue: String) {
                                if (newValue.isNotEmpty() && (existingEntry.get(key)?.asString ?: "").isEmpty()) {
                                    existingEntry.addProperty(key, newValue)
                                    logger.debug("Updated '$key' for \"$attributeName\": \"$newValue\"")
                                    entryWasModified = true
                                }
                            }
                            fun addPropertyIfMissing(key: String, value: String) {
                                if (!existingEntry.has(key)) {
                                    existingEntry.addProperty(key, value)
                                    logger.debug("Added missing '$key' property for \"$attributeName\"")
                                    entryWasModified = true
                                }
                            }

                            updateStringIfEmpty("type", type)
                            updateStringIfEmpty("rarity", rarity)

                            addPropertyIfMissing("bazaarName", "")
                            addPropertyIfMissing("statBoost", "")

                            if (entryWasModified) {
                                hasUpdatedEntries = true
                            }
                        }
                    }
                }
            }
        }

        if (hasNewEntries || hasUpdatedEntries) {
            attributeData.forEach { (name, data) ->
                existingData[name] = data
            }
            saveRepository(existingData)

            val newCount = attributeData.size
            val totalCount = existingData.size

            if (hasNewEntries && hasUpdatedEntries) {
                ChatUtils.messageToChat("§aGenerated $newCount new entries and updated existing entries (Total: $totalCount)").send()
            } else if (hasNewEntries) {
                ChatUtils.messageToChat("§aGenerated repository with $newCount new attribute entries (Total: $totalCount)").send()
            } else {
                ChatUtils.messageToChat("§aUpdated existing attribute entries with new/missing info (Total: $totalCount)").send()
            }
        } else {
            logger.debug("No new attribute entries or updates found in the menu")
        }
    }

    private fun extractTypeFromLore(lore: List<String>): String {
        if (lore.isEmpty()) return ""

        val firstLine = ColorUtils.stripColorCodes(lore[0]).trim()
        return firstLine
    }

    private fun extractRarityFromLore(lore: List<String>): String {
        for (line in lore) {
            val cleanLine = ColorUtils.stripColorCodes(line).trim()
            if (cleanLine.startsWith("Rarity: ", ignoreCase = true)) {
                return cleanLine.substring("Rarity: ".length).trim()
            }
        }
        return ""
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

    private fun stripRomanNumerals(attributeName: String): String {
        val regex = Regex("^(.+?)\\s+([IVXLCDM]+)$", RegexOption.IGNORE_CASE)
        val match = regex.matchEntire(attributeName)
        return if (match != null) match.groupValues[1] else attributeName
    }

    private fun getSlotByIndex(screen: HandledScreen<*>, index: Int): Slot? {
        return try {
            screen.screenHandler.slots.getOrNull(index)
        } catch (e: Exception) {
            logger.error("Failed to get slot at index $index: ${e.message}")
            null
        }
    }

    private fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
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
            logger.error("Failed to save attribute repository: ${e.message}")
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
            logger.error("Failed to load existing repository: ${e.message}")
            emptyMap()
        }
    }
}
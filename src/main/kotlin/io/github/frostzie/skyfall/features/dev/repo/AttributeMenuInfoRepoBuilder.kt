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

object AttributeMenuInfoRepoBuilder {
    private val logger = LoggerProvider.getLogger("AttributeMenuInfoRepoBuilder")
    private val repoFile = File("config/skyfall/AttributeMenuData.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private const val MAX_LEVEL_SLOT = 20
    private const val HOW_TO_HUNT_SLOT = 22

    private var lastProcessedScreen: HandledScreen<*>? = null
    private var processingDelayTicks = 0
    private const val PROCESSING_DELAY = 20

    fun init() {
        registerTickHandler()
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!SkyFall.feature.dev.repo.attributeMenuInfoRepoBuilder) {
                return@register
            }
            val screen = client.currentScreen
            if (screen is HandledScreen<*> && isAttributeMenu(screen)) {
                handleAttributeMenuInfoScreen(screen)
            } else {
                lastProcessedScreen = null
                processingDelayTicks = 0
            }
        }
    }

    private fun isAttributeMenu(screen: HandledScreen<*>): Boolean {
        val title = screen.title.string
        val existingRepo = loadExistingRepository()
        return existingRepo.keys.any { attributeName ->
            title.contains(attributeName, ignoreCase = true)
        }
    }

    private fun handleAttributeMenuInfoScreen(screen: HandledScreen<*>) {
        if (lastProcessedScreen != screen) {
            lastProcessedScreen = screen
            processingDelayTicks = 0
            logger.info("Detected attribute menu for info extraction, starting delay...")
            return
        }
        processingDelayTicks++
        if (processingDelayTicks >= PROCESSING_DELAY) {
            processAttributeMenuInfo(screen)
            processingDelayTicks = 0
        }
    }

    private fun processAttributeMenuInfo(screen: HandledScreen<*>) {
        logger.info("Processing attribute menu for detailed info extraction...")
        val existingRepo = loadExistingRepository()
        if (existingRepo.isEmpty()) {
            ChatUtils.messageToChat("§cNo attribute repository found! Run the main repo builder first.").send()
            logger.warn("No existing attribute repository found")
            return
        }
        val maxLevelSlot = getSlotByIndex(screen, MAX_LEVEL_SLOT)
        val howToHuntSlot = getSlotByIndex(screen, HOW_TO_HUNT_SLOT)
        if (maxLevelSlot == null || howToHuntSlot == null) {
            ChatUtils.messageToChat("§cRequired slots not found in attribute menu!").send()
            logger.warn("Missing max level or how-to-hunt slots")
            return
        }
        if (maxLevelSlot.stack.isEmpty || howToHuntSlot.stack.isEmpty) {
            ChatUtils.messageToChat("§cOne of the required slots is empty!").send()
            logger.warn("One or more required slots are empty")
            return
        }
        val attributeInfo = extractAttributeInfo(maxLevelSlot, howToHuntSlot)
        if (attributeInfo != null) {
            updateAttributeRepository(attributeInfo, existingRepo)
        } else {
            ChatUtils.messageToChat("§cFailed to extract attribute info!").send()
            logger.warn("Attribute info extraction failed")
        }
    }

    private fun extractAttributeInfo(maxLevelSlot: Slot, howToHuntSlot: Slot): AttributeInfo? {
        val maxLevelDisplayName = maxLevelSlot.stack.name.string
        val maxLevelLore = getLoreFromItemStack(maxLevelSlot.stack)
        val howToHuntLore = getLoreFromItemStack(howToHuntSlot.stack)
        val attributeName = extractAttributeNameFromTitle(maxLevelDisplayName) ?: return null
        val maxShards = extractMaxShards(maxLevelLore)
        val maxStatBoost = extractStatBoostName(maxLevelLore)
        val wayToObtain = extractWaysToObtain(howToHuntLore)
        logger.debug("Extracted info for \"$attributeName\": maxShards=$maxShards, statBoostName=\"$maxStatBoost\", ways=${wayToObtain.size}")
        return AttributeInfo(attributeName, maxShards, maxStatBoost, wayToObtain)
    }

    private fun extractAttributeNameFromTitle(displayName: String): String? {
        val cleanName = ColorUtils.stripColorCodes(displayName)
        val match = "Max Level - (.+)".toRegex().find(cleanName)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun extractMaxShards(lore: List<String>): Int {
        for (line in lore) {
            val cleanLine = ColorUtils.stripColorCodes(line)
            if (cleanLine.contains("Max level reached!", ignoreCase = true)) {
                return -1
            }
            val match = "\\(\\d+/([0-9]+)\\)".toRegex().find(cleanLine)
            if (match != null) {
                return match.groupValues[1].toIntOrNull() ?: 0
            }
        }
        return 0
    }

    private fun extractStatBoostName(lore: List<String>): String {
        val plusRegex = "\\+".toRegex()
        val shardRegex = "\\(\\d+/([0-9]+)\\)".toRegex()
        val stopIndicator = "Max level reached!"

        val cleanLore = lore.map { ColorUtils.stripColorCodes(it) }
        val firstPlusIndex = cleanLore.indexOfFirst { it.contains(plusRegex) }
        if (firstPlusIndex == -1) return ""

        var startIndex = firstPlusIndex
        for (i in firstPlusIndex - 1 downTo 0) {
            val line = cleanLore[i]
            if (line.isNotEmpty() &&
                !line.contains(stopIndicator, ignoreCase = true) &&
                !shardRegex.containsMatchIn(line)
            ) {
                startIndex = i
            } else {
                break
            }
        }

        val sb = StringBuilder()
        for (i in startIndex until lore.size) {
            val originalLine = lore[i]
            val cleanLine = cleanLore[i]
            if (cleanLine.contains(stopIndicator, ignoreCase = true) ||
                shardRegex.containsMatchIn(cleanLine)
            ) break
            if (sb.isNotEmpty()) sb.append(" ")
            sb.append(originalLine.trim())
        }
        val statBoost = sb.toString()
        val cleanStatBoost = ColorUtils.stripColorCodes(statBoost)
        val reqIndex = cleanStatBoost.indexOf("Requires")

        if (reqIndex == -1) return statBoost

        fun getColoredSubstring(fullString: String, cleanEndIndex: Int): String {
            val builder = StringBuilder()
            var cleanCharCount = 0
            var i = 0
            while (i < fullString.length) {
                if (cleanCharCount >= cleanEndIndex) break

                val char = fullString[i]
                builder.append(char)

                if (char == '§' && i + 1 < fullString.length) {
                    builder.append(fullString[i + 1])
                    i += 2
                } else {
                    cleanCharCount++
                    i++
                }
            }
            return builder.toString().trim()
        }

        return getColoredSubstring(statBoost, reqIndex)
    }

    private fun extractWaysToObtain(lore: List<String>): List<String> {
        val ways = mutableListOf<String>()
        var i = 0
        while (i < lore.size) {
            val currentLine = lore[i]
            val cleanLine = ColorUtils.stripColorCodes(currentLine).trim()
            if (cleanLine.startsWith("- ")) {
                val dashIndex = currentLine.indexOf("- ")
                if (dashIndex == -1) {
                    i++
                    continue
                }

                val sb = StringBuilder(currentLine.substring(dashIndex + 2).trim())
                var j = i + 1
                while (j < lore.size && !ColorUtils.stripColorCodes(lore[j]).trim().startsWith("- ")) {
                    val nextLine = lore[j].trim()
                    if (ColorUtils.stripColorCodes(nextLine).isNotEmpty()) {
                        sb.append(" ").append(nextLine)
                    }
                    j++
                }
                ways.add(sb.toString())
                i = j
            } else {
                i++
            }
        }
        return ways
    }

    private fun updateAttributeRepository(attributeInfo: AttributeInfo, existingRepo: Map<String, JsonObject>) {
        val updatedRepo = existingRepo.toMutableMap()
        if (updatedRepo.containsKey(attributeInfo.name)) {
            val existingData = updatedRepo[attributeInfo.name]!!
            var hasChanges = false
            if (attributeInfo.maxShards != 0) {
                existingData.addProperty("maxShards", attributeInfo.maxShards)
                hasChanges = true
            }
            if (attributeInfo.maxStatBoost.isNotEmpty()) {
                existingData.addProperty("maxStatBoost", attributeInfo.maxStatBoost)
                hasChanges = true
            }
            if (attributeInfo.wayToObtain.isNotEmpty()) {
                val wayArray = JsonArray().apply {
                    attributeInfo.wayToObtain.forEach { add(it) }
                }
                existingData.add("wayToObtain", wayArray)
                hasChanges = true
            }
            if (hasChanges) {
                saveRepository(updatedRepo)
                ChatUtils.messageToChat("§aUpdated info for \"${attributeInfo.name}\"").send()
                logger.info("Updated repository for attribute \"${attributeInfo.name}\"")
                SoundUtils.playSound(
                    SoundEvents.BLOCK_BELL_USE,
                    SoundCategory.BLOCKS
                )
            } else {
                ChatUtils.messageToChat("§eNo new info for \"${attributeInfo.name}\"").send()
                logger.info("No update needed for \"${attributeInfo.name}\"")
            }
        } else {
            ChatUtils.messageToChat("§cAttribute \"${attributeInfo.name}\" not found in repository!").send()
            logger.warn("Attribute \"${attributeInfo.name}\" not in repository")
        }
    }
    private fun saveRepository(repo: Map<String, JsonObject>) {
        try {
            repoFile.parentFile.mkdirs()
            FileWriter(repoFile).use { writer ->
                gson.toJson(repo, writer)
            }
            logger.info("Successfully saved updated attribute repository")
        } catch (e: Exception) {
            logger.error("Failed to save repository: ${e.message}", e)
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

    private data class AttributeInfo(
        val name: String,
        val maxShards: Int,
        val maxStatBoost: String,
        val wayToObtain: List<String>
    )
}
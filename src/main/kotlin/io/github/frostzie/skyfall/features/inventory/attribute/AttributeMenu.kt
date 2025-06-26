package io.github.frostzie.skyfall.features.inventory.attribute

import com.google.gson.JsonObject
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.features.inventory.InventoryConfig
import io.github.frostzie.skyfall.data.RarityType
import io.github.frostzie.skyfall.data.RepoManager
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyHeld
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.events.SlotRenderEvents
import io.github.frostzie.skyfall.utils.events.TooltipEvents
import io.github.frostzie.skyfall.utils.item.TooltipUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.text.MutableText
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color

object AttributeMenu {
    private val logger = LoggerProvider.getLogger("AttributeMenu")
    private val config get() = SkyFall.Companion.feature.inventory.attributeMenu
    private const val MAX_LORE_WIDTH = 190

    private val validSlotRanges = setOf(
        10..16,
        19..25,
        28..34,
        37..43
    )

    private var attributeData: JsonObject? = null
    private var attributeLevelData: JsonObject? = null
    private var lastDataLoadTime = 0L
    private const val DATA_REFRESH_INTERVAL = 60000L

    fun init() {
        registerSlotRenderEvent()
        registerTooltipEvent()
        loadAttributeData()
    }

    private fun registerSlotRenderEvent() {
        SlotRenderEvents.listen { event ->
            onRenderSlot(event.context, event.slot)
        }
    }

    private fun registerTooltipEvent() {
        TooltipEvents.register { stack, lines ->
            onTooltipRender(stack, lines)
        }
    }

    private fun loadAttributeData() {
        val currentTime = System.currentTimeMillis()
        if (attributeData == null || attributeLevelData == null || currentTime - lastDataLoadTime > DATA_REFRESH_INTERVAL) {
            try {
                attributeData = RepoManager.loadJsonFile("constants/AttributeMenuData.json")
                attributeLevelData = RepoManager.loadJsonFile("constants/AttributeMenuLevel.json")
                lastDataLoadTime = currentTime

                val dataLoaded = attributeData != null
                val levelDataLoaded = attributeLevelData != null

                if (dataLoaded && levelDataLoaded) {
                    logger.info("Successfully loaded attribute data. Found ${attributeData!!.keySet().size} attribute entries and level data.")
                } else {
                    if (!dataLoaded) {
                        logger.warn("Failed to load attribute menu data from 'AttributeMenuData.json'. The file might be missing or invalid.")
                    }
                    if (!levelDataLoaded) {
                        logger.warn("Failed to load attribute level data from 'AttributeMenuLevel.json'. The file might be missing or invalid.")
                    }
                }
            } catch (e: Exception) {
                logger.error("An exception occurred while loading attribute menu data", e)
                attributeData = null
                attributeLevelData = null
            }
        }
    }

    fun onRenderSlot(context: DrawContext, slot: Slot) {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen !is HandledScreen<*> || !isAttributeMenu(currentScreen)) {
            return
        }

        val slotIndex = slot.index
        if (!isSlotInChestInventory(slot) || !validSlotRanges.any { slotIndex in it } || slot.stack.isEmpty) {
            return
        }

        if (hasLore(slot) && config.highlightDisabled) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color(255, 0, 0, 220).rgb)
        } else if (isMax(slot) && config.highlightMaxed) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color(255, 170, 0, 220).rgb)
        }
    }

    fun onTooltipRender(stack: ItemStack, lines: MutableList<Text>) {
        if (!isInAttributeMenu() || stack.isEmpty) {
            return
        }

        loadAttributeData()

        if (attributeData == null) {
            logger.warn("Aborting feature: attributeData is null.")
            return
        }

        val itemName = getCleanItemName(stack)
        if (itemName.isEmpty()) {
            return
        }

        val itemData = findItemData(itemName) ?: return
        val shouldShowMaxStat = shouldShowMaxStatBoost()
        val shouldShowObtain = shouldShowObtainInfo()
        val showShardsLeftToMax = showShardsLeftToMax()

        if (shouldShowMaxStat) {
            addMaxStatBoostLore(stack, lines, itemData)
        }

        if (shouldShowObtain) {
            addObtainInfoLore(stack, lines, itemData)
        }

        if (showShardsLeftToMax) {
            addShardsLeftToMaxLore(stack, lines, itemData)
        }
    }

    private fun isInAttributeMenu(): Boolean {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        return currentScreen is HandledScreen<*> && isAttributeMenu(currentScreen)
    }

    private fun getCleanItemName(stack: ItemStack): String {
        val rawName = stack.name.string
        return ColorUtils.stripColorCodes(rawName).trim()
    }

    private fun findItemData(itemName: String): JsonObject? {
        var itemData = attributeData?.getAsJsonObject(itemName)

        if (itemData == null) {
            val cleanedName = cleanItemNameForMatching(itemName)
            itemData = attributeData?.getAsJsonObject(cleanedName)

            if (itemData == null) {
                itemData = findItemDataByPartialMatch(itemName)
            }
        }

        return itemData
    }

    private fun cleanItemNameForMatching(itemName: String): String {
        return itemName
            .replace(Regex("^(✦\\s*)?"), "") // Remove ✦ prefix
            .replace(Regex("\\s*\\(.*\\)$"), "") // Remove parentheses at end
            .replace(Regex("\\s+X$"), "") // Remove " X" suffix for maxed items
            .trim()
    }

    private fun findItemDataByPartialMatch(itemName: String): JsonObject? {
        val cleanedInput = cleanItemNameForMatching(itemName).lowercase()

        for (key in attributeData!!.keySet()) {
            val cleanedKey = cleanItemNameForMatching(key).lowercase()
            if (cleanedInput.contains(cleanedKey) || cleanedKey.contains(cleanedInput)) {
                return attributeData!!.getAsJsonObject(key)
            }
        }
        return null
    }

    private fun shouldShowMaxStatBoost(): Boolean {
        return when (config.showMaxStatBoost) {
            InventoryConfig.AttributeMenuConfig.ShowMaxBoost.ALWAYS -> true
            InventoryConfig.AttributeMenuConfig.ShowMaxBoost.SHIFT -> GLFW.GLFW_KEY_LEFT_SHIFT.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.ShowMaxBoost.CONTROL -> GLFW.GLFW_KEY_LEFT_CONTROL.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.ShowMaxBoost.ALT -> GLFW.GLFW_KEY_LEFT_ALT.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.ShowMaxBoost.NEVER -> false
        }
    }

    private fun showShardsLeftToMax(): Boolean {
        return when (config.showShardsLeftToMax) {
            InventoryConfig.AttributeMenuConfig.LeftToMax.ALWAYS -> true
            InventoryConfig.AttributeMenuConfig.LeftToMax.SHIFT -> GLFW.GLFW_KEY_LEFT_SHIFT.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.LeftToMax.CONTROL -> GLFW.GLFW_KEY_LEFT_CONTROL.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.LeftToMax.ALT -> GLFW.GLFW_KEY_LEFT_ALT.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.LeftToMax.NEVER -> false
        }
    }

    private fun shouldShowObtainInfo(): Boolean {
        return when (config.obtainOption) {
            InventoryConfig.AttributeMenuConfig.ObtainShow.ALWAYS -> true
            InventoryConfig.AttributeMenuConfig.ObtainShow.SHIFT -> GLFW.GLFW_KEY_LEFT_SHIFT.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.ObtainShow.CONTROL -> GLFW.GLFW_KEY_LEFT_CONTROL.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.ObtainShow.ALT -> GLFW.GLFW_KEY_LEFT_ALT.isKeyHeld()
            InventoryConfig.AttributeMenuConfig.ObtainShow.NEVER -> false
        }
    }

    private fun addMaxStatBoostLore(stack: ItemStack, lines: MutableList<Text>, itemData: JsonObject) {
        try {
            val maxStatBoost = itemData.get("maxStatBoost")?.asString ?: return
            val itemType = itemData.get("type")?.asString ?: return

            var typeLineIndex = -1
            var sourceLineIndex = -1

            for (i in lines.indices) {
                val cleanLine = ColorUtils.stripColorCodes(lines[i].string)

                if (typeLineIndex == -1 && cleanLine.contains(itemType, ignoreCase = true)) {
                    typeLineIndex = i
                }

                if (sourceLineIndex == -1 && lines[i].string.contains("Source:", ignoreCase = true)) {
                    sourceLineIndex = i
                }
            }

            if (typeLineIndex == -1 || sourceLineIndex == -1 || typeLineIndex >= sourceLineIndex) {
                return
            }

            val linesToRemove = mutableListOf<Int>()
            for (i in (typeLineIndex + 1) until sourceLineIndex) {
                linesToRemove.add(i)
            }

            linesToRemove.reversed().forEach { index ->
                lines.removeAt(index)
            }

            val textRenderer = MinecraftClient.getInstance().textRenderer
            val emptyLine = Text.literal("")
            val wrappedOrderedLines = textRenderer.wrapLines(Text.literal(maxStatBoost), MAX_LORE_WIDTH)
            val wrappedTextLines = wrappedOrderedLines.map { fromOrdered(it) }
            val newLines = mutableListOf(emptyLine)
            newLines.addAll(wrappedTextLines)
            newLines.add(emptyLine)

            lines.addAll(typeLineIndex + 1, newLines)

        } catch (e: Exception) {
            logger.error("Failed to add max stat boost lore for item", e)
        }
    }

    private fun addObtainInfoLore(stack: ItemStack, lines: MutableList<Text>, itemData: JsonObject) {
        try {
            val wayToObtainArray = itemData.getAsJsonArray("wayToObtain") ?: return
            val sourceLineIndex = lines.indexOfFirst {
                ColorUtils.stripColorCodes(it.string).startsWith("Source:", ignoreCase = true)
            }

            if (sourceLineIndex != -1) {
                val textRenderer = MinecraftClient.getInstance().textRenderer
                val additionalLines = wayToObtainArray.flatMap { element ->
                    val obtainText = "§7  • ${element.asString}"
                    val wrappedOrderedLines = textRenderer.wrapLines(Text.literal(obtainText), MAX_LORE_WIDTH)
                    wrappedOrderedLines.map { fromOrdered(it) }
                }

                if (additionalLines.isNotEmpty()) {
                    lines.addAll(sourceLineIndex + 1, additionalLines)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to add obtain info lore for item", e)
        }
    }

    private fun addShardsLeftToMaxLore(stack: ItemStack, lines: MutableList<Text>, itemData: JsonObject) {
        try {
            if (attributeLevelData == null) {
                logger.warn("Aborting shards lore: attributeLevelData is null.")
                return
            }

            val rarity = getRarityFromLore(stack) ?: return
            val levelInfo = parseLevelInfo(stack) ?: return

            val shardsNeeded = calculateShardsToMax(rarity, levelInfo.currentLevel, levelInfo.isUnlocked)
            if (shardsNeeded <= 0) {
                return
            }

            val insertIndex = findShardsInsertIndex(lines)

            val shardsLeftText = Text.literal("§7Shards left to max: §b${shardsNeeded}")
            lines.add(insertIndex, shardsLeftText)

        } catch (e: Exception) {
            val itemName = getCleanItemName(stack)
            logger.error("[$itemName] An unexpected error occurred in addShardsLeftToMaxLore", e)
        }
    }

    private fun fromOrdered(orderedText: OrderedText): MutableText {
        val fullText = Text.empty()
        val buffer = StringBuilder()
        var lastStyle: Style? = null
        val flushBuffer = {
            if (buffer.isNotEmpty()) {
                fullText.append(Text.literal(buffer.toString()).setStyle(lastStyle))
                buffer.setLength(0)
            }
        }

        orderedText.accept { _, style, codePoint ->
            if (style != lastStyle && buffer.isNotEmpty()) {
                flushBuffer()
            }
            lastStyle = style
            buffer.append(Character.toChars(codePoint))
            true
        }
        flushBuffer()

        return fullText
    }

    private fun getRarityFromLore(stack: ItemStack): RarityType? {
        try {
            val loreLines = TooltipUtils.getLoreAsStrings(stack)
            for (line in loreLines) {
                val cleanLine = ColorUtils.stripColorCodes(line)
                if (cleanLine.trim().startsWith("Rarity:", ignoreCase = true)) {
                    val parts = cleanLine.split(":", limit = 2)
                    if (parts.size < 2) continue

                    val rarityName = parts[1].trim().split(" ")[0]
                    if (rarityName.isEmpty()) continue

                    return try {
                        RarityType.valueOf(rarityName.uppercase())
                    } catch (e: IllegalArgumentException) {
                        logger.error("[${getCleanItemName(stack)}] Could not parse rarity from name: '$rarityName'.")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while getting rarity from lore", e)
        }
        return null
    }

    private data class LevelInfo(val currentLevel: Int, val isUnlocked: Boolean)

    private fun parseLevelInfo(stack: ItemStack): LevelInfo? {
        try {
            val loreLines = TooltipUtils.getLoreAsStrings(stack)
            var foundLevel: Int? = null
            var isLocked = false

            for (line in loreLines) {
                val cleanLine = ColorUtils.stripColorCodes(line)

                if (cleanLine.startsWith("Level: ")) {
                    val levelText = cleanLine.substring("Level: ".length).trim().split(" ")[0]
                    foundLevel = levelText.toIntOrNull()
                    break
                } else if (cleanLine.contains("shard to unlock!", ignoreCase = true)) {
                    isLocked = true
                }
            }

            if (foundLevel != null) {
                return LevelInfo(foundLevel, true)
            }
            if (isLocked) {
                return LevelInfo(0, false)
            }
            return null
        } catch (e: Exception) {
            logger.error("[${getCleanItemName(stack)}] An exception occurred while parsing level info", e)
            return null
        }
    }

    private fun calculateShardsToMax(rarity: RarityType, currentLevel: Int, isUnlocked: Boolean): Int {
        val rarityName = rarity.name.lowercase()
        try {
            val rarityData = attributeLevelData?.getAsJsonObject("rarities")?.getAsJsonObject(rarityName)
            if (rarityData == null) {
                logger.warn("No level data found for rarity '$rarityName' in AttributeMenuLevel.json")
                return 0
            }

            val totalShardsToMax = rarityData.get("total")?.asInt
            if (totalShardsToMax == null) {
                logger.warn("Missing 'total' shard count for rarity '$rarityName'")
                return 0
            }

            if (!isUnlocked) {
                return totalShardsToMax
            }

            var shardsSpent = 0
            for (level in 1..currentLevel) {
                val shardsForLevel = rarityData.get("level_$level")?.asInt
                if (shardsForLevel == null) {
                    logger.warn("[$rarityName] Missing shard cost for 'level_$level' in JSON. Assuming 0.")
                    continue
                }
                shardsSpent += shardsForLevel
            }
            return totalShardsToMax - shardsSpent
        } catch (e: Exception) {
            logger.error("[$rarityName] Failed to calculate shards to max", e)
            return 0
        }
    }

    private fun findShardsInsertIndex(lines: MutableList<Text>): Int {
        val syphonLineIndex = lines.indexOfFirst {
            val cleanLine = ColorUtils.stripColorCodes(it.string)
            cleanLine.contains("Syphon", ignoreCase = true) &&
                    (cleanLine.contains("more to level up!", ignoreCase = true) ||
                            cleanLine.contains("shard to unlock!", ignoreCase = true))
        }
        if (syphonLineIndex != -1) return syphonLineIndex + 1

        val lastObtainLineIndex = lines.indexOfLast { it.string.contains("• ") }
        if (lastObtainLineIndex != -1) return lastObtainLineIndex + 1

        val sourceLineIndex = lines.indexOfFirst {
            ColorUtils.stripColorCodes(it.string).startsWith("Source:", ignoreCase = true)
        }
        if (sourceLineIndex != -1) return sourceLineIndex + 1

        return lines.size
    }

    private fun isAttributeMenu(screen: HandledScreen<*>): Boolean {
        return screen.title.string.equals("Attribute Menu", ignoreCase = true)
    }

    private fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
    }

    private fun hasLore(slot: Slot): Boolean {
        if (!config.highlightDisabled) {
            return false
        }
        try {
            val loreLines = TooltipUtils.getCleanLoreAsStrings(slot.stack)
            return loreLines.any { line -> line.equals("Enabled: No", ignoreCase = false) }
        } catch (e: Exception) {
            logger.error("Failed to check lore for slot: ${e.message}", e)
            return false
        }
    }

    private fun isMax(slot: Slot): Boolean {
        if (!config.highlightMaxed) {
            return false
        }
        try {
            return slot.stack.name.string.contains(" X")
        } catch (e: Exception) {
            logger.error("Failed to check item name for slot: ${e.message}", e)
            return false
        }
    }
}
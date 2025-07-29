package io.github.frostzie.skyfall.features.mining

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.mining.PowderChangeEvent
import io.github.frostzie.skyfall.hud.FeatureHudElement
import io.github.frostzie.skyfall.hud.HudElementConfig
import io.github.frostzie.skyfall.hud.HudManager
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.item.TooltipUtils
import io.github.frostzie.skyfall.utils.power.PowderTracking
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.Slot
import java.io.File

@Feature(name = "Powder Hud")
object PowderHud : IEventFeature {
    override var isRunning = false
    private val logger = LoggerProvider.getLogger("PowderHud")
    private val config get() = SkyFall.feature.mining
    private val SAVE_FILE = File(MinecraftClient.getInstance().runDirectory, "config/skyfall/power/powder_hud.json")

    enum class PowderType(val displayName: String, val color: String) {
        MITHRIL("Mithril", "§2"),
        GEMSTONE("Gemstone", "§d"),
        GLACITE("Glacite", "§b")
    }

    data class PowderItem(val name: String, val cost: Int, val powderType: PowderType)

    private val trackedItems = mutableListOf<PowderItem>()
    private var currentMithril: String = "???"
    private var currentGemstone: String = "???"
    private var currentGlacite: String = "???"
    private var tickCounter = 0

    override fun shouldLoad(): Boolean {
        return config.neededPowderHud
    }

    override fun init() {
        if (isRunning) return
        isRunning = true

        loadTrackedItems()

        currentMithril = PowderTracking.getCurrentMithrilAmount() ?: "???"
        currentGemstone = PowderTracking.getCurrentGemstoneAmount() ?: "???"
        currentGlacite = PowderTracking.getCurrentGlaciteAmount() ?: "???"

        EventBus.listen(PowderChangeEvent::class.java) { event ->
            currentMithril = event.powderAmounts.mithril ?: "???"
            currentGemstone = event.powderAmounts.gemstone ?: "???"
            currentGlacite = event.powderAmounts.glacite ?: "???"
        }

        HudManager.registerElement(
            FeatureHudElement(
                id = "skyfall:powder_hud",
                name = "Powder Tracker",
                defaultConfig = HudElementConfig(x = 10, y = 160, width = 200, height = 150),
                advancedSizingOverride = false,
                minWidthOverride = 150,
                minHeightOverride = 20,
                renderAction = { drawContext, element ->
                    renderHud(
                        drawContext,
                        element.config.x,
                        element.config.y,
                        element.config.width,
                        element.config.height
                    )
                }
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(::onClientTick)
    }

    override fun terminate() {
        if (!isRunning) return
        isRunning = false
        trackedItems.clear()
        HudManager.unregisterElement("skyfall:powder_hud")
    }

    private fun onClientTick(client: MinecraftClient) {
        if (!isRunning) return

        val screen = client.currentScreen
        if (screen is HandledScreen<*> && isPowderMenu(screen)) {
            tickCounter++
            if (tickCounter % 10 == 0) {
                updateTrackedItemCosts(screen)
            }

            if (KeyboardManager.MIDDLE_MOUSE.isKeyClicked()) {
                handleMiddleClick(screen)
            }
        } else {
            tickCounter = 0
        }
    }

    private fun renderHud(drawContext: DrawContext, x: Int, y: Int, width: Int, height: Int) {
        val currentIsland = IslandDetector.getCurrentIsland()
        if (!currentIsland.isOneOf(IslandType.DWARVEN_MINES, IslandType.CRYSTAL_HOLLOWS, IslandType.MINESHAFT)) {
            return
        }

        val client = MinecraftClient.getInstance()

        if (client.player == null || trackedItems.isEmpty()) {
            return
        }

        val textRenderer = client.textRenderer
        var currentY = y
        val lineHeight = textRenderer.fontHeight + 2

        val titleText = "§6Powder Tracker"
        drawContext.drawTextWithShadow(textRenderer, titleText, x, currentY, 0xFFFFFFFF.toInt())
        currentY += lineHeight + 2

        for (powderType in PowderType.values()) {
            val itemsForType = trackedItems.filter { it.powderType == powderType }
            if (itemsForType.isEmpty()) continue

            val categoryText = "${powderType.color}${powderType.displayName} Powder"
            drawContext.drawTextWithShadow(textRenderer, categoryText, x, currentY, 0xFFFFFFFF.toInt())
            currentY += lineHeight

            val currentAmount = getCurrentPowderAmount(powderType)
            val currentAmountNum = getCurrentPowderAmountAsNumber(powderType) ?: 0L
            var remainingPowder = currentAmountNum
            var isFocusPerkFound = false

            itemsForType.forEach { item ->
                val isFunded = remainingPowder >= item.cost
                val allocatedPowder = if (isFunded) item.cost.toLong() else remainingPowder

                var displayCount = allocatedPowder
                var displayColor = if (isFunded) "§a" else "§f"

                if (config.onePerkAtATime) {
                    if (!isFunded && !isFocusPerkFound) {
                        isFocusPerkFound = true
                    } else if (isFocusPerkFound) {
                        displayCount = 0L
                        displayColor = "§f"
                    }
                }

                if (isFunded) {
                    remainingPowder -= item.cost
                } else {
                    remainingPowder = 0
                }

                // [COLOR][NAME]: §c[NEEDED]§f/§e[HAVE]
                val itemText = "  ${displayColor}${item.name}: §c${formatNumber(item.cost.toLong())}§f/§e${formatNumber(displayCount)}"
                drawContext.drawTextWithShadow(textRenderer, itemText, x, currentY, 0xFFFFFFFF.toInt())
                currentY += lineHeight
            }

            val totalCost = itemsForType.sumOf { it.cost }
            val totalText = "  §eTotal: §c${formatNumber(totalCost.toLong())}§f/§e${currentAmount}"
            drawContext.drawTextWithShadow(textRenderer, totalText, x, currentY, 0xFFFFFFFF.toInt())
            currentY += lineHeight + 3
        }
    }

    private fun getCurrentPowderAmount(powderType: PowderType): String {
        return when (powderType) {
            PowderType.MITHRIL -> currentMithril
            PowderType.GEMSTONE -> currentGemstone
            PowderType.GLACITE -> currentGlacite
        }
    }

    private fun getCurrentPowderAmountAsNumber(powderType: PowderType): Long? {
        return when (powderType) {
            PowderType.MITHRIL -> PowderTracking.getCurrentMithrilAmountAsNumber()
            PowderType.GEMSTONE -> PowderTracking.getCurrentGemstoneAmountAsNumber()
            PowderType.GLACITE -> PowderTracking.getCurrentGlaciteAmountAsNumber()
        }
    }

    private fun getHoveredSlot(screen: HandledScreen<*>): Slot? {
        return try {
            screen.focusedSlot
        } catch (e: Exception) {
            logger.error("Failed to get hovered slot: ${e.message}", e)
            val mouseX = MinecraftClient.getInstance().mouse.x * screen.width / MinecraftClient.getInstance().window.width
            val mouseY = MinecraftClient.getInstance().mouse.y * screen.height / MinecraftClient.getInstance().window.height

            try {
                screen.getSlotAt(mouseX, mouseY)
            } catch (e2: Exception) {
                logger.error("Failed to get slot at mouse position: ${e2.message}", e2)
                null
            }
        }
    }

    private fun handleMiddleClick(screen: HandledScreen<*>) {
        try {
            val hoveredSlot = getHoveredSlot(screen) ?: return

            if (!isSlotInChestInventory(hoveredSlot) || hoveredSlot.stack.isEmpty) {
                return
            }

            val stack = hoveredSlot.stack
            val itemName = extractItemName(stack) ?: return
            val (powderCost, powderType) = extractPowderCost(stack) ?: return

            val powderItem = PowderItem(itemName, powderCost, powderType)

            val existingItem = trackedItems.find { it.name == itemName && it.powderType == powderType }
            if (existingItem != null) {
                trackedItems.remove(existingItem)
                logger.debug("Removed tracked item: $itemName (${powderType.displayName})")
            } else {
                trackedItems.add(powderItem)
                logger.debug("Added new tracked item: $itemName with cost $powderCost (${powderType.displayName})")
            }
            saveTrackedItems()

        } catch (e: Exception) {
            logger.error("Error handling middle click in PowderHud", e)
        }
    }

    private fun updateTrackedItemCosts(screen: HandledScreen<*>) {
        var costsUpdated = false
        for (slot in screen.screenHandler.slots) {
            if (slot.inventory is PlayerInventory || !slot.hasStack()) continue

            val stack = slot.stack
            val itemName = extractItemName(stack) ?: continue

            val trackedItem = trackedItems.find { it.name == itemName } ?: continue

            val (newCost, newPowderType) = extractPowderCost(stack) ?: continue

            if (trackedItem.cost != newCost || trackedItem.powderType != newPowderType) {
                val itemIndex = trackedItems.indexOf(trackedItem)
                if (itemIndex != -1) {
                    trackedItems[itemIndex] = trackedItem.copy(cost = newCost, powderType = newPowderType)
                    logger.debug("Updated cost for ${trackedItem.name} from ${trackedItem.cost} to $newCost (${newPowderType.displayName})")
                    costsUpdated = true
                }
            }
        }

        if (costsUpdated) saveTrackedItems()
    }

    private fun isPowderMenu(screen: HandledScreen<*>): Boolean {
        return screen.title.string.equals("Heart of the Mountain", ignoreCase = false)
    }

    private fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
    }

    private fun extractItemName(stack: net.minecraft.item.ItemStack): String? {
        return try {
            val displayName = stack.name.string
            ColorUtils.stripColorCodes(displayName).trim().takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            logger.error("Failed to extract item name", e)
            null
        }
    }

    private fun extractPowderCost(stack: net.minecraft.item.ItemStack): Pair<Int, PowderType>? {
        return try {
            val cleanLore = TooltipUtils.getCleanLoreAsStrings(stack)

            if (cleanLore.isEmpty()) {
                logger.debug("No lore found in item")
                return null
            }

            var foundCostLine = false
            for (line in cleanLore) {
                val cleanLine = line.trim()

                if (cleanLine.equals("Cost", ignoreCase = true)) {
                    foundCostLine = true
                    continue
                }

                if (foundCostLine) {
                    if (cleanLine.contains("Mithril Powder", ignoreCase = true)) {
                        val numberPattern = Regex("([0-9,]+)\\s+Mithril Powder", RegexOption.IGNORE_CASE)
                        val match = numberPattern.find(cleanLine)

                        if (match != null) {
                            val numberStr = match.groupValues[1].replace(",", "")
                            val cost = numberStr.toIntOrNull()
                            if (cost != null) {
                                logger.debug("Successfully extracted Mithril cost: $cost from line: '$cleanLine'")
                                return Pair(cost, PowderType.MITHRIL)
                            }
                        }
                    }

                    if (cleanLine.contains("Gemstone Powder", ignoreCase = true)) {
                        val numberPattern = Regex("([0-9,]+)\\s+Gemstone Powder", RegexOption.IGNORE_CASE)
                        val match = numberPattern.find(cleanLine)

                        if (match != null) {
                            val numberStr = match.groupValues[1].replace(",", "")
                            val cost = numberStr.toIntOrNull()
                            if (cost != null) {
                                logger.debug("Successfully extracted Gemstone cost: $cost from line: '$cleanLine'")
                                return Pair(cost, PowderType.GEMSTONE)
                            }
                        }
                    }

                    if (cleanLine.contains("Glacite Powder", ignoreCase = true)) {
                        val numberPattern = Regex("([0-9,]+)\\s+Glacite Powder", RegexOption.IGNORE_CASE)
                        val match = numberPattern.find(cleanLine)

                        if (match != null) {
                            val numberStr = match.groupValues[1].replace(",", "")
                            val cost = numberStr.toIntOrNull()
                            if (cost != null) {
                                logger.debug("Successfully extracted Glacite cost: $cost from line: '$cleanLine'")
                                return Pair(cost, PowderType.GLACITE)
                            }
                        }
                    }
                }
            }

            logger.debug("Could not find any powder cost in item lore.")
            null
        } catch (e: Exception) {
            logger.error("Failed to extract powder cost", e)
            null
        }
    }

    private fun formatNumber(number: Long): String {
        return when {
            number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.2fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    private fun saveTrackedItems() {
        try {
            SAVE_FILE.parentFile.mkdirs()
            val gson = Gson()
            val json = gson.toJson(trackedItems)
            SAVE_FILE.writeText(json)
            logger.debug("Saved tracked powder items.")
        } catch (e: Exception) {
            logger.error("Failed to save tracked powder items", e)
        }
    }

    private fun loadTrackedItems() {
        if (!SAVE_FILE.exists()) return
        try {
            val json = SAVE_FILE.readText()
            if (json.isBlank()) return

            val gson = Gson()
            val type = object : TypeToken<MutableList<PowderItem>>() {}.type
            val loadedItems: MutableList<PowderItem>? = gson.fromJson(json, type)

            if (loadedItems != null) {
                trackedItems.clear()
                trackedItems.addAll(loadedItems)
                logger.debug("Loaded ${trackedItems.size} tracked powder items.")
            }
        } catch (e: Exception) {
            logger.error("Failed to load tracked powder items", e)
        }
    }
}
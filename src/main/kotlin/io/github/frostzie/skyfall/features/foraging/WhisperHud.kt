package io.github.frostzie.skyfall.features.foraging

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.mining.WhisperChangeEvent
import io.github.frostzie.skyfall.hud.FeatureHudElement
import io.github.frostzie.skyfall.hud.HudElementConfig
import io.github.frostzie.skyfall.hud.HudManager
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.item.TooltipUtils
import io.github.frostzie.skyfall.utils.power.WhisperTracking
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.Slot
import java.io.File

@Feature(name = "Whisper Hud")
object WhisperHud : IEventFeature {
    override var isRunning = false
    private val logger = LoggerProvider.getLogger("WhisperHud")
    private val config get() = SkyFall.feature.foraging
    private val SAVE_FILE = File(MinecraftClient.getInstance().runDirectory, "config/skyfall/power/whisper_hud.json")

    data class WhisperItem(val name: String, val cost: Int)

    private val trackedItems = mutableListOf<WhisperItem>()
    private var currentWhispers: String = "???"
    private var tickCounter = 0

    override fun shouldLoad(): Boolean {
        return config.neededWhisperHud
    }

    override fun init() {
        if (isRunning) return
        isRunning = true

        loadTrackedItems()

        currentWhispers = WhisperTracking.getCurrentWhisperAmount() ?: "???"

        EventBus.listen(WhisperChangeEvent::class.java) { event ->
            currentWhispers = event.newAmount ?: "???"
        }

        HudManager.registerElement(
            FeatureHudElement(
                id = "skyfall:whisper_hud",
                name = "Forest Whisper Tracker",
                defaultConfig = HudElementConfig(x = 10, y = 160, width = 200, height = 100),
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
        HudManager.unregisterElement("skyfall:whisper_hud")
    }

    private fun onClientTick(client: MinecraftClient) {
        if (!isRunning) return

        val screen = client.currentScreen
        if (screen is HandledScreen<*> && isHeartOfForestMenu(screen)) {
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
        if (!currentIsland.isOneOf(IslandType.THE_PARK, IslandType.GALATEA)) {
            return
        }

        val client = MinecraftClient.getInstance()

        if (client.player == null || trackedItems.isEmpty()) {
            return
        }

        val textRenderer = client.textRenderer
        var currentY = y
        val lineHeight = textRenderer.fontHeight + 2

        val currentWhispersNum = WhisperTracking.getCurrentWhisperAmountAsNumber() ?: 0L
        var remainingWhispers = currentWhispersNum
        var isFocusPerkFound = false

        val titleText = "§6Forest Whisper Tracker"
        drawContext.drawTextWithShadow(textRenderer, titleText, x, currentY, 0xFFFFFFFF.toInt())
        currentY += lineHeight + 2

        trackedItems.forEach { item ->
            val isFunded = remainingWhispers >= item.cost
            val allocatedWhispers = if (isFunded) item.cost.toLong() else remainingWhispers

            var displayCount = allocatedWhispers
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
                remainingWhispers -= item.cost
            } else {
                remainingWhispers = 0
            }

            // [COLOR][NAME]: §c[NEEDED]§f/§e[HAVE]
            val itemText = "${displayColor}${item.name}: §c${formatNumber(item.cost.toLong())}§f/§e${formatNumber(displayCount)}"
            drawContext.drawTextWithShadow(textRenderer, itemText, x, currentY, 0xFFFFFFFF.toInt())
            currentY += lineHeight
        }

        currentY += 2

        val totalCost = trackedItems.sumOf { it.cost }
        val totalText = "§eTotal: §c${formatNumber(totalCost.toLong())}§f/§e${currentWhispers}"
        drawContext.drawTextWithShadow(textRenderer, totalText, x, currentY, 0xFFFFFFFF.toInt())
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
            val whisperCost = extractWhisperCost(stack) ?: return

            val whisperItem = WhisperItem(itemName, whisperCost)

            val existingItem = trackedItems.find { it.name == itemName }
            if (existingItem != null) {
                trackedItems.remove(existingItem)
                logger.debug("Removed tracked item: $itemName")
            } else {
                trackedItems.add(whisperItem)
                logger.debug("Added new tracked item: $itemName with cost $whisperCost")
            }
            saveTrackedItems()

        } catch (e: Exception) {
            logger.error("Error handling middle click in WhisperHud", e)
        }
    }

    private fun updateTrackedItemCosts(screen: HandledScreen<*>) {
        var costsUpdated = false
        for (slot in screen.screenHandler.slots) {
            if (slot.inventory is PlayerInventory || !slot.hasStack()) continue

            val stack = slot.stack
            val itemName = extractItemName(stack) ?: continue

            val trackedItem = trackedItems.find { it.name == itemName } ?: continue

            val newCost = extractWhisperCost(stack) ?: continue

            if (trackedItem.cost != newCost) {
                val itemIndex = trackedItems.indexOf(trackedItem)
                if (itemIndex != -1) {
                    trackedItems[itemIndex] = trackedItem.copy(cost = newCost)
                    logger.debug("Updated cost for ${trackedItem.name} from ${trackedItem.cost} to $newCost")
                    costsUpdated = true
                }
            }
        }

        if (costsUpdated) saveTrackedItems()
    }

    private fun isHeartOfForestMenu(screen: HandledScreen<*>): Boolean {
        return screen.title.string.equals("Heart of the Forest", ignoreCase = false)
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

    private fun extractWhisperCost(stack: net.minecraft.item.ItemStack): Int? {
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

                if (foundCostLine && cleanLine.contains("Forest Whispers", ignoreCase = true)) {
                    val numberPattern = Regex("([0-9,]+)\\s+Forest Whispers", RegexOption.IGNORE_CASE)
                    val match = numberPattern.find(cleanLine)

                    if (match != null) {
                        val numberStr = match.groupValues[1].replace(",", "")
                        return numberStr.toIntOrNull()?.also {
                            logger.debug("Successfully extracted whisper cost: $it from line: '$cleanLine'")
                        } ?: run {
                            logger.warn("Failed to convert '$numberStr' to integer")
                            null
                        }
                    } else {
                        logger.warn("Number pattern did not match in line: '$cleanLine'")
                    }
                }
            }

            logger.debug("Could not find Forest Whispers cost in item lore.")
            null
        } catch (e: Exception) {
            logger.error("Failed to extract whisper cost", e)
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
            logger.debug("Saved tracked whisper items.")
        } catch (e: Exception) {
            logger.error("Failed to save tracked whisper items", e)
        }
    }

    private fun loadTrackedItems() {
        if (!SAVE_FILE.exists()) return
        try {
            val json = SAVE_FILE.readText()
            if (json.isBlank()) return

            val gson = Gson()
            val type = object : TypeToken<MutableList<WhisperItem>>() {}.type
            val loadedItems: MutableList<WhisperItem>? = gson.fromJson(json, type)

            if (loadedItems != null) {
                trackedItems.clear()
                trackedItems.addAll(loadedItems)
                logger.debug("Loaded ${trackedItems.size} tracked whisper items.")
            }
        } catch (e: Exception) {
            logger.error("Failed to load tracked whisper items", e)
        }
    }
}
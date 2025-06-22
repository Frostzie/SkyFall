package io.github.frostzie.skyfall.features.inventory

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.events.SlotRenderEvents
import io.github.frostzie.skyfall.utils.item.SlotHandler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.screen.slot.Slot
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object FavoriteAbiContact {
    private val logger = LoggerProvider.getLogger("FavoriteAbiContact")
    private val configFile = File("config/skyfall/favorite-contacts.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var highlightedItems = mutableListOf<String>()
    private var favoredOnlyToggle = true
    private var currentScreen: HandledScreen<*>? = null

    private val validSlotRanges = setOf(
        10..16,
        19..25,
        28..34,
        37..43
    )

    fun init() {
        loadConfig()
        registerSlotHandler()
        registerTickHandler()
        registerSlotRenderEvent()
    }

    private fun registerSlotHandler() {
        SlotHandler.registerHandler { event ->
            val currentScreen = MinecraftClient.getInstance().currentScreen
            if (currentScreen is HandledScreen<*> && isAbiPhoneContacts(currentScreen)) {
                if (isSlotInChestInventory(event.slot) && validSlotRanges.any { event.slotNumber in it }) {
                    if (favoredOnlyToggle && !event.slot.stack.isEmpty) {
                        val itemName = event.slot.stack.name.string
                        if (!highlightedItems.contains(itemName)) {
                            event.blockAndHide()
                        }
                    }
                }
            }
        }
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val screen = client.currentScreen
            if (screen is HandledScreen<*> && isAbiPhoneContacts(screen)) {
                if (currentScreen != screen) {
                    currentScreen = screen
                    setupToggleButton(screen)
                }
                handleKeyPress(screen)
            } else {
                if (currentScreen != null) {
                    currentScreen = null
                    removeToggleButton()
                }
            }
        }
    }

    private fun setupToggleButton(screen: HandledScreen<*>) {
        removeToggleButton()

        val highlightKey = SkyFall.feature.inventory.abiContact.favoriteKey
        if (highlightKey == GLFW.GLFW_KEY_UNKNOWN || !isAbiPhoneContacts(screen)) {
            favoredOnlyToggle = false
            return
        }

        val screenX = screen.x
        val screenY = screen.y
        val backgroundWidth = screen.backgroundWidth

        var buttonX = screenX + backgroundWidth - 16
        val buttonY = screenY + 4

        val existingButtons = Screens.getButtons(screen)
        while (existingButtons.any { it is ButtonWidget && it.x == buttonX && it.y == buttonY }) {
            buttonX -= 15
        }

        val toggleButton = ButtonWidget.builder(
            Text.literal(if (favoredOnlyToggle) "F" else "A")
        ) { _ ->
            favoredOnlyToggle = !favoredOnlyToggle
            saveConfig()
            setupToggleButton(screen)
        }
            .dimensions(buttonX, buttonY, 12, 12)
            .tooltip(Tooltip.of(Text.literal(if (favoredOnlyToggle) "Click to Show All" else "Click for Favorites Only")))
            .build()

        Screens.getButtons(screen).add(toggleButton)
    }

    private fun removeToggleButton() {
        currentScreen?.let { screen ->
            Screens.getButtons(screen).removeIf { widget ->
                widget is ButtonWidget &&
                        (widget.message.string == "F" || widget.message.string == "A")
            }
        }
    }

    private fun handleKeyPress(screen: HandledScreen<*>) {
        val highlightKey = SkyFall.feature.inventory.abiContact.favoriteKey
        if (highlightKey == GLFW.GLFW_KEY_UNKNOWN) {
            return
        }

        if (KeyboardManager.run { highlightKey.isKeyClicked() }) {
            handleKeyPressAction(screen)
        }
    }

    private fun handleKeyPressAction(screen: HandledScreen<*>) {
        val hoveredSlot = getHoveredSlot(screen) ?: return
        val slotIndex = hoveredSlot.index

        if (!isSlotInChestInventory(hoveredSlot) ||
            !validSlotRanges.any { slotIndex in it } ||
            hoveredSlot.stack.isEmpty) {
            return
        }

        val itemName = hoveredSlot.stack.name.string
        if (highlightedItems.contains(itemName)) {
            highlightedItems.remove(itemName)
        } else {
            highlightedItems.add(itemName)
        }
        saveConfig()
    }

    private fun isAbiPhoneContacts(screen: HandledScreen<*>): Boolean {
        val title = screen.title.string
        return title.contains("Abiphone")
    }

    fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
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

    private fun registerSlotRenderEvent() {
        SlotRenderEvents.listen { event ->
            onRenderSlot(event.context, event.slot)
        }
    }

    fun onRenderSlot(context: DrawContext, slot: Slot) {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen !is HandledScreen<*> || !isAbiPhoneContacts(currentScreen)) {
            return
        }

        val highlightKey = SkyFall.feature.inventory.abiContact.favoriteKey
        if (highlightKey == GLFW.GLFW_KEY_UNKNOWN) {
            return
        }

        val slotIndex = slot.index
        if (!isSlotInChestInventory(slot) || !validSlotRanges.any { slotIndex in it } || slot.stack.isEmpty) {
            return
        }

        val itemName = slot.stack.name.string
        if (highlightedItems.contains(itemName)) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color(255, 170, 0, 220).rgb)
        }
    }

    private fun loadConfig() {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            return
        }

        try {
            FileReader(configFile).use { reader ->
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val configData: Map<String, Any> = gson.fromJson(reader, type) ?: emptyMap()
                highlightedItems = (configData["highlightedItems"] as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                favoredOnlyToggle = configData["favoredOnlyToggle"] as? Boolean ?: true
            }
        } catch (e: Exception) {
            logger.error("Failed to load favorite contacts config: ${e.message}", e)
            highlightedItems = mutableListOf()
            favoredOnlyToggle = true
        }
    }

    private fun saveConfig() {
        try {
            configFile.parentFile.mkdirs()
            val configData = mapOf(
                "highlightedItems" to highlightedItems,
                "favoredOnlyToggle" to favoredOnlyToggle
            )
            FileWriter(configFile).use { writer ->
                gson.toJson(configData, writer)
            }
        } catch (e: Exception) {
            logger.error("Failed to save favorite contacts config: ${e.message}", e)
        }
    }
}
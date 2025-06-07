package io.github.frostzie.skyfall.features.inventory

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.events.SlotRenderEvents
import io.github.frostzie.skyfall.utils.item.SlotHandler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.slot.Slot
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object FavoritePowerStone {
    private val configFile = File("config/skyfall/favorite-power-stones.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var keyWasPressed = false
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
            if (currentScreen is HandledScreen<*> && isAccessoryBagThaumaturgy(currentScreen)) {
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
            if (screen is HandledScreen<*> && isAccessoryBagThaumaturgy(screen)) {
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

        val highlightKey = SkyFall.feature.inventory.powerStone.favoriteKey
        if (highlightKey == GLFW.GLFW_KEY_UNKNOWN || !isAccessoryBagThaumaturgy(screen)) {
            return
        }

        val screenX = getScreenX(screen)
        val screenY = getScreenY(screen)
        val backgroundWidth = getBackgroundWidth(screen)

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

    private fun getScreenX(screen: HandledScreen<*>): Int {
        return try {
            val field = HandledScreen::class.java.getDeclaredField("x")
            field.isAccessible = true
            field.getInt(screen)
        } catch (e: Exception) {
            (screen.width - getBackgroundWidth(screen)) / 2
        }
    }

    private fun getScreenY(screen: HandledScreen<*>): Int {
        return try {
            val field = HandledScreen::class.java.getDeclaredField("y")
            field.isAccessible = true
            field.getInt(screen)
        } catch (e: Exception) {
            (screen.height - getBackgroundHeight(screen)) / 2
        }
    }

    private fun getBackgroundWidth(screen: HandledScreen<*>): Int {
        return try {
            val field = HandledScreen::class.java.getDeclaredField("backgroundWidth")
            field.isAccessible = true
            field.getInt(screen)
        } catch (e: Exception) {
            176
        }
    }

    private fun getBackgroundHeight(screen: HandledScreen<*>): Int {
        return try {
            val field = HandledScreen::class.java.getDeclaredField("backgroundHeight")
            field.isAccessible = true
            field.getInt(screen)
        } catch (e: Exception) {
            166
        }
    }

    private fun handleKeyPress(screen: HandledScreen<*>) {
        val highlightKey = SkyFall.feature.inventory.powerStone.favoriteKey
        if (highlightKey == GLFW.GLFW_KEY_UNKNOWN) {
            keyWasPressed = false
            return
        }

        val window = MinecraftClient.getInstance().window.handle
        val isPressed = if (highlightKey >= GLFW.GLFW_MOUSE_BUTTON_1 && highlightKey <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            GLFW.glfwGetMouseButton(window, highlightKey) == GLFW.GLFW_PRESS
        } else {
            InputUtil.isKeyPressed(window, highlightKey)
        }

        if (isPressed && !keyWasPressed) {
            handleKeyPressAction(screen)
        }
        keyWasPressed = isPressed
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

    private fun isAccessoryBagThaumaturgy(screen: HandledScreen<*>): Boolean {
        val title = screen.title.string
        return title.equals("Accessory Bag Thaumaturgy")
    }

    fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
    }

    private fun getHoveredSlot(screen: HandledScreen<*>): Slot? {
        try {
            val focusedSlotField = HandledScreen::class.java.getDeclaredField("focusedSlot")
            focusedSlotField.isAccessible = true
            return focusedSlotField.get(screen) as? Slot
        } catch (e: Exception) {
            val mouseX = MinecraftClient.getInstance().mouse.x * screen.width / MinecraftClient.getInstance().window.width
            val mouseY = MinecraftClient.getInstance().mouse.y * screen.height / MinecraftClient.getInstance().window.height

            try {
                val getSlotAtMethod = HandledScreen::class.java.getDeclaredMethod("getSlotAt", Double::class.javaPrimitiveType, Double::class.javaPrimitiveType)
                getSlotAtMethod.isAccessible = true
                return getSlotAtMethod.invoke(screen, mouseX, mouseY) as? Slot
            } catch (e2: Exception) {
                return null
            }
        }
    }

    private fun registerSlotRenderEvent() {
        SlotRenderEvents.register { event ->
            onRenderSlot(event.context, event.slot)
        }
    }

    fun onRenderSlot(context: DrawContext, slot: Slot) {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen !is HandledScreen<*> || !isAccessoryBagThaumaturgy(currentScreen)) {
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
                val type = object : TypeToken<List<String>>() {}.type
                highlightedItems = gson.fromJson(reader, type) ?: mutableListOf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            highlightedItems = mutableListOf()
        }
    }

    private fun saveConfig() {
        try {
            configFile.parentFile.mkdirs()
            FileWriter(configFile).use { writer ->
                gson.toJson(highlightedItems, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
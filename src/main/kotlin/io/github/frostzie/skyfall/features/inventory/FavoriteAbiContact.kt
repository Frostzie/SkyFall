package io.github.frostzie.skyfall.features.inventory

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.events.SlotRenderEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.slot.Slot
import net.minecraft.entity.player.PlayerInventory
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object FavoriteAbiContact {
    private val configFile = File("config/skyfall/favorite-contacts.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var keyWasPressed = false
    private var highlightedItems = mutableListOf<String>()

    private val validSlotRanges = setOf(
        10..16,
        19..25,
        28..34,
        37..43
    )

    fun init() {
        loadConfig()

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val currentScreen = client.currentScreen
            if (currentScreen is HandledScreen<*> && isAbiPhoneContacts(currentScreen)) {
                val highlightKey = SkyFall.feature.inventory.abiContact.favoriteKey
                val window = MinecraftClient.getInstance().window.handle

                if (highlightKey != GLFW.GLFW_KEY_UNKNOWN) {
                    val isPressed = if (highlightKey >= GLFW.GLFW_MOUSE_BUTTON_1 && highlightKey <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                        GLFW.glfwGetMouseButton(window, highlightKey) == GLFW.GLFW_PRESS
                    } else {
                        InputUtil.isKeyPressed(window, highlightKey)
                    }

                    if (isPressed && !keyWasPressed) {
                        handleKeyPress(currentScreen)
                    }
                    keyWasPressed = isPressed
                } else {
                    keyWasPressed = false
                }
            } else {
                keyWasPressed = false
            }
        }
        registerSlotRenderEvent()
    }

    private fun isAbiPhoneContacts(screen: HandledScreen<*>): Boolean {
        val title = screen.title.string
        return title.contains("Abiphone")
    }

    fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
    }

    private fun handleKeyPress(screen: HandledScreen<*>) {
        val hoveredSlot = getHoveredSlot(screen) ?: return
        val slotIndex = hoveredSlot.index

        if (!isSlotInChestInventory(hoveredSlot)) {
            return
        }

        if (!validSlotRanges.any { slotIndex in it }) {
            return
        }

        if (hoveredSlot.stack.isEmpty) {
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
        if (currentScreen !is HandledScreen<*> || !isAbiPhoneContacts(currentScreen)) {
            return
        }

        val slotIndex = slot.index

        if (!isSlotInChestInventory(slot)) {
            return
        }

        if (!validSlotRanges.any { slotIndex in it } || slot.stack.isEmpty) {
            return
        }

        val itemName = slot.stack.name.string
        if (highlightedItems.contains(itemName)) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, Color(15, 255, 0, 100).rgb)
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
package io.github.frostzie.skyfall.features.misc.keybind

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

class MiscKeybindManager {
    private val keybindStates = mutableMapOf<Int, Boolean>()
    private val commandCooldowns = mutableMapOf<String, SimpleTimeMark>()
    private val config = SkyFall.feature.miscFeatures.keybinds

    private val keybindCommandMap = mapOf(
        { config.petsMenuKeybind } to "pets",
        { config.storageMenuKeybind } to "storage",
        { config.wardrobeMenuKeybind } to "wardrobe",
        { config.equipmentMenuKeybind } to "equipment",
        { config.potionBagKeybind } to "potbag",
        { config.tradeMenuKeybind } to "trades"
    )

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val window = MinecraftClient.getInstance().window.handle
                val player = client.player ?: return@register

                keybindCommandMap.forEach { (keybindGetter, command) ->
                    val keybind = keybindGetter()

                    if (keybind != GLFW.GLFW_KEY_UNKNOWN) {
                        val isPressed = isKeyPressed(window, keybind)
                        val wasPressed = keybindStates.getOrDefault(keybind, false)
                        val lastCommandTime = commandCooldowns.getOrDefault(command, SimpleTimeMark.farPast())

                        if (isPressed && !wasPressed && lastCommandTime.passedSince() >= 450.milliseconds) {
                            player.networkHandler.sendChatCommand(command)
                            commandCooldowns[command] = SimpleTimeMark.now()
                        }

                        keybindStates[keybind] = isPressed
                    } else {
                        keybindStates.remove(keybind)
                    }
                }
            } else {
                keybindStates.clear()
            }
        }
    }

    private fun isKeyPressed(window: Long, key: Int): Boolean {
        return if (key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            GLFW.glfwGetMouseButton(window, key) == GLFW.GLFW_PRESS
        } else {
            InputUtil.isKeyPressed(window, key)
        }
    }
}
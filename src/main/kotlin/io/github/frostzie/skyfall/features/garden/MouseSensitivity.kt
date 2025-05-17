package io.github.frostzie.skyfall.features.garden

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.mixin.accessor.MouseAccessor
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.IslandManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object MouseSensitivity {

    var isLockActive: Boolean = false
        private set

    private var wasToggleKeyPressedLastTick: Boolean = false
    private var originalMouseSensitivityOptionValue: Double? = null

    private var mouseHookWasEffectiveLastTick: Boolean = false

    private val toggleKeyCode: Int
        get() = SkyFall.feature.garden.mouseSensitivity.mouseSensitivity

    private fun isFeatureConditionallyActive(): Boolean {
        val onGardenConfig = SkyFall.feature.garden.mouseSensitivity.onGarden
        return if (onGardenConfig) {
            IslandManager.isOnIsland(IslandType.GARDEN)
        } else {
            true
        }
    }

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (client.player == null) {
                if (isLockActive) {
                    restoreMouseSensitivityOption(client)
                }
                isLockActive = false
                wasToggleKeyPressedLastTick = false
                mouseHookWasEffectiveLastTick = false
                return@EndTick
            }

            val conditionsMet = isFeatureConditionallyActive()

            val currentKey = toggleKeyCode
            if (currentKey != GLFW.GLFW_KEY_UNKNOWN && currentKey >= 0) {
                val isToggleCurrentlyPressed = isKeyPressValid(client.window.handle, currentKey)
                if (isToggleCurrentlyPressed && !wasToggleKeyPressedLastTick && client.currentScreen == null) {
                    if (isLockActive) {
                        isLockActive = false
                        ChatUtils.messageToChat("§3§lSkyFall§r §8» §rMouse Lock §cDisabled")
                    } else if (conditionsMet) {
                        isLockActive = true
                        ChatUtils.messageToChat("§3§lSkyFall§r §8» §rMouse Lock §aEnabled")
                    } else {
                        ChatUtils.messageToChat("§3§lSkyFall§r §8» §rMouse Lock can only be enabled on the Garden island.")
                    }
                }
                wasToggleKeyPressedLastTick = isToggleCurrentlyPressed
            } else {
                if (isLockActive) {
                    isLockActive = false
                    ChatUtils.messageToChat("§3§lSkyFall§r §8» §rMouse Lock §cDisabled (key unassigned)")
                }
                wasToggleKeyPressedLastTick = false
            }

            val mouseHookIsEffectiveThisTick = isLockActive && client.currentScreen == null && conditionsMet
            if (!mouseHookIsEffectiveThisTick && mouseHookWasEffectiveLastTick) {
                restoreMouseSensitivityOption(client)
                resetMouseStateAfterLock(client)
            }
            if (mouseHookIsEffectiveThisTick) {
                disableMouseMovementEffect(client)
            } else {
                if (originalMouseSensitivityOptionValue != null) {
                    restoreMouseSensitivityOption(client)
                }
            }
            mouseHookWasEffectiveLastTick = mouseHookIsEffectiveThisTick
        })
    }

    private fun isKeyPressValid(windowHandle: Long, keyCode: Int): Boolean {
        return if (keyCode >= GLFW.GLFW_MOUSE_BUTTON_1 && keyCode <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            GLFW.glfwGetMouseButton(windowHandle, keyCode) == GLFW.GLFW_PRESS
        } else {
            InputUtil.isKeyPressed(windowHandle, keyCode)
        }
    }

    private fun disableMouseMovementEffect(client: MinecraftClient) {
        if (originalMouseSensitivityOptionValue == null) {
            originalMouseSensitivityOptionValue = client.options.mouseSensitivity.value
        }
        if (client.options.mouseSensitivity.value != 0.0) {
            client.options.mouseSensitivity.value = 0.0
        }
    }

    private fun restoreMouseSensitivityOption(client: MinecraftClient) {
        originalMouseSensitivityOptionValue?.let { savedSensitivity ->
            if (client.options.mouseSensitivity.value != savedSensitivity) {
                client.options.mouseSensitivity.value = savedSensitivity
            }
            val conditionsMet = isFeatureConditionallyActive()
            if (!isLockActive || !conditionsMet) {
                originalMouseSensitivityOptionValue = null
            }
        }
    }

    private fun resetMouseStateAfterLock(client: MinecraftClient) {
        val mouse = client.mouse
        if (mouse is MouseAccessor) {
            val xPosArr = DoubleArray(1)
            val yPosArr = DoubleArray(1)
            GLFW.glfwGetCursorPos(client.window.handle, xPosArr, yPosArr)
            val currentX = xPosArr[0]
            val currentY = yPosArr[0]

            mouse.skyfall_setMouseX(currentX)
            mouse.skyfall_setMouseY(currentY)
            mouse.skyfall_setCursorDeltaX(0.0)
            mouse.skyfall_setCursorDeltaY(0.0)
        }
    }

    @JvmStatic
    fun shouldCancelMouseMovement(): Boolean {
        val client = MinecraftClient.getInstance() ?: return false
        return isLockActive && client.currentScreen == null && isFeatureConditionallyActive()
    }
}
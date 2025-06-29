package io.github.frostzie.skyfall.features.garden

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import io.github.frostzie.skyfall.mixin.accessor.MouseAccessor
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.IslandManager
import io.github.frostzie.skyfall.utils.KeyboardManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

@Feature(name = "Mouse Sensitivity Lock")
object MouseSensitivity : IFeature {

    override var isRunning = false
    var isLockActive: Boolean = false
        private set

    private var originalMouseSensitivityOptionValue: Double? = null
    private var mouseHookWasEffectiveLastTick: Boolean = false
    private val toggleKeyCode: Int
        get() = SkyFall.feature.garden.mouseSensitivity.mouseSensitivity

    override fun shouldLoad(): Boolean {
        return SkyFall.feature.garden.mouseSensitivity.mouseSensitivity != GLFW.GLFW_KEY_UNKNOWN
    }

    private fun isFeatureConditionallyActive(): Boolean {
        val onGardenConfig = SkyFall.feature.garden.mouseSensitivity.onGarden
        return if (onGardenConfig) {
            IslandManager.isOnIsland(IslandType.GARDEN)
        } else {
            true
        }
    }

    override fun init() {
        if (isRunning) return
        isRunning = true
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (client.player == null) {
                if (isLockActive) {
                    restoreMouseSensitivityOption(client)
                }
                isLockActive = false
                mouseHookWasEffectiveLastTick = false
                return@EndTick
            }

            val conditionsMet = isFeatureConditionallyActive()

            val currentKey = toggleKeyCode
            if (currentKey != GLFW.GLFW_KEY_UNKNOWN) {
                if (KeyboardManager.run { currentKey.isKeyClicked() } && client.currentScreen == null) {
                    if (isLockActive) {
                        isLockActive = false
                        ChatUtils.messageToChat("Mouse Lock §cDisabled").send()
                    } else if (conditionsMet) {
                        isLockActive = true
                        ChatUtils.messageToChat("Mouse Lock §aEnabled").send()
                    } else {
                        ChatUtils.messageToChat("§eMouse Lock can only be enabled on the Garden island.").send()
                    }
                }
            } else {
                if (isLockActive) {
                    isLockActive = false
                    ChatUtils.messageToChat("Mouse Lock §cDisabled §r(key unassigned)").send()
                }
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

    override fun terminate() {
        isRunning = false
        isLockActive = false
        restoreMouseSensitivityOption(MinecraftClient.getInstance())
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
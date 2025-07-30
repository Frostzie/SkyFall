package io.github.frostzie.skyfall.features.garden

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.hud.FeatureHudElement
import io.github.frostzie.skyfall.hud.HudElementConfig
import io.github.frostzie.skyfall.hud.HudManager
import io.github.frostzie.skyfall.mixin.accessor.MouseAccessor
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.KeyboardManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import org.lwjgl.glfw.GLFW

@Feature(name = "Mouse Sensitivity Lock")
object MouseSensitivity : IEventFeature {

    override var isRunning = false
    var isLockActive: Boolean = false
        private set

    private var originalMouseSensitivityOptionValue: Double? = null
    private var mouseHookWasEffectiveLastTick: Boolean = false
    private val toggleKeyCode: Int
        get() = SkyFall.feature.garden.mouseSensitivity.mouseSensitivity
    private val config get() = SkyFall.feature.garden.mouseSensitivity

    override fun shouldLoad(): Boolean {
        return SkyFall.feature.garden.mouseSensitivity.mouseSensitivity != GLFW.GLFW_KEY_UNKNOWN
    }

    private fun isFeatureConditionallyActive(): Boolean {
        val onGardenConfig = SkyFall.feature.garden.mouseSensitivity.onGarden
        return if (onGardenConfig) {
            IslandDetector.isOnIsland(IslandType.GARDEN)
        } else {
            true
        }
    }

    override fun init() {
        if (isRunning) return
        isRunning = true

        HudManager.registerElement(
            FeatureHudElement(
                id = "skyfall:mouse_sensitivity_lock",
                name = "Mouse Lock Status",
                defaultConfig = HudElementConfig(x = 440, y = 225, width = 90, height = 25),
                advancedSizingOverride = false,
                minWidthOverride = 80,
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

        ClientSendMessageEvents.COMMAND.register(::handleSentCommand)
    }

    override fun terminate() {
        isRunning = false
        isLockActive = false
        restoreMouseSensitivityOption(MinecraftClient.getInstance())
        HudManager.unregisterElement("skyfall:mouse_sensitivity_lock")
    }

    private fun renderHud(drawContext: DrawContext, x: Int, y: Int, width: Int, height: Int) {
        val client = MinecraftClient.getInstance()

        if (!isLockActive || client.player == null || !isFeatureConditionallyActive() || !config.showHud) {
            return
        }

        val textRenderer = client.textRenderer
        val statusText = "§lMouse: §cDisabled"

        val textWidth = textRenderer.getWidth(statusText)
        val textX = x + (width - textWidth) / 2
        val textY = y + (height - textRenderer.fontHeight) / 2

        drawContext.drawTextWithShadow(textRenderer, statusText, textX, textY, 0xFFFFFFFF.toInt())
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

    private val plotWarpCommands = listOf("plottp", "plotteleport")

    private fun handleSentCommand(command: String) {
        if (!isLockActive || !config.disableOnWarp) {
            return
        }

        val lowerCaseCommand = command.lowercase()
        if (plotWarpCommands.any { lowerCaseCommand.startsWith(it) }) {
            isLockActive = false
            ChatUtils.messageToChat("Mouse Lock §cDisabled! §r(Plot warp)").send()
        }
    }
}
package io.github.frostzie.skyfall.features.misc.keybind

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

// Original idea by Odin
@Feature(name = "Misc Keybinds")
object MiscKeybinds : IFeature {
    override var isRunning = false

    private val commandCooldowns = mutableMapOf<String, SimpleTimeMark>()
    private val config get() = SkyFall.feature.miscFeatures.keybinds

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
            if (!isRunning) return@register

            if (client.currentScreen == null) {
                val player = client.player ?: return@register

                keybindCommandMap.forEach { (keybindGetter, command) ->
                    val keybind = keybindGetter()

                    if (keybind != GLFW.GLFW_KEY_UNKNOWN) {
                        val lastCommandTime = commandCooldowns.getOrDefault(command, SimpleTimeMark.farPast())

                        if (keybind.isKeyClicked() && lastCommandTime.passedSince() >= 450.milliseconds) {
                            player.networkHandler.sendChatCommand(command)
                            commandCooldowns[command] = SimpleTimeMark.now()
                        }
                    }
                }
            }
        }
    }

    override fun shouldLoad(): Boolean {
        return keybindCommandMap.keys.any { keybindGetter ->
            keybindGetter() != GLFW.GLFW_KEY_UNKNOWN
        }
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }
}
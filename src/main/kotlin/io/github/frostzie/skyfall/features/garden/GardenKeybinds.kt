package io.github.frostzie.skyfall.features.garden

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ConditionalUtils
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import io.github.notenoughupdates.moulconfig.observer.Property
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.SignEditScreen
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

//TODO: Check other mods that register their keybinds in the minecraft options
//TODO: Finish this :skull:

object GardenKeybinds {

    private val config get() = SkyFall.feature.garden.keybindConfig
    private val mcKeybinds get() = MinecraftClient.getInstance().options

    private var map: Map<KeyBinding, Int> = emptyMap()
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastDuplicatedKeybindWarning = SimpleTimeMark.farPast()
    private var isDuplicated = false

    private fun isEnabled() = config.enabled // Add if on garden // for now just use farming tool detection and later on change that to repo
    private fun isActive() = isEnabled() && lastWindowOpenTime.passedSince() < 400.milliseconds

    fun isKeyDown(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = map[keyBinding] ?: run {
            if (map.containsValue(keyBinding.code)) {
                cir.returnValue = false
            }
            return
        }
        cir.returnValue = override.isKeyHeld()
    }

    fun isKeyPressed(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = map[keyBinding] ?: run {
            if (map.containsValue(keyBinding.keycode)) {
                cir.returnValue = false
            }
            return
        }
        cir.returnValue = override.isKeyClicked()
    }

    fun onTick(event: ClientTickEvents) {
        if (!isEnabled()) return
        val screen = MinecraftClient.getInstance().currentScreen ?: return
        if (screen !is SignEditScreen) return
        lastWindowOpenTime = SimpleTimeMark.now()
    }

    fun passedSecond() {
        if (isEnabled()) return
        if (!isDuplicated || lastDuplicatedKeybindWarning.passedSince() < 30.seconds) return
        ChatUtils.messageToChat("You aren't allowed having multiple keybinds with the same key!")
        lastDuplicatedKeybindWarning = SimpleTimeMark.now()
    }

    fun configLoad() {
        with(config) {
            ConditionalUtils.onToggle(leftClick, rightClick, moveForwards, moveRight, moveLeft, moveBackwards, moveJump, moveSneak) {
                updateSettings()
            }
            updateSettings()
        }
    }

    private fun updateSettings() {
        with(config) {
            with(mcKeybinds) {
                map = buildMap {
                    fun add(keyBinding: KeyBinding, property: Property<Int>) {
                        put(keyBinding, property.get())
                    }
                    add(attackKey, leftClick)
                    add(useKey, rightClick)
                }
            }
        }
    }

    @JvmStatic
    fun resetAll() {
        with(config) {
            leftClick.set(GLFW.GLFW_KEY_UNKNOWN)
            rightClick.set(GLFW.GLFW_KEY_UNKNOWN)
            moveForwards.set(GLFW.GLFW_KEY_UNKNOWN)
            moveLeft.set(GLFW.GLFW_KEY_UNKNOWN)
            moveRight.set(GLFW.GLFW_KEY_UNKNOWN)
            moveBackwards.set(GLFW.GLFW_KEY_UNKNOWN)
            moveJump.set(GLFW.GLFW_KEY_UNKNOWN)
            moveSneak.set(GLFW.GLFW_KEY_UNKNOWN)
        }
    }
}
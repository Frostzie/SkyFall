package io.github.frostzie.skyfall.features.garden

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ConditionalUtils.onToggle
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.KeyboardManager.KEY_NONE
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyHeld
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

object GardenKeybinds {

    private val config get() = SkyFall.feature.garden.keybindConfig

    private var map: Map<KeyBinding, Int> = emptyMap()
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastKeybindWarning = SimpleTimeMark.farPast()
    private var isDuplicated = false
    private var alreadyWarnedMouseKeys = mutableSetOf<Property<Int?>>()

    //TODO: Add detection for tool in hand and if in garden
    private val toolInHand: Boolean
        get() {
            val item = MinecraftClient.getInstance().player?.mainHandStack?.item
            return item != null
        }

    private fun inGarden(): Boolean {
        return true
    }

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { onClientTick() }

        configLoad()
    }

    private fun isEnabled() = config.enabled && inGarden() && toolInHand
    private fun isActive() = isEnabled() && !isDuplicated && !hasGuiOpen() &&
            lastWindowOpenTime.passedSince() > 300.milliseconds

    private fun hasGuiOpen() = MinecraftClient.getInstance().currentScreen != null

    @JvmStatic
    fun isKeyDown(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        if (!map.containsKey(keyBinding)) return

        val override = map[keyBinding] ?: KEY_NONE
        if (override == KEY_NONE) {
            cir.returnValue = false
            return
        }

        val window = MinecraftClient.getInstance().window.handle
        cir.returnValue = if (override >= GLFW.GLFW_MOUSE_BUTTON_1 && override <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            GLFW.glfwGetMouseButton(window, override) == GLFW.GLFW_PRESS
        } else {
            override.isKeyHeld()
        }
    }

    @JvmStatic
    fun isKeyPressed(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        if (!map.containsKey(keyBinding)) return

        val override = map[keyBinding] ?: KEY_NONE
        if (override == KEY_NONE) {
            cir.returnValue = false
            return
        }

        val window = MinecraftClient.getInstance().window.handle
        cir.returnValue = if (override >= GLFW.GLFW_MOUSE_BUTTON_1 && override <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            GLFW.glfwGetMouseButton(window, override) == GLFW.GLFW_PRESS
        } else {
            override.isKeyClicked()
        }
    }

    private fun onClientTick() {
        val screen = MinecraftClient.getInstance().currentScreen
        if (screen is SignEditScreen) {
            lastWindowOpenTime = SimpleTimeMark.now()
        }

        if (isEnabled() && isDuplicated && lastKeybindWarning.passedSince() > 30.seconds) {
            ChatUtils.messageToChat("§3§lSkyFall§r §8» §eYou aren't allowed having multiple keybinds with the same key!")
            lastKeybindWarning = SimpleTimeMark.now()
        }
    }

    fun configLoad() {
        with(config) {
            processAllKeybinds()
            onToggle(leftClick, rightClick, moveForwards, moveRight, moveLeft, moveBackwards, moveJump, moveSneak) {
                updateSettings()
            }
            updateSettings()
        }
    }

    private fun processAllKeybinds() {
        with(config) {
            val allKeybinds = listOf(
                leftClick, rightClick, moveForwards, moveLeft, moveRight, moveBackwards, moveJump, moveSneak
            )
            for (keybind in allKeybinds) {
                processKeybind(keybind)
            }
            alreadyWarnedMouseKeys.clear()
        }
    }

    private fun processKeybind(property: Property<Int?>) {
        property.get()?.let { key ->
            if (key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                if (!alreadyWarnedMouseKeys.contains(property)) {
                    ChatUtils.messageToChat("§3§lSkyFall§r §8» §eMouse buttons are not allowed for keybinds!")
                    alreadyWarnedMouseKeys.add(property)
                }
                property.set(KEY_NONE)
            }
        }
    }

    private fun calculateDuplicates() {
        isDuplicated = map.values
            .filter { it != KEY_NONE }
            .let { values -> values.size != values.toSet().size }
    }

    private fun updateSettings() {
        val minecraft = MinecraftClient.getInstance()
        val options = minecraft.options

        if (options == null) {
            return
        }
        processAllKeybinds()

        with(config) {
            map = buildMap {
                fun add(keyBinding: KeyBinding, property: Property<Int?>) {
                    put(keyBinding, property.get() ?: KEY_NONE)
                }
                add(options.attackKey, leftClick)
                add(options.useKey, rightClick)
                add(options.forwardKey, moveForwards)
                add(options.leftKey, moveLeft)
                add(options.rightKey, moveRight)
                add(options.backKey, moveBackwards)
                add(options.jumpKey, moveJump)
                add(options.sneakKey, moveSneak)
            }
        }

        calculateDuplicates()
        lastKeybindWarning = SimpleTimeMark.farPast()
        KeyBinding.unpressAll()
    }

    @JvmStatic
    fun resetAll() {
        with(config) {
            leftClick.set(KEY_NONE)
            rightClick.set(KEY_NONE)
            moveForwards.set(KEY_NONE)
            moveLeft.set(KEY_NONE)
            moveRight.set(KEY_NONE)
            moveBackwards.set(KEY_NONE)
            moveJump.set(KEY_NONE)
            moveSneak.set(KEY_NONE)
        }
    }
}
package io.github.frostzie.skyfall.features.garden

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.EventBus
import io.github.frostzie.skyfall.events.KeyDownEvent
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ConditionalUtils.onToggle
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyHeld
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.SignEditScreen
import net.minecraft.client.option.KeyBinding
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object GardenKeybinds {

    private val config get() = SkyFall.feature.garden.keybindConfig

    private var map: Map<KeyBinding, Int> = emptyMap()
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastDuplicatedKeybindWarning = SimpleTimeMark.farPast()
    private var isDuplicated = false

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

        EventBus.listen(KeyDownEvent::class.java) { event ->
        }
        configLoad()
    }

    private fun isEnabled() = config.enabled && inGarden() && toolInHand
    private fun isActive() = isEnabled() && !isDuplicated && !hasGuiOpen() &&
            lastWindowOpenTime.passedSince() > 300.milliseconds

    private fun hasGuiOpen() = MinecraftClient.getInstance().currentScreen != null

    @JvmStatic
    fun isKeyDown(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = map[keyBinding] ?: return

        if (override == KeyboardManager.KEY_NONE) {
            cir.returnValue = false
            return
        }

        cir.returnValue = override.isKeyHeld()
    }

    @JvmStatic
    fun isKeyPressed(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = map[keyBinding] ?: return

        if (override == KeyboardManager.KEY_NONE) {
            cir.returnValue = false
            return
        }

        cir.returnValue = override.isKeyClicked()
    }

    /**
     * Tick handler registered with Fabric client tick events
     */
    private fun onClientTick() {
        val screen = MinecraftClient.getInstance().currentScreen
        if (screen is SignEditScreen) {
            lastWindowOpenTime = SimpleTimeMark.now()
        }

        if (isEnabled() && isDuplicated && lastDuplicatedKeybindWarning.passedSince() > 30.seconds) {
            ChatUtils.messageToChat("§3§lSkyFall§r §8» §eYou aren't allowed having multiple keybinds with the same key!")
            lastDuplicatedKeybindWarning = SimpleTimeMark.now()
        }
    }

    fun configLoad() {
        with(config) {
            onToggle(leftClick, rightClick, moveForwards, moveRight, moveLeft, moveBackwards, moveJump, moveSneak) {
                updateSettings()
            }
            updateSettings()
        }
    }

    private fun calculateDuplicates() {
        isDuplicated = map.values
            .filter { it != KeyboardManager.KEY_NONE }
            .let { values -> values.size != values.toSet().size }
    }

    private fun updateSettings() {
        val minecraft = MinecraftClient.getInstance()
        val options = minecraft.options

        if (options == null) {
            return
        }

        with(config) {
            map = buildMap {
                fun add(keyBinding: KeyBinding, property: io.github.notenoughupdates.moulconfig.observer.Property<Int?>) {
                    put(keyBinding, property.get() ?: KeyboardManager.KEY_NONE)
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
        lastDuplicatedKeybindWarning = SimpleTimeMark.farPast()
        KeyBinding.unpressAll()
    }

    @JvmStatic
    fun resetAll() {
        with(config) {
            leftClick.set(KeyboardManager.KEY_NONE)
            rightClick.set(KeyboardManager.KEY_NONE)
            moveForwards.set(KeyboardManager.KEY_NONE)
            moveLeft.set(KeyboardManager.KEY_NONE)
            moveRight.set(KeyboardManager.KEY_NONE)
            moveBackwards.set(KeyboardManager.KEY_NONE)
            moveJump.set(KeyboardManager.KEY_NONE)
            moveSneak.set(KeyboardManager.KEY_NONE)
        }
    }
}
package io.github.frostzie.skyfall.features.garden

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.FarmingToolTypes
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ConditionalUtils.onToggle
import io.github.frostzie.skyfall.utils.IslandManager
import io.github.frostzie.skyfall.utils.events.IslandChangeListener
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.KeyboardManager.KEY_NONE
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyHeld
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import io.github.frostzie.skyfall.utils.events.IslandChangeEvent
import io.github.frostzie.skyfall.utils.item.ItemUtils
import io.github.notenoughupdates.moulconfig.observer.Property
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.SignEditScreen
import net.minecraft.client.option.KeyBinding
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object GardenKeybinds {

    private val keybinds get() = SkyFall.feature.garden.keybindConfig.customGardenKeybinds
    private val config get() = SkyFall.feature.garden.keybindConfig

    private var map: Map<KeyBinding, Int> = emptyMap()
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastKeybindWarning = SimpleTimeMark.farPast()
    private var isDuplicated = false

    private var wasInGarden = false
    private var wasHoldingTool = false

    /**
     * Checks if the player is currently in the Garden.
     */
    private val inGarden: Boolean
        get() = IslandManager.isOnIsland(IslandType.GARDEN)

    /**
     * Checks if the player is currently holding a farming tool.
     */
    private val toolInHand: Boolean
        get() {
            val player = MinecraftClient.getInstance().player ?: return false
            val heldItem = player.mainHandStack
            val skyblockId = ItemUtils.getSkyblockId(heldItem) ?: return false
            return FarmingToolTypes.getToolType(skyblockId) != null
        }

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { onClientTick() }
        configLoad()

        IslandManager.registerIslandChangeListener(object : IslandChangeListener {
            override fun onIslandChange(event: IslandChangeEvent) {
                if (event.newIsland == IslandType.GARDEN) {
                    updateSettings()
                }
            }
        })
    }

    private fun isEnabled() = config.enabled && inGarden && toolInHand
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

        cir.returnValue = override.isKeyHeld()
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

        cir.returnValue = override.isKeyClicked()
    }

    private fun onClientTick() {
        val screen = MinecraftClient.getInstance().currentScreen
        if (screen is SignEditScreen) {
            lastWindowOpenTime = SimpleTimeMark.now()
        }

        if (isEnabled() && isDuplicated && lastKeybindWarning.passedSince() > 30.seconds) {
            ChatUtils.warning("You aren't allowed having multiple keybinds with the same key!")
            lastKeybindWarning = SimpleTimeMark.now()
        }
        val currentlyInGarden = inGarden
        val currentlyHoldingTool = toolInHand

        if ((currentlyInGarden && !wasInGarden) || (currentlyHoldingTool && !wasHoldingTool)) {
            updateSettings()
        }
        wasInGarden = currentlyInGarden
        wasHoldingTool = currentlyHoldingTool
    }

    fun configLoad() {
        with(keybinds) {
            onToggle(leftClick, rightClick, moveForwards, moveRight, moveLeft, moveBackwards, moveJump, moveSneak) {
                updateSettings()
            }
            updateSettings()
        }
    }

    private fun calculateDuplicates(): List<Int> {
        val keyValues = map.values.filter { it != KEY_NONE }
        val duplicates = keyValues.groupBy { it }.filter { it.value.size > 1 }.keys.toList()
        isDuplicated = duplicates.isNotEmpty()
        return duplicates
    }

    private fun resetDuplicateKeybinds(duplicateKeys: List<Int>) {
        with(keybinds) {
            val allKeybinds = listOf(
                leftClick, rightClick, moveForwards, moveLeft, moveRight, moveBackwards, moveJump, moveSneak
            )

            for (keybind in allKeybinds) {
                if (keybind.get() in duplicateKeys) {
                    keybind.set(KEY_NONE)
                }
            }
        }

        if (duplicateKeys.isNotEmpty()) {
            val keyNames = duplicateKeys.joinToString(", ") { KeyboardManager.getKeyName(it) }
            ChatUtils.warning("Detected duplicate keybinds for: $keyNames")
        }
    }

    private fun updateSettings() {
        val minecraft = MinecraftClient.getInstance()
        val options = minecraft.options

        if (options == null) {
            return
        }

        with(keybinds) {
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

        val duplicateKeys = calculateDuplicates()
        if (duplicateKeys.isNotEmpty()) {
            resetDuplicateKeybinds(duplicateKeys)
            with(keybinds) {
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
        }

        lastKeybindWarning = SimpleTimeMark.farPast()
        KeyBinding.unpressAll()
    }

    /* //TODO: implement with a button in config menu
    fun resetAll() {
        with(keybinds) {
            leftClick.set(KEY_NONE)
            rightClick.set(KEY_NONE)
            moveForwards.set(KEY_NONE)
            moveLeft.set(KEY_NONE)
            moveRight.set(KEY_NONE)
            moveBackwards.set(KEY_NONE)
            moveJump.set(KEY_NONE)
            moveSneak.set(KEY_NONE)
        }
    } */

    private var lastHomeCommandTime = SimpleTimeMark.farPast()

    fun homeHotkey() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (isEnabled()) {
                val homeKey = config.homeHotkey.get()
                if (homeKey != KEY_NONE && homeKey?.isKeyClicked() == true && lastHomeCommandTime.passedSince() >= 500.milliseconds) {
                    val player = client.player
                    if (player != null && inGarden) {
                        player.networkHandler.sendChatCommand("warp garden")
                        lastHomeCommandTime = SimpleTimeMark.now()
                    }
                }
            }
        }
    }
}
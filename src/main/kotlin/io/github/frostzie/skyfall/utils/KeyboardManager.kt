package io.github.frostzie.skyfall.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.option.KeyBinding
import org.apache.commons.lang3.SystemUtils
import org.lwjgl.glfw.GLFW
import net.minecraft.client.util.InputUtil

/**
 * Manages keyboard and mouse input for the mod
 */
object KeyboardManager {

    const val LEFT_MOUSE = -100
    const val RIGHT_MOUSE = -99
    const val MIDDLE_MOUSE = -98

    private var lastClickedMouseButton = -1

    // Store GLFW key constants
    private const val GLFW_KEY_LMETA = GLFW.GLFW_KEY_LEFT_SUPER
    private const val GLFW_KEY_RMETA = GLFW.GLFW_KEY_RIGHT_SUPER
    private const val GLFW_KEY_LMENU = GLFW.GLFW_KEY_LEFT_ALT
    private const val GLFW_KEY_RMENU = GLFW.GLFW_KEY_RIGHT_ALT
    private const val GLFW_KEY_LCONTROL = GLFW.GLFW_KEY_LEFT_CONTROL
    private const val GLFW_KEY_RCONTROL = GLFW.GLFW_KEY_RIGHT_CONTROL
    private const val GLFW_KEY_LSHIFT = GLFW.GLFW_KEY_LEFT_SHIFT
    private const val GLFW_KEY_RSHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT
    private const val GLFW_KEY_BACK = GLFW.GLFW_KEY_BACKSPACE
    private const val GLFW_KEY_V = GLFW.GLFW_KEY_V
    private const val GLFW_KEY_C = GLFW.GLFW_KEY_C
    private const val GLFW_KEY_LAST = GLFW.GLFW_KEY_LAST

    // Track key/character events for GLFW
    private var lastEventKey = 0
    private var lastEventChar = '\u0000'
    private var lastEventKeyState = false

    // Need to register these callbacks during initialization
    fun setupKeyboardCallbacks(window: Long) {
        GLFW.glfwSetKeyCallback(window) { _, key, _, action, _ ->
            lastEventKey = key
            lastEventKeyState = (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        }

        GLFW.glfwSetCharCallback(window) { _, codepoint ->
            lastEventChar = codepoint.toChar()
        }
    }

    // A mac-only key, represents Windows key on windows (but different key code)
    private fun isCommandKeyDown() = GLFW_KEY_LMETA.isKeyHeld() || GLFW_KEY_RMETA.isKeyHeld()

    // Windows: Alt key Mac: Option key
    private fun isMenuKeyDown() = GLFW_KEY_LMENU.isKeyHeld() || GLFW_KEY_RMENU.isKeyHeld()

    private fun isControlKeyDown() = GLFW_KEY_LCONTROL.isKeyHeld() || GLFW_KEY_RCONTROL.isKeyHeld()

    fun isDeleteWordDown() = GLFW_KEY_BACK.isKeyHeld() && if (SystemUtils.IS_OS_MAC) isMenuKeyDown() else isControlKeyDown()

    fun isDeleteLineDown() = GLFW_KEY_BACK.isKeyHeld() && if (SystemUtils.IS_OS_MAC) isCommandKeyDown() else isControlKeyDown() && isShiftKeyDown()

    fun isShiftKeyDown() = GLFW_KEY_LSHIFT.isKeyHeld() || GLFW_KEY_RSHIFT.isKeyHeld()

    fun isPastingKeysDown() = isModifierKeyDown() && GLFW_KEY_V.isKeyHeld()

    fun isCopyingKeysDown() = isModifierKeyDown() && GLFW_KEY_C.isKeyHeld()

    fun isModifierKeyDown() = if (SystemUtils.IS_OS_MAC) isCommandKeyDown() else isControlKeyDown()

    /**
     * Returns the name of the modifier key based on the OS
     */
    fun getModifierKeyName(): String = if (SystemUtils.IS_OS_MAC) "Command" else "Control"

    private data class EventKey(val key: Int, val pressed: Boolean)

    // For modern versions, we handle events through GLFW callbacks
    private fun getEventKey(): EventKey {
        val window = MinecraftClient.getInstance().window.handle

        // If there is a keyboard event from our callback tracking
        if (lastEventKey != 0) {
            return when (lastEventKey) {
                0 -> EventKey(lastEventChar.code + 256, lastEventKeyState)
                else -> EventKey(lastEventKey, lastEventKeyState)
                    .also { lastClickedMouseButton = -1 }
            }
        }

        // Check for mouse events
        for (button in 0..7) { // Check common mouse buttons
            val state = GLFW.glfwGetMouseButton(window, button)
            if (state == GLFW.GLFW_PRESS) {
                val key = button - 100
                lastClickedMouseButton = key
                return EventKey(key, true)
            }
        }

        // Check if previously clicked mouse button is still down
        if (lastClickedMouseButton != -1) {
            val originalButton = lastClickedMouseButton + 100
            if (GLFW.glfwGetMouseButton(window, originalButton) == GLFW.GLFW_PRESS) {
                return EventKey(lastClickedMouseButton, true)
            } else {
                lastClickedMouseButton = -1
            }
        }

        return EventKey(0, false)
    }

    private val clickedKeys = mutableSetOf<Int>()

    /**
     * Called every tick to process keyboard and mouse input
     */
    fun onTick() {
        val minecraft = MinecraftClient.getInstance()
        val currentScreen = minecraft.currentScreen
        if (currentScreen is ChatScreen) return

        val (key, pressed) = getEventKey()
        if (pressed) {
            fireKeyPressEvent(key)
            if (!clickedKeys.contains(key)) {
                fireKeyDownEvent(key)
                clickedKeys.add(key)
            }
        } else clickedKeys.remove(key)
    }

    // Event handling - implement these with your mod's event system
    private fun fireKeyPressEvent(keyCode: Int) {
        // Replace with your mod's event system
        // Example: KeyPressEvent(keyCode).fire()
    }

    private fun fireKeyDownEvent(keyCode: Int) {
        // Replace with your mod's event system
        // Example: KeyDownEvent(keyCode).fire()
    }

    /**
     * Checks if a KeyBinding is currently active
     */
    /**
     * Checks if a KeyBinding is currently active
     */
    fun KeyBinding.isActive(): Boolean {
        try {
            val keyCode = InputUtil.fromTranslationKey(this.boundKeyTranslationKey).code
            if (keyCode.isKeyHeld()) return true
        } catch (e: Exception) {
            println("Error while checking if a key is pressed")
            e.printStackTrace()
            return false
        }
        return this.isPressed
    }

    /**
     * Checks if a key is currently held down
     */
    fun Int.isKeyHeld(): Boolean = when {
        this == 0 -> false
        this < 0 -> {
            val button = this + 100
            GLFW.glfwGetMouseButton(MinecraftClient.getInstance().window.handle, button) == GLFW.GLFW_PRESS
        }
        this >= GLFW_KEY_LAST -> {
            val pressedKey = if (lastEventKey == 0) lastEventChar.code + 256 else lastEventKey
            lastEventKeyState && this == pressedKey
        }
        else -> GLFW.glfwGetKey(MinecraftClient.getInstance().window.handle, this) == GLFW.GLFW_PRESS
    }

    private val pressedKeys = mutableMapOf<Int, Boolean>()

    /**
     * Can only be used once per click, since the function locks itself until the key is no longer held.
     * Do not use in key press events, since it won't be unlocked again.
     */
    fun Int.isKeyClicked(): Boolean = if (this.isKeyHeld()) {
        if (pressedKeys[this] != true) {
            pressedKeys[this] = true
            true
        } else {
            false
        }
    } else {
        pressedKeys[this] = false
        false
    }

    /**
     * Gets a readable name for a key code
     */
    fun getKeyName(keyCode: Int): String = when {
        keyCode < 0 -> "Mouse ${keyCode + 100}"
        else -> InputUtil.fromKeyCode(keyCode, 0).localizedText.string
    }

    /**
     * Utility for working with WASD + jump/sneak keys
     */
    object WasdInputMatrix : Iterable<KeyBinding> {
        operator fun contains(keyBinding: KeyBinding) = when (keyBinding) {
            w, a, s, d, up, down -> true
            else -> false
        }

        val w get() = MinecraftClient.getInstance().options.forwardKey!!
        val a get() = MinecraftClient.getInstance().options.leftKey!!
        val s get() = MinecraftClient.getInstance().options.backKey!!
        val d get() = MinecraftClient.getInstance().options.rightKey!!

        val up get() = MinecraftClient.getInstance().options.jumpKey!!
        val down get() = MinecraftClient.getInstance().options.sneakKey!!

        override fun iterator(): Iterator<KeyBinding> =
            object : Iterator<KeyBinding> {

                var current = w
                var finished = false

                override fun hasNext(): Boolean =
                    !finished

                override fun next(): KeyBinding {
                    if (!hasNext()) throw NoSuchElementException()

                    return current.also {
                        current = when (it) {
                            w -> a
                            a -> s
                            s -> d
                            d -> up
                            up -> down
                            else -> {
                                finished = true
                                throw NoSuchElementException()
                            }
                        }
                    }
                }
            }
    }
}
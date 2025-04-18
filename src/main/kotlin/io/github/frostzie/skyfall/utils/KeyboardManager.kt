package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.events.EventBus
import io.github.frostzie.skyfall.events.KeyDownEvent
import io.github.frostzie.skyfall.events.KeyPressEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.option.KeyBinding
import org.apache.commons.lang3.SystemUtils
import org.lwjgl.glfw.GLFW
import net.minecraft.client.util.InputUtil

/**
 * Manages keyboard and mouse input
 */
object KeyboardManager {

    const val LEFT_MOUSE = -100
    const val RIGHT_MOUSE = -99
    const val MIDDLE_MOUSE = -98
    const val KEY_NONE = -1

    private const val MAX_MOUSE_BUTTON = 7

    private var lastClickedMouseButton = -1

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

    private var lastEventKey = 0
    private var lastEventChar = '\u0000'
    private var lastEventKeyState = false

    private val mouseButtonStates = BooleanArray(MAX_MOUSE_BUTTON)

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { onTick() }
    }

    fun setupKeyboardCallbacks(window: Long) {
        GLFW.glfwSetKeyCallback(window) { _, key, _, action, _ ->
            lastEventKey = key
            lastEventKeyState = (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        }

        GLFW.glfwSetCharCallback(window) { _, codepoint ->
            lastEventChar = codepoint.toChar()
        }

        GLFW.glfwSetMouseButtonCallback(window) { _, button, action, _ ->
            if (button >= 0 && button < MAX_MOUSE_BUTTON) {
                mouseButtonStates[button] = (action == GLFW.GLFW_PRESS)

                val keyCode = -(100 - button)

                if (action == GLFW.GLFW_PRESS) {
                    if (button == 0) {
                        lastClickedMouseButton = keyCode
                    }
                    if (!clickedKeys.contains(keyCode)) {
                        fireKeyDownEvent(keyCode)
                        clickedKeys.add(keyCode)
                    }
                    fireKeyPressEvent(keyCode)
                } else {
                    clickedKeys.remove(keyCode)
                    if (lastClickedMouseButton == keyCode) {
                        lastClickedMouseButton = -1
                    }
                }
            }
        }
    }

    // A mac-only key, represents Windows key on windows (but different key code)
    private fun isCommandKeyDown() = GLFW_KEY_LMETA.isKeyHeld() || GLFW_KEY_RMETA.isKeyHeld()

    // Windows: Alt key Mac: Option key
    fun isMenuKeyDown() = GLFW_KEY_LMENU.isKeyHeld() || GLFW_KEY_RMENU.isKeyHeld()

    fun isControlKeyDown() = GLFW_KEY_LCONTROL.isKeyHeld() || GLFW_KEY_RCONTROL.isKeyHeld()

    fun isDeleteWordDown() = GLFW_KEY_BACK.isKeyHeld() && if (SystemUtils.IS_OS_MAC) isMenuKeyDown() else isControlKeyDown()

    fun isDeleteLineDown() = GLFW_KEY_BACK.isKeyHeld() && if (SystemUtils.IS_OS_MAC) isCommandKeyDown() else isControlKeyDown() && isShiftKeyDown()

    fun isShiftKeyDown() = GLFW_KEY_LSHIFT.isKeyHeld() || GLFW_KEY_RSHIFT.isKeyHeld()

    fun isPastingKeysDown() = isModifierKeyDown() && GLFW_KEY_V.isKeyHeld()

    fun isCopyingKeysDown() = isModifierKeyDown() && GLFW_KEY_C.isKeyHeld()

    fun isModifierKeyDown() = if (SystemUtils.IS_OS_MAC) isCommandKeyDown() else isControlKeyDown()

    fun isRightMouseClicked() = RIGHT_MOUSE.isKeyClicked()

    /**
     * Returns the name of the modifier key based on the OS
     */
    fun getModifierKeyName(): String = if (SystemUtils.IS_OS_MAC) "Command" else "Control"

    private data class EventKey(val key: Int, val pressed: Boolean)

    private fun getEventKey(): EventKey {
        val window = MinecraftClient.getInstance().window.handle

        if (lastEventKey != 0) {
            return when (lastEventKey) {
                0 -> EventKey(lastEventChar.code + 256, lastEventKeyState)
                else -> EventKey(lastEventKey, lastEventKeyState)
            }
        }

        for (button in 0 until MAX_MOUSE_BUTTON) {
            val state = try {
                GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS
            } catch (e: Exception) {
                false
            }

            if (state) {
                val key = -(100 - button)
                return EventKey(key, true)
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
        } else {
            clickedKeys.remove(key)
        }
    }

    private fun fireKeyPressEvent(keyCode: Int) {
        KeyPressEvent(keyCode).post()
    }

    private fun fireKeyDownEvent(keyCode: Int) {
        KeyDownEvent(keyCode).post()
    }

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
    fun Int.isKeyHeld(): Boolean {
        if (this == 0 || this == KEY_NONE) return false

        val minecraft = MinecraftClient.getInstance()
        if (minecraft?.window == null) return false

        if (this < 0) {
            // Mouse button
            val button = Math.abs(this + 100)
            if (button < 0 || button >= MAX_MOUSE_BUTTON) return false

            return try {
                mouseButtonStates[button] ||
                        GLFW.glfwGetMouseButton(minecraft.window.handle, button) == GLFW.GLFW_PRESS
            } catch (e: Exception) {
                false
            }
        }
        else {
            return try {
                GLFW.glfwGetKey(minecraft.window.handle, this) == GLFW.GLFW_PRESS
            } catch (e: Exception) {
                false
            }
        }
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
        keyCode == KEY_NONE -> "None"
        keyCode == LEFT_MOUSE -> "Left Mouse"
        keyCode == RIGHT_MOUSE -> "Right Mouse"
        keyCode == MIDDLE_MOUSE -> "Middle Mouse"
        keyCode < -100 -> "Mouse ${Math.abs(keyCode + 100)}"
        else -> try {
            InputUtil.fromKeyCode(keyCode, 0).localizedText.string
        } catch (e: Exception) {
            "Unknown Key ($keyCode)"
        }
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
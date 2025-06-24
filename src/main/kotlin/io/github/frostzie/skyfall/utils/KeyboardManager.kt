package io.github.frostzie.skyfall.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

// Taken and modified from SkyHanni

/**
 * Manages keyboard and mouse input using Minecraft's InputUtil system
 */
object KeyboardManager {

    private val logger = LoggerProvider.getLogger("keyboardManager")

    val LEFT_MOUSE = InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_LEFT).code
    val RIGHT_MOUSE = InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT).code
    val MIDDLE_MOUSE = InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_MIDDLE).code
    val KEY_NONE = InputUtil.UNKNOWN_KEY.code

    private val MOUSE_BUTTON_RANGE = GLFW.GLFW_MOUSE_BUTTON_1..GLFW.GLFW_MOUSE_BUTTON_LAST
    private val pressedKeys = mutableMapOf<InputUtil.Key, Boolean>()

    /**
     * Enhanced key state checking using InputUtil.
     * This is the key fix: It now uses the raw GLFW method for checking mouse buttons,
     * which is more reliable inside GUIs where Minecraft might otherwise consume the input.
     */
    fun InputUtil.Key.isPressed(): Boolean {
        return try {
            val minecraft = MinecraftClient.getInstance()
            val windowHandle = minecraft?.window?.handle ?: return false

            when (this.category) {
                InputUtil.Type.MOUSE -> {
                    GLFW.glfwGetMouseButton(windowHandle, this.code) == GLFW.GLFW_PRESS
                }
                InputUtil.Type.KEYSYM -> {
                    InputUtil.isKeyPressed(windowHandle, this.code)
                }
                else -> false
            }
        } catch (e: Exception) {
            logger.error("Error checking key state for key: $this", e)
            false
        }
    }

    /**
     * Check if a value is a valid mouse button code.
     */
    private fun isMouseButtonCode(code: Int): Boolean {
        return code in MOUSE_BUTTON_RANGE
    }

    /**
     * Convert GLFW key code to InputUtil.Key
     */
    fun glfwKeyToInputUtil(glfwKey: Int): InputUtil.Key? {
        return try {
            when {
                glfwKey == GLFW.GLFW_KEY_UNKNOWN -> null
                isMouseButtonCode(glfwKey) -> InputUtil.Type.MOUSE.createFromCode(glfwKey)
                else -> InputUtil.Type.KEYSYM.createFromCode(glfwKey)
            }
        } catch (e: Exception) {
            logger.error("Error converting GLFW key $glfwKey to InputUtil.Key", e)
            null
        }
    }

    /**
     * Check if a GLFW key code is currently pressed.
     */
    fun isGlfwKeyPressed(glfwKey: Int): Boolean {
        val inputKey = glfwKeyToInputUtil(glfwKey) ?: return false
        return inputKey.isPressed()
    }

    /**
     * Extension for Int key codes to check if a key is held down.
     */
    fun Int.isKeyHeld(): Boolean {
        return isGlfwKeyPressed(this)
    }

    /**
     * Can only be used once per click, locks until the key is released.
     * This tracks the key state to return true only on the initial press.
     */
    fun InputUtil.Key.isKeyClicked(): Boolean = if (this.isPressed()) {
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
     * GLFW version of isKeyClicked. This is the primary method you should use.
     */
    fun Int.isKeyClicked(): Boolean {
        if (this == GLFW.GLFW_KEY_UNKNOWN) return false
        val inputKey = glfwKeyToInputUtil(this) ?: return false
        return inputKey.isKeyClicked()
    }

    /**
     * Gets a readable name for a GLFW key code.
     */
    fun getKeyName(glfwKey: Int): String = when {
        glfwKey == GLFW.GLFW_KEY_UNKNOWN -> "None"
        isMouseButtonCode(glfwKey) -> {
            when (glfwKey) {
                GLFW.GLFW_MOUSE_BUTTON_LEFT -> "Left Mouse"
                GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "Right Mouse"
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "Middle Mouse"
                else -> "Mouse ${glfwKey + 1}"
            }
        }
        else -> try {
            InputUtil.Type.KEYSYM.createFromCode(glfwKey).localizedText.string
        } catch (e: Exception) {
            logger.error("Error getting key name for GLFW key $glfwKey", e)
            "Unknown Key ($glfwKey)"
        }
    }
}
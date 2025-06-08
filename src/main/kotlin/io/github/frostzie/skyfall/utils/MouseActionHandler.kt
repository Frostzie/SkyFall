package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.events.EventBus
import io.github.frostzie.skyfall.events.KeyDownEvent
import io.github.frostzie.skyfall.events.KeyPressEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

/**
 * Handles attack and use actions for both keyboard and mouse inputs
 */
object MouseActionHandler {
    private val logger = LoggerProvider.getLogger("MouseActionHandler")
    private val attackKey get() = MinecraftClient.getInstance().options.attackKey!!
    private val useKey get() = MinecraftClient.getInstance().options.useKey!!

    private var customAttackHandler: ((Int) -> Boolean)? = null
    private var customUseHandler: ((Int) -> Boolean)? = null

    fun init() {
        EventBus.listen(KeyDownEvent::class.java) { event ->
            handleKeyDown(event.keyCode)
        }

        EventBus.listen(KeyPressEvent::class.java) { event ->
            handleKeyPress(event.keyCode)
        }
    }

    fun setAttackHandler(handler: (Int) -> Boolean) {
        customAttackHandler = handler
    }

    fun setUseHandler(handler: (Int) -> Boolean) {
        customUseHandler = handler
    }

    fun clearHandlers() {
        customAttackHandler = null
        customUseHandler = null
    }

    private fun handleKeyDown(keyCode: Int) {
        when (keyCode) {
            KeyboardManager.LEFT_MOUSE -> {
                customAttackHandler?.invoke(keyCode)
            }
            KeyboardManager.RIGHT_MOUSE -> {
                customUseHandler?.invoke(keyCode)
            }
            else -> {
                val attackKeyCode = getKeyCode(attackKey)
                val useKeyCode = getKeyCode(useKey)

                when (keyCode) {
                    attackKeyCode -> customAttackHandler?.invoke(keyCode)
                    useKeyCode -> customUseHandler?.invoke(keyCode)
                }
            }
        }
    }

    private fun handleKeyPress(keyCode: Int) {
        when (keyCode) {
            KeyboardManager.LEFT_MOUSE -> {
                customAttackHandler?.invoke(keyCode)
            }
            KeyboardManager.RIGHT_MOUSE -> {
                customUseHandler?.invoke(keyCode)
            }
            else -> {
                val attackKeyCode = getKeyCode(attackKey)
                val useKeyCode = getKeyCode(useKey)

                when (keyCode) {
                    attackKeyCode -> customAttackHandler?.invoke(keyCode)
                    useKeyCode -> customUseHandler?.invoke(keyCode)
                }
            }
        }
    }

    private fun getKeyCode(keyBinding: KeyBinding): Int {
        return try {
            InputUtil.fromTranslationKey(keyBinding.boundKeyTranslationKey).code
        } catch (e: Exception) {
            logger.error("Error getting key code for key binding: ${keyBinding.boundKeyTranslationKey}", e)
            KeyboardManager.KEY_NONE
        }
    }
}
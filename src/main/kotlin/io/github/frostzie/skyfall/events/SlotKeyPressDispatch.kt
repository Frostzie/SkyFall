package io.github.frostzie.skyfall.events

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

fun interface SlotKeyPressListener {
    fun onKeyPress(screen: AbstractContainerScreen<*>, slot: Slot, key: Int): Boolean
}

object SlotKeyPressDispatch {
    @JvmField
    var listeners = arrayOfNulls<SlotKeyPressListener>(16)

    @JvmField
    var listenerCount = 0

    fun register(listener: SlotKeyPressListener) {
        if (listenerCount < listeners.size) {
            listeners[listenerCount++] = listener
        }
    }

    @JvmStatic
    fun dispatch(screen: AbstractContainerScreen<*>, slot: Slot, key: Int) : Boolean {
        for (i in 0 until listenerCount) {
            if (listeners[i]?.onKeyPress(screen, slot, key) == true) return true
        }
        return false
    }
}
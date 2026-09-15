package io.github.frostzie.skyfall.events

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

fun interface SlotClickListener {
    fun onSlotClick(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean
}

object SlotClickDispatch {
    @JvmField
    var listeners = arrayOfNulls<SlotClickListener>(16)

    @JvmField
    var listenerCount = 0

    fun register(listener: SlotClickListener) {
        if (listenerCount < listeners.size) {
            listeners[listenerCount++] = listener
        }
    }

    @JvmStatic
    fun dispatch(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean {
        for (i in 0 until listenerCount) {
            if (listeners[i]?.onSlotClick(screen, slot, button) == true) return true
        }
        return false
    }
}

package io.github.frostzie.skyfall.events

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

fun interface SlotHighlightListener {
    fun onDrawHighlight(graphics: GuiGraphicsExtractor, screen: AbstractContainerScreen<*>, slot: Slot)
}

object SlotRenderDispatch {

    @JvmField
    var listeners = arrayOfNulls<SlotHighlightListener>(16)

    @JvmField
    var listenerCount = 0

    fun register(listener: SlotHighlightListener) {
        if (listenerCount < listeners.size) {
            listeners[listenerCount++] = listener
        }
    }

    @JvmStatic
    fun process(graphics: GuiGraphicsExtractor, screen: AbstractContainerScreen<*>, slot: Slot) {
        for (i in 0 until listenerCount) {
            listeners[i]?.onDrawHighlight(graphics, screen, slot)
        }
    }

}
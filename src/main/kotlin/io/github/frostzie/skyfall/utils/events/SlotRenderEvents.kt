package io.github.frostzie.skyfall.utils.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.screen.slot.Slot

object SlotRenderEvents {
    class Before(val context: DrawContext, val slot: Slot)

    private val listeners = mutableListOf<(Before) -> Unit>()

    fun register(listener: (Before) -> Unit) {
        listeners.add(listener)
    }

    fun publish(event: Before) {
        listeners.forEach { it(event) }
    }
}

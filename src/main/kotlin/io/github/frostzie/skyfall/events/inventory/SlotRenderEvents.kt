package io.github.frostzie.skyfall.events.inventory

import net.minecraft.client.gui.DrawContext
import net.minecraft.screen.slot.Slot

object SlotRenderEvents {
    /**
     * The data class for the event, containing the rendering context and the slot.
     */
    class Before(val context: DrawContext, val slot: Slot)

    private val listeners = mutableListOf<(Before) -> Unit>()

    /**
     * Subscribes a listener to this event.
     * The listener will be called every time a slot is about to be rendered.
     */
    fun listen(listener: (Before) -> Unit) {
        listeners.add(listener)
    }

    /**
     * Publishes the event to all subscribed listeners.
     * This should only be called from the relevant Mixin.
     */
    fun publish(event: Before) {
        listeners.forEach { it(event) }
    }
}
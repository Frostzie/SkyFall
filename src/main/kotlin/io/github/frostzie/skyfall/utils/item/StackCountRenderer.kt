package io.github.frostzie.skyfall.utils.item

import io.github.frostzie.skyfall.utils.events.SlotRenderEvent
import net.minecraft.screen.slot.Slot

/**
 * A utility to render fake stack counts on items in slots.
 *
 * Features can register a "provider" function. This utility will listen for
 * slot rendering events and use the provider to determine if a fake stack
 * count should be displayed, overriding the vanilla one.
 */
object StackCountRenderer {
    private val providers = mutableListOf<(Slot) -> Int?>()

    /**
     * Registers this utility to listen for slot rendering events.
     * This should be called once during your mod's main initialization.
     */
    fun initialize() {
        SlotRenderEvent.subscribe(priority = -100) { event ->
            if (event.slot.stack.isEmpty) return@subscribe
            val fakeCount = providers.asSequence()
                .mapNotNull { provider -> provider(event.slot) }
                .firstOrNull()

            if (fakeCount != null) {
                val fakeStack = event.slot.stack.copy()
                fakeStack.count = fakeCount
                event.replaceWith(fakeStack)
            }
        }
    }

    /**
     * Registers a provider function that determines the fake stack count for a slot.
     *
     * @param provider A function that takes a [Slot] and returns an [Int]?
     *                 - Return an [Int] to set the fake stack count.
     *                 - Return `null` if no change should be made for this slot.
     */
    fun registerProvider(provider: (Slot) -> Int?) {
        providers.add(provider)
    }
}
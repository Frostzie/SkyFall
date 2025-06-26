package io.github.frostzie.skyfall.utils.events

import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

/**
 * Event fired when a slot is about to be clicked.
 * Contains all context about the click action.
 */
data class SlotClickEvent(
    val slot: Slot?,
    val slotId: Int,
    val button: Int,
    val actionType: SlotActionType,
    val screenTitle: String,
    val cursorStack: ItemStack,
    val originalStack: ItemStack?
) : Event() {
    companion object {
        fun publish(event: SlotClickEvent): SlotClickEvent {
            EventBus.post(event)
            return event
        }

        fun subscribe(priority: Int = 0, handler: (SlotClickEvent) -> Unit) {
            EventBus.listen(SlotClickEvent::class.java, priority, handler)
        }
    }
}

/**
 * Event for modifying slot appearance during rendering.
 */
data class SlotRenderEvent(
    val slot: Slot,
    val originalStack: ItemStack
) : Event() {
    var replaceWith: ItemStack = originalStack
        private set

    var isHidden: Boolean = false
        private set

    var hideTooltip: Boolean = false
        private set

    /** Replace the item stack shown in this slot. */
    fun replaceWith(itemStack: ItemStack) {
        this.replaceWith = itemStack
    }

    /** Hide the slot completely from rendering. */
    fun hide() {
        this.isHidden = true
    }

    /** Hide only the tooltip for this slot. */
    fun hideTooltip() {
        this.hideTooltip = true
    }

    companion object {
        fun publish(event: SlotRenderEvent): SlotRenderEvent {
            EventBus.post(event)
            return event
        }

        fun subscribe(priority: Int = 0, handler: (SlotRenderEvent) -> Unit) {
            EventBus.listen(SlotRenderEvent::class.java, priority, handler)
        }
    }
}
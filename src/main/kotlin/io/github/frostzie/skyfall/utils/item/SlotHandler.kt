package io.github.frostzie.skyfall.utils.item

import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

class SlotEvent(
    original: ItemStack?,
    val slot: Slot,
    val slotNumber: Int,

    val clickContext: ClickContext? = null
) {
    var replaceWith: ItemStack? = original
        private set

    var isHidden: Boolean = false
        private set

    var hideTooltip: Boolean = false
        private set

    var blockClick: Boolean = false
        private set

    fun replaceWith(itemStack: ItemStack?) {
        this.replaceWith = itemStack
    }

    fun hide() {
        this.isHidden = true
        this.replaceWith = ItemStack.EMPTY
    }

    fun hideTooltip() {
        this.hideTooltip = true
    }

    fun hideComplete() {
        hide()
        hideTooltip()
    }

    fun blockClick() {
        this.blockClick = true
    }

    fun blockAndHide() {
        blockClick()
        hideComplete()
    }
}

data class ClickContext(
    val slotId: Int,
    val button: Int,
    val actionType: SlotActionType,
    val screenTitle: String,
    val cursorStack: ItemStack? = null
)

object SlotHandler {
    private val eventHandlers = mutableListOf<(SlotEvent) -> Unit>()

    fun registerHandler(handler: (SlotEvent) -> Unit) {
        eventHandlers.add(handler)
    }

    private fun processSlot(slot: Slot, clickContext: ClickContext? = null): SlotEvent {
        val event = SlotEvent(slot.stack, slot, slot.index, clickContext)
        eventHandlers.forEach { handler ->
            handler(event)
        }
        return event
    }

    fun shouldHideSlot(slot: Slot): Boolean {
        val event = processSlot(slot)
        return event.isHidden
    }

    fun getReplacementStack(slot: Slot): ItemStack? {
        val event = processSlot(slot)
        return event.replaceWith
    }

    fun shouldHideTooltip(slot: Slot): Boolean {
        val event = processSlot(slot)
        return event.hideTooltip
    }

    fun shouldBlockClick(
        slot: Slot?,
        slotId: Int,
        button: Int,
        actionType: SlotActionType,
        screenTitle: String,
        cursorStack: ItemStack? = null
    ): Boolean {
        if (slot == null) {
            val clickContext = ClickContext(slotId, button, actionType, screenTitle, cursorStack)
            val event = SlotEvent(cursorStack, slot ?: return false, slotId, clickContext)
            eventHandlers.forEach { handler ->
                handler(event)
            }
        }

        val clickContext = ClickContext(slotId, button, actionType, screenTitle, cursorStack)
        val event = processSlot(slot, clickContext)
        return event.blockClick
    }
}
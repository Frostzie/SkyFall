package io.github.frostzie.skyfall.utils.item

import io.github.frostzie.skyfall.events.inventory.SlotClickEvent
import io.github.frostzie.skyfall.events.inventory.SlotRenderEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
//TODO: REMOVE
/**
 * A centralized handler for dispatching all slot-related events.
 * This object is the bridge between game code (injected via Mixins) and our event system.
 * Its single responsibility is to create and publish events with the correct context.
 */
object SlotHandler {
// Taken and modified from Firmament
    /**
     * Called from a mixin to determine if a slot click should be canceled.
     * It creates and publishes a [SlotClickEvent] and returns true if any listener cancels it.
     */
    @JvmStatic
    fun shouldBlockClick(
        slot: Slot?,
        slotId: Int,
        button: Int,
        actionType: SlotActionType,
        screenTitle: Text,
        cursorStack: ItemStack,
        player: PlayerEntity
    ): Boolean {
        val event = SlotClickEvent(
            slot = slot,
            slotId = slotId,
            button = button,
            actionType = actionType,
            screenTitle = screenTitle.string,
            cursorStack = cursorStack,
            originalStack = slot?.stack?.copy()
        )

        SlotClickEvent.publish(event)
        return event.isCancelled
    }

    /**
     * Called from a mixin to determine if a slot should be hidden from rendering.
     */
    @JvmStatic
    fun shouldHideSlot(slot: Slot): Boolean {
        val event = processSlotRender(slot)
        return event.isHidden
    }

    /**
     * Called from a mixin to get a potential replacement item for rendering.
     * Returns null if the stack was not replaced by a listener.
     */
    @JvmStatic
    fun getReplacementStack(slot: Slot): ItemStack? {
        val event = processSlotRender(slot)
        return if (event.replaceWith !== event.originalStack) {
            event.replaceWith
        } else {
            null
        }
    }

    /**
     * Called from a mixin to determine if a slot's tooltip should be hidden.
     */
    @JvmStatic
    fun shouldHideTooltip(slot: Slot): Boolean {
        val event = processSlotRender(slot)
        return event.hideTooltip
    }

    /**
     * Creates and publishes a [SlotRenderEvent] for the given slot.
     * This is a private helper for the public methods above.
     */
    private fun processSlotRender(slot: Slot): SlotRenderEvent {
        val event = SlotRenderEvent(slot, slot.stack.copy())
        SlotRenderEvent.publish(event)
        return event
    }

    @JvmStatic
    fun firePostClickEvent(slot: Slot, originalStack: ItemStack, button: Int, actionType: SlotActionType) {
        val client = MinecraftClient.getInstance()
        val screen = client.currentScreen
        val handler = client.player?.currentScreenHandler

        val event = SlotClickEvent(
            slot = slot,
            slotId = slot.id,
            button = button,
            actionType = actionType,
            screenTitle = screen?.title?.string ?: "",
            cursorStack = handler?.cursorStack ?: ItemStack.EMPTY,
            originalStack = originalStack
        )
        SlotClickEvent.publish(event)
    }
}
package io.github.frostzie.skyfall.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object GuiEvents {

    @JvmField
    val SLOT_RENDER: Event<SlotRenderCallback> =
        EventFactory.createArrayBacked(SlotRenderCallback::class.java) { listeners ->
            SlotRenderCallback { screen, slot ->
                var context = SlotRenderContext()

                for (listener in listeners) {
                    val next = listener.getContext(screen, slot)

                    context = SlotRenderContext(
                        if (context.highlightColor != 0) context.highlightColor else next.highlightColor,
                        context.renderSlot && next.renderSlot,
                        context.renderTooltip && next.renderTooltip,
                    )
                }
                context
            }
        }

    data class SlotRenderContext(
        val highlightColor: Int = 0,
        val renderSlot: Boolean = true,
        val renderTooltip: Boolean = true,
    )

    fun interface SlotRenderCallback {
        fun getContext(screen: AbstractContainerScreen<*>, slot: Slot): SlotRenderContext
    }
}
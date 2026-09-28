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

    @JvmField
    val SLOT_CLICK_KEY: Event<SlotClickKeyCallback> =
        EventFactory.createArrayBacked(SlotClickKeyCallback::class.java) { listeners ->
            SlotClickKeyCallback { screen, slot, button ->
                for (listener in listeners) {
                    if (listener.onSlotKeyClick(screen, slot, button)) return@SlotClickKeyCallback true
                }
                false
            }
        }

    fun interface SlotClickKeyCallback {
        fun onSlotKeyClick(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean
    }


    @JvmField
    val SLOT_CLICK: Event<SlotClickCallback> =
        EventFactory.createArrayBacked(SlotClickCallback::class.java) { listeners ->
            SlotClickCallback { screen, slot, button ->
                for (listener in listeners) {
                    if (!listener.onSlotClick(screen, slot, button)) return@SlotClickCallback false
                }
                true
            }
        }

    fun interface SlotClickCallback {
        fun onSlotClick(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean
    }
}
package io.github.frostzie.skyfall.events.inventory

import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.core.Event
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object TooltipEvents {
    private val logger = LoggerProvider.getLogger("TooltipEvents")
    private val handlers = mutableListOf<TooltipHandler>()

    fun interface TooltipHandler {
        fun onTooltip(stack: ItemStack, lines: MutableList<Text>)
    }

    /**
     * Register a tooltip event handler
     */
    fun register(handler: TooltipHandler) {
        handlers.add(handler)
    }

    /**
     * Call this method from your tooltip rendering mixin/hook
     * This should be called whenever tooltips are about to be rendered
     */
    fun onTooltipRender(stack: ItemStack, lines: MutableList<Text>) {
        handlers.forEach { handler ->
            try {
                handler.onTooltip(stack, lines)
            } catch (e: Exception) {
                logger.error("Tooltip handler threw an exception: ${e.message}")
            }
        }
    }

    /**
     * Event fired just before a tooltip is rendered.
     * Allows for modification of the tooltip lines.
     * This is intended to be used with the central EventBus.
     */
    data class ItemTooltipEvent(
        val stack: ItemStack,
        val lines: MutableList<Text>
    ) : Event()
}
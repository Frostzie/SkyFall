package io.github.frostzie.skyfall.utils.events

import io.github.frostzie.skyfall.utils.LoggerProvider
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
}
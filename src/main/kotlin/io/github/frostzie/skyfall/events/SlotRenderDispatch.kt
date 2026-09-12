package io.github.frostzie.skyfall.events

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

class SlotRenderContext {
    lateinit var graphics: GuiGraphicsExtractor
    lateinit var screen: AbstractContainerScreen<*>
    lateinit var slot: Slot

    var shouldRenderSlot: Boolean = true
    var highlightColor: Int = 0

    fun reset(graphics: GuiGraphicsExtractor, screen: AbstractContainerScreen<*>, slot: Slot) {
        this.graphics = graphics
        this.screen = screen
        this.slot = slot

        this.shouldRenderSlot = true
        this.highlightColor = 0
    }
}


fun interface SlotRenderListener {
    fun onRender(context: SlotRenderContext)
}

object SlotRenderDispatch {

    @JvmField
    val sharedContext = SlotRenderContext()

    @JvmField
    var listeners = arrayOfNulls<SlotRenderListener>(16)

    @JvmField
    var listenerCount = 0

    fun register(listener: SlotRenderListener) {
        if (listenerCount < listeners.size) {
            listeners[listenerCount++] = listener
        }
    }

    @JvmStatic
    fun process(graphics: GuiGraphicsExtractor, screen: AbstractContainerScreen<*>, slot: Slot): SlotRenderContext {

        sharedContext.reset(graphics, screen, slot)
        for (i in 0 until listenerCount) {
            listeners[i]?.onRender(sharedContext)
        }

        return sharedContext
    }
}
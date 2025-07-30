package io.github.frostzie.skyfall.events.render

import io.github.frostzie.skyfall.events.core.Event
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.entity.Entity
import net.minecraft.screen.slot.Slot
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType

/**
 * Base class for all rendering events
 */
abstract class RenderEvent : Event()

/**
 * HUD rendering events
 */
sealed class HudRenderEvent : RenderEvent() {
    abstract val context: DrawContext
    abstract val tickCounter: RenderTickCounter

    data class Pre(
        override val context: DrawContext,
        override val tickCounter: RenderTickCounter
    ) : HudRenderEvent()

    data class Main(
        override val context: DrawContext,
        override val tickCounter: RenderTickCounter
    ) : HudRenderEvent()

    data class Post(
        override val context: DrawContext,
        override val tickCounter: RenderTickCounter
    ) : HudRenderEvent()
}

/**
 * Slot rendering events
 */
sealed class SlotRenderEvent : RenderEvent() {
    abstract val context: DrawContext
    abstract val slot: Slot
    abstract val originalStack: ItemStack

    data class Pre(
        override val context: DrawContext,
        override val slot: Slot,
        override val originalStack: ItemStack
    ) : SlotRenderEvent()

    data class Main(
        override val context: DrawContext,
        override val slot: Slot,
        override val originalStack: ItemStack,
        var replaceWith: ItemStack = originalStack,
        var isHidden: Boolean = false,
        var hideTooltip: Boolean = false
    ) : SlotRenderEvent() {
        fun replaceWith(itemStack: ItemStack) {
            this.replaceWith = itemStack
        }

        fun hide() {
            this.isHidden = true
        }

        fun hideTooltip() {
            this.hideTooltip = true
        }
    }

    data class Post(
        override val context: DrawContext,
        override val slot: Slot,
        override val originalStack: ItemStack
    ) : SlotRenderEvent()
}

sealed class WorldRenderEvent : RenderEvent() {
    data class Pre(val context: DrawContext, val tickCounter: RenderTickCounter) : WorldRenderEvent()
    data class Post(val context: DrawContext, val tickCounter: RenderTickCounter) : WorldRenderEvent()
}

sealed class EntityRenderEvent : RenderEvent() {
    abstract val entity: Entity
    data class Pre(override val entity: Entity, val context: DrawContext) : EntityRenderEvent()
    data class Post(override val entity: Entity, val context: DrawContext) : EntityRenderEvent()
}

/**
 * Slot interaction events
 */
data class SlotClickEvent(
    val slot: Slot?,
    val slotId: Int,
    val button: Int,
    val actionType: SlotActionType,
    val screenTitle: String,
    val cursorStack: ItemStack,
    val originalStack: ItemStack?
) : Event()
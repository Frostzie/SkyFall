package io.github.frostzie.skyfall.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.Entity

object MobEvents {

    @JvmField
    val ENTITY_RENDER: Event<EntityRenderCallback> =
        EventFactory.createArrayBacked(EntityRenderCallback::class.java) { listeners ->
            EntityRenderCallback { entity ->
                for (listener in listeners) {
                    if (!listener.onEntityRender(entity)) return@EntityRenderCallback true
                }
                false
            }
        }

    fun interface EntityRenderCallback {
        fun onEntityRender(entity: Entity): Boolean
    }
}
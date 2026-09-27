package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.GuiEvents
import io.github.frostzie.skyfall.events.GuiEvents.SlotRenderContext
import io.github.frostzie.skyfall.util.ItemUtils
import io.github.frostzie.skyfall.util.skyblock.PetInfoReader
import io.github.frostzie.skyfall.util.skyblock.commonSlotLayout
import io.github.frostzie.skyfall.util.skyblock.isPetMenu
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.SlotClickEvent

object FavoritePets {
    private val config get() = SkyFall.features.pets.favoritePet

    private val favoriteData = config.favoritePets

    fun register() {
        GuiEvents.SLOT_RENDER.register(::onRender)
    }

    private fun onRender(screen: AbstractContainerScreen<*>, slot: Slot): SlotRenderContext {
        if (!config.favEnable || !isPetMenu(screen)) return SlotRenderContext()
        val info = PetInfoReader.read(slot.item) ?: return SlotRenderContext()

        if (info.uuid in favoriteData) {
            return SlotRenderContext(config.favColor.getEffectiveColourRGB())
        }

        return if (config.favOnlyToggle && !info.active) {
            SlotRenderContext(
                renderSlot = false,
                renderTooltip = false
            )
        } else {
            SlotRenderContext()
        }
    }

    @Subscription(event = [SlotClickEvent::class])
    private fun onSlotClick(event: SlotClickEvent) {
        if (!isPetMenu(event.screen) || !config.favEnable) return
        if (event.slot.index !in commonSlotLayout) return
        val uuid = ItemUtils.customDataTag(event.slot.item).getString("uuid").orElse(null) ?: return
        println(event.button)

        if (config.favKey == event.button) {
            if (uuid in favoriteData) {
                favoriteData.remove(uuid)
            } else {
                favoriteData.add(uuid)
            }
        }

        if (config.favOnlyToggle && uuid !in favoriteData) {
            event.cancel()
        }
    }
}

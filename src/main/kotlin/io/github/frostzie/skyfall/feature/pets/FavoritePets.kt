package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.GuiEvents
import io.github.frostzie.skyfall.events.GuiEvents.SlotRenderContext
import io.github.frostzie.skyfall.util.skyblock.PetInfoReader
import io.github.frostzie.skyfall.util.skyblock.commonSlotLayout
import io.github.frostzie.skyfall.util.skyblock.isPetMenu
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object FavoritePets {
    private val config get() = SkyFall.features.pets.favoritePet

    private val favoriteData = config.favoritePets

    fun register() {
        GuiEvents.SLOT_RENDER.register(::onRender)
        GuiEvents.SLOT_CLICK.register(::onSlotClick)
        GuiEvents.SLOT_CLICK_KEY.register(::onSlotKey)
    }

    private fun onRender(screen: AbstractContainerScreen<*>, slot: Slot): SlotRenderContext {
        if (!config.favEnable || !isPetMenu(screen)) return SlotRenderContext()
        if (slot.index !in commonSlotLayout) return SlotRenderContext()
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

    private fun onSlotClick(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean {
        if (!config.favEnable || !isPetMenu(screen)) return true
        if (slot.index !in commonSlotLayout) return true
        val uuid = PetInfoReader.read(slot.item)?.uuid ?: return true

        return (!config.favOnlyToggle || uuid in favoriteData)
    }


    private fun onSlotKey(screen: AbstractContainerScreen<*>, slot: Slot, key: Int): Boolean {
        if (!config.favEnable || !isPetMenu(screen)) return false
        if (slot.index !in commonSlotLayout) return false
        if (key != config.favKey) return false

        val uuid = PetInfoReader.read(slot.item)?.uuid ?: return false

        if (!favoriteData.add(uuid)) favoriteData.remove(uuid)
        return true
    }
}

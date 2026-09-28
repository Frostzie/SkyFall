package io.github.frostzie.skyfall.feature.misc

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.GuiEvents
import io.github.frostzie.skyfall.events.GuiEvents.SlotRenderContext
import io.github.frostzie.skyfall.util.skyblock.commonSlotLayout
import io.github.frostzie.skyfall.util.skyblock.isAbiphoneMenu
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object FavoriteContacts {
    private val config get() = SkyFall.features.misc.abiphone

    private val favoriteData = config.favoriteContacts

    fun register() {
        GuiEvents.SLOT_RENDER.register(::onRender)
        GuiEvents.SLOT_CLICK.register(::onSlotClick)
        GuiEvents.SLOT_CLICK_KEY.register(::onSlotKey)
    }

    private fun onRender(screen: AbstractContainerScreen<*>, slot: Slot): SlotRenderContext {
        if (!config.favEnable || !isAbiphoneMenu(screen)) return SlotRenderContext()
        if (slot.index !in commonSlotLayout) return SlotRenderContext()

        val name = slot.item.customName.toString()

        if (name in favoriteData) {
            return SlotRenderContext(config.favColor.getEffectiveColourRGB())
        }

        return if (config.favOnlyToggle) {
            SlotRenderContext(
                renderSlot = false,
                renderTooltip = false
            )
        } else {
            SlotRenderContext()
        }
    }

    private fun onSlotClick(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean {
        if (!config.favEnable || !isAbiphoneMenu(screen)) return true
        if (slot.index !in commonSlotLayout) return true
        val name = slot.item.customName.toString()

        return (!config.favOnlyToggle || name in favoriteData)
    }

    private fun onSlotKey(screen: AbstractContainerScreen<*>, slot: Slot, key: Int): Boolean {
        if (!config.favEnable || !isAbiphoneMenu(screen)) return false
        if (slot.index !in commonSlotLayout) return false
        if (key != config.favKey) return false

        val name = slot.item.customName.toString()

        if (!favoriteData.add(name)) favoriteData.remove(name)
        return true
    }
}

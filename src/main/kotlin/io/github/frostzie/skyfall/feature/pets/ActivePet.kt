package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.GuiEvents
import io.github.frostzie.skyfall.events.GuiEvents.SlotRenderContext
import io.github.frostzie.skyfall.util.skyblock.PetInfoReader
import io.github.frostzie.skyfall.util.skyblock.isPetMenu
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object ActivePet {
    private val config get() = SkyFall.features.pets

    fun register() {
        GuiEvents.SLOT_RENDER.register(::onRender)
    }

    private fun onRender(screen: AbstractContainerScreen<*>, slot: Slot): SlotRenderContext {
        if (!config.activeEnabled || !isPetMenu(screen)) return SlotRenderContext()

        val info = PetInfoReader.read(slot.item) ?: return SlotRenderContext()

        val color = if (info.active) {
            config.activeColor.getEffectiveColourRGB()
        } else 0

        return SlotRenderContext(color)
    }
}
package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.SlotHighlightListener
import io.github.frostzie.skyfall.util.skyblock.PetInfoReader
import io.github.frostzie.skyfall.util.skyblock.isPetMenu
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object ActivePet : SlotHighlightListener {
    private val config get() = SkyFall.features.pets

    override fun onDrawHighlight(
        graphics: GuiGraphicsExtractor,
        screen: AbstractContainerScreen<*>,
        slot: Slot
    ) {
        if (!config.activeEnabled || !isPetMenu(screen)) return

        val info = PetInfoReader.read(slot.item) ?: return
        if (info.active) {
            val color = config.activeColor.getEffectiveColourRGB()

            val x = slot.x
            val y = slot.y
            graphics.fill(x, y, x+ 16, y + 16, color)
        }
    }
}
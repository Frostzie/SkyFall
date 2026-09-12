package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.SlotRenderContext
import io.github.frostzie.skyfall.events.SlotRenderListener
import io.github.frostzie.skyfall.util.skyblock.PetInfoReader
import io.github.frostzie.skyfall.util.skyblock.isPetMenu

object ActivePet : SlotRenderListener {
    private val config get() = SkyFall.features.pets

    override fun onRender(context: SlotRenderContext) {

        if (!config.activeEnabled || !isPetMenu(context.screen)) return

        val info = PetInfoReader.read(context.slot.item) ?: return
        if (info.active) {
            val color = config.activeColor.getEffectiveColourRGB()

            context.highlightColor = color
        }
    }
}
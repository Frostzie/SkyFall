package io.github.frostzie.skyfall.feature.misc

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.SlotClickListener
import io.github.frostzie.skyfall.events.SlotRenderContext
import io.github.frostzie.skyfall.events.SlotRenderListener
import io.github.frostzie.skyfall.util.skyblock.commonSlotLayout
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.inventory.Slot

object FavoriteContacts : SlotRenderListener, SlotClickListener {
    private val config get() = SkyFall.features.misc.abiphone

    private val favoriteData = config.favoriteContacts

    override fun onRender(context: SlotRenderContext) {
        if (!config.favEnable || !isAbiphoneMenu(context.screen)) return

        val name = context.slot.item.customName.toString()

        if (name in favoriteData) {
            val color = config.favColor.getEffectiveColourRGB()

            context.highlightColor = color
        } else if (config.favOnlyToggle) {
            context.shouldRenderSlot = false
            context.shouldRenderTooltips = false
        }
    }

    override fun onSlotClick(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean {
        if (!isAbiphoneMenu(screen) || !config.favEnable) return false
        if (slot.index !in commonSlotLayout) return false
        if (button == 256 || button == 69) return false // for escape and e key to allow leaving the menu. //TODO: there must be a better way
        val name = slot.item.customName.toString()

        if (config.favKey == button) {
            if (name in favoriteData) {
                favoriteData.remove(name)
            } else {
                favoriteData.add(name)
            }
        }

        return (config.favOnlyToggle && name !in favoriteData)
    }

    private fun isAbiphoneMenu(screen: AbstractContainerScreen<*>?): Boolean {
        if (screen !is ContainerScreen) return false
        val title = screen.title.string
        // Blocks out the levels abiphone menu from working idk if there is another menu containing Abiphone tho
        return title.contains("Abiphone") && !title.contains("Abiphone Contacts")
    }
}

package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.SkyFall.Companion.configManager
import io.github.frostzie.skyfall.events.SlotRenderListener
import io.github.frostzie.skyfall.events.SlotClickListener
import io.github.frostzie.skyfall.events.SlotRenderContext
import io.github.frostzie.skyfall.util.ItemUtils
import io.github.frostzie.skyfall.util.skyblock.PetInfoReader
import io.github.frostzie.skyfall.util.skyblock.commonSlotLayout
import io.github.frostzie.skyfall.util.skyblock.isPetMenu
import io.github.frostzie.skyfall.util.update.FavPetsConfig
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object FavoritePets : SlotRenderListener, SlotClickListener {
    private val config get() = SkyFall.features.pets.favoritePet

    private val favoriteData = config.favoritePets

    init {
        FavPetsConfig.load()
        configManager.save()
    }

    override fun onRender(context: SlotRenderContext) {
        if (!config.favEnable || !isPetMenu(context.screen)) return
        val info = PetInfoReader.read(context.slot.item) ?: return

        if (info.uuid in favoriteData) {
            val color = config.favColor.getEffectiveColourRGB()

            context.highlightColor = color
        } else if (config.favOnlyToggle && !info.active) {
            context.shouldRenderSlot = false
            context.shouldRenderTooltips = false
        }
    }

    override fun onSlotClick(screen: AbstractContainerScreen<*>, slot: Slot, button: Int): Boolean {
        if (!isPetMenu(screen) || !config.favEnable) return false
        if (slot.index !in commonSlotLayout) return false
        if (button == 256 || button == 69) return false // for escape and e key to allow leaving the menu. //TODO: there must be a better way
        val uuid = ItemUtils.customDataTag(slot.item).getString("uuid").orElse(null) ?: return false

        if (config.favKey == button) {
            if (uuid in favoriteData) {
                favoriteData.remove(uuid)
            } else {
                favoriteData.add(uuid)
            }
        }

        return (config.favOnlyToggle && uuid !in favoriteData)
    }
}

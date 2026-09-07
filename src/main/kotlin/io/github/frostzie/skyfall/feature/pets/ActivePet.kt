package io.github.frostzie.skyfall.feature.pets

import com.google.gson.JsonParser
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.SlotHighlightListener
import io.github.frostzie.skyfall.util.ItemUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

object ActivePet : SlotHighlightListener {
    private val config get() = SkyFall.features.pets

    private fun isPetMenu(screen: AbstractContainerScreen<*>?): Boolean {
        if (screen !is ContainerScreen) return false
        val title = screen.title.string
        return title == "Pets" || Regex("""\(\d+/\d+\) Pets""").matches(title)
    }

    private fun isActivePet(stack: ItemStack): Boolean {
        val petInfo = getPetInfo(stack) ?: return false
        return try {
            JsonParser.parseString(petInfo).asJsonObject
                .get("active")?.asBoolean ?: false
        } catch (_: Exception) {
            false
        }
    }

    private fun getPetInfo(stack: ItemStack): String? =
        ItemUtils.customDataTag(stack)?.getString("petInfo")?.orElse(null)

    override fun onDrawHighlight(
        graphics: GuiGraphicsExtractor,
        screen: AbstractContainerScreen<*>,
        slot: Slot
    ) {
        if (!config.activeEnabled || !isPetMenu(screen)) return

        if (isActivePet(slot.item)) {
            val color = config.activeColor.getEffectiveColourRGB()

            val x = slot.x
            val y = slot.y
            graphics.fill(x, y, x+ 16, y + 16, color)
        }
    }
}
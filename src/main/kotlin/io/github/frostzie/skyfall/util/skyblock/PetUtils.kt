package io.github.frostzie.skyfall.util.skyblock

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.ItemStack

private val PetMenuRegex = Regex("""\(\d+/\d+\) Pets""")

val petSlotIds = setOf(
    10..16,
    19..25,
    28..34,
    37..43
).flatten().toSet()

fun isPetMenu(screen: AbstractContainerScreen<*>?): Boolean {
    if (screen !is ContainerScreen) return false
    val title = screen.title.string
    return title == "Pets" || PetMenuRegex.matches(title)
}
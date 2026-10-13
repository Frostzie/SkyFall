package io.github.frostzie.skyfall.util.skyblock

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen

val commonSlotLayout = setOf(
    10..16,
    19..25,
    28..34,
    37..43
).flatten().toSet()

//TODO: remove
fun isAbiphoneMenu(screen: AbstractContainerScreen<*>?): Boolean {
    if (screen !is ContainerScreen) return false
    val title = screen.title.string
    // Blocks out the levels abiphone menu from working idk if there is another menu containing Abiphone tho
    // edit: Yes there was indeed the bit shops exists how could I forget about such a important part of the game...
    return title.contains("Abiphone") && !title.contains("Abiphone Contacts") && !title.contains("Bits Shop")
}
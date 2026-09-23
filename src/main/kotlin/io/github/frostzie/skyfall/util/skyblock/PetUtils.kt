package io.github.frostzie.skyfall.util.skyblock

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen

fun isPetMenu(screen: AbstractContainerScreen<*>?): Boolean {
    if (screen !is ContainerScreen) return false
    val title = screen.title.string
    return title == "Pets" || title.endsWith(") Pets")
}
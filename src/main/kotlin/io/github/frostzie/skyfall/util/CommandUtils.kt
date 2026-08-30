package io.github.frostzie.skyfall.util

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object CommandUtils {

    fun clientMessage(message: String) {
        val prefix = "§8[§bSkyFall§8]§r" //TODO: gradient
        val text = Component.literal("$prefix $message")
        Minecraft.getInstance().gui.hud.chat.addClientSystemMessage(text)
    }
}



package io.github.frostzie.skyfall.util

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ChatUtils {
    private const val PREFIX =  "§8[§bSkyFall§8]§r" //TODO: Gradient eventually
    private val mc = Minecraft.getInstance()

    fun clientMessage(message: String) {
        val text = Component.literal("$PREFIX $message")
        mc.gui.hud.chat.addClientSystemMessage(text)
    }

    fun sendCommand(command: String) {
        mc.execute { mc.player?.connection?.sendCommand(command) }
    }
}



package io.github.frostzie.skyfall.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.network.chat.Component

private const val PREFIX = "§8[§bSkyFall§8]§r" //TODO: Gradient eventually
private val mc = Minecraft.getInstance()

fun modMessage(message: String, prefix: Boolean = true) {
    val text = Component.literal("${if (prefix) PREFIX else ""} $message")
    mc.gui.hud.chat.addClientSystemMessage(text)
}

fun modMessage(message: Component, prefix: Boolean = true) {
    val text = Component.literal(if (prefix) PREFIX else "").append(message)
    mc.gui.hud.chat.addClientSystemMessage(text)
}

fun sendCommand(command: String) {
    mc.execute { mc.player?.connection?.sendCommand(command) }
}

fun getChatSeparator(color: String = "§f", style: String = "§m", char: String = " "): String {
    return ChatComponent.getWidth(mc.options.chatWidth().get()).let {
        color + style + char.repeat(it / mc.font.width(char))
    }
}


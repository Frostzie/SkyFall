package io.github.frostzie.skyfall.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object ChatUtils {
    fun messageToChat(message: String) {
        MinecraftClient.getInstance().player?.sendMessage(Text.of(message), false)
    }
}
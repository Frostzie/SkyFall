package io.github.frostzie.skyfall.features.dungeon

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

//TODO: keybind not being registered when pressed

object RequeueKey {
    fun load() {
        if (SkyFall.feature.dungeon.keyRequeue != GLFW.GLFW_KEY_UNKNOWN) {
            MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("/instancerequeue")
            ChatUtils.messageToChat("Requeueing")
        }
    }
}
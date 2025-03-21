package io.github.frostzie.skyfall.features.dungeon

import com.mojang.brigadier.context.CommandContext
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

//TODO: Rework this fully

object RequeueKey {
    fun load() {
        if (SkyFall.feature.dungeon.keyRequeue != GLFW.GLFW_KEY_UNKNOWN) {
            MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand("/instancerequeue")
            ChatUtils.messageToChat("Requeueing")
        }
    }
}


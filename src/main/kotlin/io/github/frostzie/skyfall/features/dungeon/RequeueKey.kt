package io.github.frostzie.skyfall.features.dungeon

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

object RequeueKey {
    private var lastCommandTime = SimpleTimeMark.farPast()

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val requeueKey = SkyFall.feature.dungeon.requeueKey

                if (requeueKey != GLFW.GLFW_KEY_UNKNOWN) {
                    if (requeueKey.isKeyClicked() && lastCommandTime.passedSince() >= 350.milliseconds) {
                        val player = client.player
                        if (player != null) {
                            player.networkHandler.sendChatCommand("instancerequeue")
                            ChatUtils.messageToChat("§aRequeueing").send()
                            lastCommandTime = SimpleTimeMark.now()
                        }
                    }
                }
            }
        }
    }
}
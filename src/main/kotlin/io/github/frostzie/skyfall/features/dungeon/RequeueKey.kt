package io.github.frostzie.skyfall.features.dungeon

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

@Feature(name = "Requeue Key")
object RequeueKey : IEventFeature {
    override var isRunning = false
    private var lastCommandTime = SimpleTimeMark.farPast()
    private val requeueKey get() = SkyFall.feature.dungeon.requeueKey

    override fun shouldLoad(): Boolean {
        return requeueKey != GLFW.GLFW_KEY_UNKNOWN
    }

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
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

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }
}
package io.github.frostzie.skyfall.features.chat.filters

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.util.Formatting

// Taken and modified from Skyblocker
@Feature(name = "Hide Watchdog Messages")
object HideWatchdogMessage : IFeature {

    override var isRunning = false

    init {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (!isRunning || overlay) {
                return@register true
            }

            val messageString = Formatting.strip(message.string)
            if (isWatchdogAnnouncement(messageString)) {
                return@register false
            }

            return@register true
        }
    }

    override fun shouldLoad(): Boolean {
        return SkyFall.feature.chat.chatFilters.hideWatchdog
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }

    private fun isWatchdogAnnouncement(message: String?): Boolean {
        if (message == null) return false

        return message.contains("[WATCHDOG ANNOUNCEMENT]") ||
                (message.contains("Watchdog has banned") && message.contains("players in the last 7 days")) ||
                (message.contains("Staff have banned an additional") && message.contains("in the last 7 days")) ||
                message.contains("Blacklisted modifications are a bannable offense!")
    }
}
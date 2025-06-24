package io.github.frostzie.skyfall.features.chat.filters

import io.github.frostzie.skyfall.SkyFall
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.util.Formatting

// Taken and modified from Skyblocker
object HideWatchdogMessage {
    private val config get() = SkyFall.feature.chat.chatFilters.hideWatchdog

    fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (overlay) {
                return@register true
            }

            if (config) {
                val messageString = Formatting.strip(message.string)
                if (isWatchdogAnnouncement(messageString)) {
                    return@register false
                }
            }

            return@register true
        }
    }

    private fun isWatchdogAnnouncement(message: String?): Boolean {
        if (message == null) return false

        return message.contains("[WATCHDOG ANNOUNCEMENT]") ||
                (message.contains("Watchdog has banned") && message.contains("players in the last 7 days")) ||
                (message.contains("Staff have banned an additional") && message.contains("in the last 7 days")) ||
                message.contains("Blacklisted modifications are a bannable offense!")
    }
}
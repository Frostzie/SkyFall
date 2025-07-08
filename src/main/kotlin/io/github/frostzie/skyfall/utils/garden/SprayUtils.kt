package io.github.frostzie.skyfall.utils.garden

import io.github.frostzie.skyfall.data.GardenPlot
import io.github.frostzie.skyfall.data.GardenPlots
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object SprayUtils {

    private val sprayedPlots: MutableMap<GardenPlot, Long> = mutableMapOf()
    private const val DEFAULT_SPRAY_DURATION_MIN = 30

    init {
        ClientReceiveMessageEvents.ALLOW_GAME.register(::onReceiveMessage)
    }

    /**
     * Callback for incoming chat messages.
     * It strips formatting from the message and processes only non-overlay chat.
     */
    private fun onReceiveMessage(message: Text, overlay: Boolean): Boolean {
        if (overlay) return false
        val cleanMessage = Formatting.strip(message.string)
        println("SprayUtils: Received chat message: $cleanMessage")
        processChatMessage(cleanMessage.toString())
        return true
    }

    /**
     * Returns a list of currently sprayed plots by removing any that have expired.
     */
    fun getSprayedPlots(): List<GardenPlot> {
        val currentTime = System.currentTimeMillis()
        sprayedPlots.entries.removeIf { currentTime >= it.value }
        return sprayedPlots.keys.toList()
    }

    /**
     * Processes a chat message to detect a spray event.
     * Uses a regex to extract the plot number and marks that plot as sprayed with its own timer.
     *
     * Expected format: "SPRAYONATOR! You sprayed Plot - [number] ..."
     */
    fun processChatMessage(message: String) {
        val chatRegex = """SPRAYONATOR!\s+You sprayed Plot - (\d+).*""".toRegex()
        val match = chatRegex.find(message)
        match?.groupValues?.get(1)?.toIntOrNull()?.let { plotNumber ->
            GardenPlots.allPlots.find { it.id == plotNumber }?.let { plot ->
                val expiration = System.currentTimeMillis() + DEFAULT_SPRAY_DURATION_MIN * 60 * 1000
                sprayedPlots[plot] = expiration
            }
        }
    }
}
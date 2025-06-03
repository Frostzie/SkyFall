package io.github.frostzie.skyfall.features.chat.filters

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.IslandManager
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.util.Formatting

object HideSkyMallChanges {
    private val config get() = SkyFall.feature.chat.chatFilters.hideSkyMallChange
    private val miningIslands = listOf(
        IslandType.GOLD_MINE,
        IslandType.DEEP_CAVERNS,
        IslandType.DWARVEN_MINES,
        IslandType.CRYSTAL_HOLLOWS,
        IslandType.MINESHAFT,
    )

    fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (overlay) {
                return@register true
            }

            if (config) {
                val messageString = Formatting.strip(message.string)
                if (isSkyMallChange(messageString) && !inMiningIsland()) {
                    return@register false
                }
            }

            return@register true
        }
    }

    private fun isSkyMallChange(message: String?): Boolean {
        if (message == null) return false

        return message.contains("New day! Your Sky Mall buff changed!") ||
                (message.contains("New buff: ")) ||
                (message.contains("You can disable this messaging by toggling Sky Mall in your /hotm!"))
    }

    private fun inMiningIsland(): Boolean {
        return miningIslands.any { IslandManager.isOnIsland(it) }
    }
}
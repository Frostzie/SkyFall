package io.github.frostzie.skyfall.features.chat.filters

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import io.github.frostzie.skyfall.utils.IslandManager
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting

@Feature(name = "Hide SkyMall Changes")
object HideSkyMallChanges : IFeature {

    override var isRunning = false

    private val skyMallPhrases = setOf(
        "New day! Your Sky Mall buff changed!",
        "New buff: ",
        "You can disable this messaging by toggling Sky Mall in your /hotm!"
    )

    private val miningIslands = setOf(
        IslandType.GOLD_MINE,
        IslandType.DEEP_CAVERNS,
        IslandType.DWARVEN_MINES,
        IslandType.CRYSTAL_HOLLOWS,
        IslandType.MINESHAFT,
    )

    init {
        ClientReceiveMessageEvents.ALLOW_GAME.register(::shouldAllowMessage)
    }

    override fun shouldLoad(): Boolean {
        return SkyFall.feature.chat.chatFilters.hideSkyMallChange
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }

    private fun shouldAllowMessage(message: Text, overlay: Boolean): Boolean {
        if (!isRunning || overlay || inMiningIsland()) {
            return true
        }

        if (isSkyMallChange(message.string)) {
            return false
        }

        return true
    }

    private fun isSkyMallChange(message: String?): Boolean {
        return Formatting.strip(message)?.let { cleanMessage ->
            skyMallPhrases.any { phrase -> cleanMessage.contains(phrase) }
        } ?: false
    }

    private fun inMiningIsland(): Boolean {
        return miningIslands.any { IslandManager.isOnIsland(it) }
    }
}
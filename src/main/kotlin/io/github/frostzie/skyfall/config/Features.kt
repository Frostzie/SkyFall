package io.github.frostzie.skyfall.config

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.config.features.chat.ChatConfig
import io.github.frostzie.skyfall.config.features.dungeon.DungeonConfig
import io.github.frostzie.skyfall.config.features.garden.GardenConfig
import io.github.frostzie.skyfall.config.features.gui.GuiConfig
import io.github.frostzie.skyfall.config.features.misc.MiscFeatures
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.Social
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation

open class Features : Config() {
    //TODO: add version change support and better color for title
    override fun getTitle(): String {
        return "SkyFall§r by §bFrostzie§r, config by §5Moulberry §rand §5nea89"

    }


    // TODO: Make socials open directly in the browser
    private val discord = MyResourceLocation("skyfall", "social/discord.png")
    private val github = MyResourceLocation("skyfall", "social/github.png")
    private val patreon = MyResourceLocation("skyfall", "social/patreon.png")

        override fun getSocials(): List<Social?>? {
        return listOf(
            Social.forLink("Discord", discord, "https://discord.gg/qZ885qTvkx"),
            Social.forLink("GitHub", github, "https://github.com/Frostzie/SkyFall"),
            Social.forLink("Patreon", patreon, "https://www.patreon.com/Frostzie"),
        )
    }


    //override fun saveNow() {
    //    SkyFall.configManager.saveConfig(Features, "close-gui")
    //}




    //  Config Start
    
    @Expose
    @Category(name = "Gui", desc = "Gui Settings")
    var gui: GuiConfig = GuiConfig()

    @JvmField
    @Expose
    @Category(name = "Chat", desc = "Chat related features.")
    var chat: ChatConfig = ChatConfig()

    @Expose
    @Category(name = "Dungeon", desc = "Dungeon related features.")
    var dungeon: DungeonConfig = DungeonConfig()

    @Expose
    @Category(name = "Garden", desc = "Garden related features.")
    var garden: GardenConfig = GardenConfig()

    @Expose
    @Category(name = "Misc", desc = "Miscellaneous features.")
    var miscFeatures: MiscFeatures = MiscFeatures()


    //  Config End
}
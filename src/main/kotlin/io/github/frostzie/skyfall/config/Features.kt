package io.github.frostzie.skyfall.config

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.features.Misc
import io.github.frostzie.skyfall.config.features.Pets
import io.github.frostzie.skyfall.config.features.MobHighlight
import io.github.frostzie.skyfall.config.features.mining.Mining
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.Social
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

class Features : Config() {

    @Expose
    @Category(name = "Mob Highlight", desc = "Custom highlight for mobs")
    val hitbox: MobHighlight = MobHighlight()

    @Expose
    @Category(name = "Pets", desc = "")
    val pets: Pets = Pets()

    @Expose
    @Category(name = "Misc", desc = "")
    val misc: Misc = Misc()

    @Expose
    @Category(name = "Mining", desc = "")
    val mining: Mining = Mining()

    override fun getTitle(): StructuredText {
        return StructuredText.of("SkyFall by Frostzie, config by §5Moulberry §rand §5nea89")
    }

    private val githubIcon = MyResourceLocation("skyfall", "social/github.png")
    private val discordIcon = MyResourceLocation("skyfall", "social/discord.png")

    private val socials = listOf(
        Social.forLink(StructuredText.of("Discord"), discordIcon, "https://discord.gg/qZ885qTvkx"),
        Social.forLink(StructuredText.of("Github"), githubIcon, "https://github.com/Frostzie/SkyFall")
    )

    override fun getSocials(): List<Social> = socials

    override fun saveNow() = SkyFall.configManager.save()
}
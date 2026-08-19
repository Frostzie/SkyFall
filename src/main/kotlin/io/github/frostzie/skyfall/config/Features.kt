package io.github.frostzie.skyfall.config

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.features.Pets
import io.github.frostzie.skyfall.config.features.MobHighlight
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

class Features : Config() {

    @Expose
    @Category(name = "Mob Highlight", desc = "Custom highlight for mobs")
    val hitbox: MobHighlight = MobHighlight()

    @Expose
    @Category(name = "Pets", desc = "")
    val pets: Pets = Pets()

    override fun getTitle(): StructuredText {
        return StructuredText.of("SkyFall by Frostzie, config by §5Moulberry §rand §5nea89")
    }

    override fun saveNow() {
        SkyFall.configManager.save()
    }
}
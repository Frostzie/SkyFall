package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

// §cAutopet §eequipped your §7[Lvl 200] §8[§6240§8§4✦§8] §6Golden Dragon§e! §a§lVIEW RULE
// Autopet equipped your [Lvl 200] [240✦] Golden Dragon! VIEW RULE
// Autopet equipped your [Lvl 100] Enderman! VIEW RULE

//TODO: I have no idea how this works on non maxed cosmetic pets so if someone figures that out lmk
object Autopet {

    private val config get() = SkyFall.features.pets.autoPet

    private val COLOR_REGEX = Regex("§.") //TODO: Move this to color utils when it is made

    fun load() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            if (!config.enabled) return@register true
            val clean = message.string.replace(COLOR_REGEX, "").trim()
            
            val isAutopetMsg = clean.startsWith("Autopet equipped your [Lvl ")
            if (!isAutopetMsg) return@register true

            // Tried this with Regex on the first try, resulted horribly so trying out substring and I think it is okay
            val baseLevel = clean.substringAfter("[Lvl ").substringBefore("]").toIntOrNull() ?: 0
            val cosmeticLevel = clean.substringAfterLast("[").substringBefore("✦]").toIntOrNull() ?: 0
            val pet = clean.substringAfterLast("] ").substringBefore("!")
            val cosmeticPet = clean.contains("✦]")

            val rarity = message.string.substringAfterLast("] §").first()
            
            val totalLevel = if (cosmeticPet) {
                baseLevel + cosmeticLevel
            } else baseLevel
            
            if (config.combineLevel && !config.shorten) {
                val combinedMsg = Component.literal("§cAutopet §eequipped your §7[Lvl $totalLevel] §$rarity$pet§e!")
                Minecraft.getInstance().gui.hud.chat.addClientSystemMessage(combinedMsg)
                return@register false

            } else if (config.shorten) { // Pretty sure this setting is also in skyhanni I was basing it off something I had enabled
                val shortMsg = Component.literal("§4[§cAutopet§4] §7[Lvl $totalLevel] §$rarity$pet§e!")
                Minecraft.getInstance().gui.hud.chat.addClientSystemMessage(shortMsg)
                return@register false
            }

            true
        }
    }
}

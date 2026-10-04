package io.github.frostzie.skyfall.feature.mining.shaft

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.util.ChatUtils
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.CorpseSpawnEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.MineshaftEnteredEvent
import tech.thatgravyboat.skyblockapi.api.profile.party.PartyAPI

object ShaftPartyMessage {
    private val config = SkyFall.features.mining.shaft

    fun register() {
        SkyBlockAPI.eventBus.register(this)
    }

    @Subscription
    fun enterMineshaft(event: MineshaftEnteredEvent) {
        if (!config.partyAnnouncer || !PartyAPI.inParty) return

        val type = event.type
        val hasCrystal = event.isCrystal

        val text = "Shaft Type: $type" + if (hasCrystal) ", Crystal: $type" else ""

        ChatUtils.sendCommand("pc $text")
    }

    @Subscription
    fun detectCorpse(event: CorpseSpawnEvent) {
        if (!config.partyAnnouncer || !PartyAPI.inParty) return

        val corpses = event.corpses
            .groupingBy { it.type.name }
            .eachCount()
            .entries
            .joinToString(", ") { "${it.key}: ${it.value}" }

        ChatUtils.sendCommand("pc Corpses: $corpses")
    }
}
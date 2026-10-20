package io.github.frostzie.skyfall.feature.mining.shaft

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.util.LoggerProvider
import io.github.frostzie.skyfall.util.getChatSeparator
import io.github.frostzie.skyfall.util.isColored
import io.github.frostzie.skyfall.util.modMessage
import io.github.frostzie.skyfall.util.sendCommand
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.CorpseSpawnEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.MineshaftEnteredEvent
import tech.thatgravyboat.skyblockapi.api.profile.party.PartyAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.capitalize
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick

// §9Party §8> §b[MVP§8+§b] Frostzie§f: SkyFall > Shaft: Umber ; Corpses: LAPIS: 1, UMBER: 1, TUNGSTEN: 1

object ShaftPartyMessage {
    private val logger = LoggerProvider.getLogger("ShaftPartyMessage")
    private val config = SkyFall.features.mining.shaft

    private val messageRegex = Regex("^Party > (?:\\[.+] )?[a-zA-Z0-9_]+: ${Regex.escape("SkyFall > ")}(.+)$")

    private var type: String? = null
    private var hasCrystal: Boolean = false

    fun register() {
        SkyBlockAPI.eventBus.register(this)
    }

    @Subscription
    fun enterMineshaft(event: MineshaftEnteredEvent) {
        if (!config.partyAnnouncer || !PartyAPI.inParty) return
        type = event.type?.toString()
        hasCrystal = event.isCrystal
    }

    @Subscription
    fun detectCorpse(event: CorpseSpawnEvent) {
        if (!config.partyAnnouncer || !PartyAPI.inParty) return

        val corpses = event.corpses
            .groupingBy { it.type }
            .eachCount()
            .entries
            .joinToString(", ") { "${it.key.name}: ${it.value}" }

        val message = buildList {
            type?.let { add("Shaft: $type" + if (hasCrystal) " + Crystal" else "") }
            add("Corpses: $corpses")

            type = null
        }.joinToString(" ; ")

        sendCommand("pc SkyFall > $message")
    }

    @Subscription
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (!config.partyAnnouncer || !PartyAPI.inParty) return
        val raw = messageRegex.matchEntire(event.text)?.groupValues?.get(1) ?: return
        val pretty = beautify(raw) ?: return

        event.cancel()
        modMessage(pretty, false)

        //TODO: Add !warp to skyfall and have some extra functionality so only if leader in shaft could warp
        if (config.clickableMessage) {
            modMessage(
                Component.literal("§a§lClick to Warp!§r")
                    .onClick { sendCommand("pc !warp") },
                false
            )
        }
    }

    private fun beautify(message: String): MutableComponent? {
        var shaft: String? = null
        var crystal = false
        val corpses = mutableListOf<Pair<String, Int>>()

        for (part in message.split(" ; ")) {
            when {
                part.startsWith("Shaft: ") -> {
                    val value = part.substring("Shaft: ".length)
                    crystal = value.endsWith(" + Crystal")
                    shaft = value.removeSuffix(" + Crystal")
                }

                part.startsWith("Corpses: ") ->
                    part.substring("Corpses: ".length)
                        .split(", ")
                        .forEach { item ->
                            val split = item.split(": ")
                            val count = split.getOrNull(1)?.toIntOrNull() ?: return@forEach
                            corpses.add(Pair(split[0], count))
                        }
            }
        }

        if (shaft == null && corpses.isEmpty()) {
            logger.error("Shaft message unparsable, message: $message")

            return null
        }

        val block = Component.empty()

        if (shaft != null) {
            block.append(getChatSeparator("§7") + "\n")

            block.append(shaft.isColored(shaftColors[shaft.uppercase()] ?: ChatFormatting.WHITE))
            if (crystal) block.append(" + §bCrystal")
            block.append("\n")
        }

        if (corpses.isNotEmpty()) {
            block.append(getChatSeparator("§7") + "\n")
            block.append("Corpses:\n")

            corpses.forEach { (name, count) ->

                block.append(
                    name.capitalize().isColored(corpseColors[name.uppercase()] ?: ChatFormatting.WHITE)
                ).append(": $count\n")
            }

            block.append(getChatSeparator("§7"))
        }

        return block
    }

    //TODO: Set colors that match closer to glass textures
    private val corpseColors = mapOf(
        "LAPIS" to ChatFormatting.BLUE,
        "TUNGSTEN" to ChatFormatting.GRAY,
        "UMBER" to ChatFormatting.GOLD,
        "VANGUARD" to ChatFormatting.AQUA
    )

    private val shaftColors = mapOf(
        "TOPAZ" to ChatFormatting.YELLOW,
        "SAPPHIRE" to ChatFormatting.BLUE,
        "AMETHYST" to ChatFormatting.DARK_PURPLE,
        "AMBER" to ChatFormatting.GOLD,
        "JADE" to ChatFormatting.GREEN,
        "TITANIUM" to ChatFormatting.WHITE,
        "UMBER" to ChatFormatting.GOLD,
        "TUNGSTEN" to ChatFormatting.GRAY,
        "VANGUARD" to ChatFormatting.AQUA,
        "RUBY" to ChatFormatting.RED,
        "ONYX" to ChatFormatting.BLACK,
        "AQUAMARINE" to ChatFormatting.DARK_BLUE,
        "CITRINE" to ChatFormatting.GOLD,
        "PERIDOT" to ChatFormatting.DARK_GREEN,
        "JASPER" to ChatFormatting.LIGHT_PURPLE,
        "OPAL" to ChatFormatting.WHITE,
        "LITTLE" to ChatFormatting.WHITE
    )
}
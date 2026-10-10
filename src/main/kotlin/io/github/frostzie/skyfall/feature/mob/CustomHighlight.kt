package io.github.frostzie.skyfall.feature.mob

import com.github.stivais.commodore.Commodore
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.core.MobStore
import io.github.frostzie.skyfall.core.MobScanRequest
import io.github.frostzie.skyfall.util.ChatUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI

object CustomHighlight {
    private val config get() = SkyFall.features.hitbox

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (!LocationAPI.isOnSkyBlock || !config.enabled) {
                MobStore.boxes.clear()
                return@register
            }

            MobStore.refresh(MobScanRequest(
                config.hypixelMobs,
                config.vanillaMobs,
                config.color.getEffectiveColourRGB(),
                config.style,
                config.model
            ))
        }
    }

    val highlightCommands = Commodore("skyfallHighlight") {

        literal("hypixel").runs { name: String ->
            if (config.hypixelMobs.add(name)) {
                ChatUtils.clientMessage("Added hypixel mob: $name")
            } else {
                config.hypixelMobs.remove(name)
                ChatUtils.clientMessage("Removed hypixel mob: $name")
            }
        }

        literal("vanilla").runs { name: String ->
            if (config.vanillaMobs.add(name)) {
                ChatUtils.clientMessage("Added vanilla mob: $name")
            } else {
                config.vanillaMobs.remove(name)
                ChatUtils.clientMessage("Removed vanilla mob: $name")
            }
        }

        literal("list").runs {
            val hypixelMobs = if (config.hypixelMobs.isEmpty()) "None" else config.hypixelMobs.joinToString(", ")
            val vanillaMobs = if (config.vanillaMobs.isEmpty()) "None" else config.vanillaMobs.joinToString(", ")

            ChatUtils.clientMessage("\nHypixel: $hypixelMobs\nVanilla: $vanillaMobs")
        }
    }
}

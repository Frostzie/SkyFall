package io.github.frostzie.skyfall.features.chat.filters

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.util.Formatting

@Feature(name = "Hide Fire Sale Messages")
object HideFireSaleMessage : IFeature {

    override var isRunning = false

    init {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (!isRunning || overlay) {
                return@register true
            }


            val messageString = Formatting.strip(message.string)
            return@register !isFireSaleMessage(messageString)
        }
    }

    override fun shouldLoad(): Boolean {
        return SkyFall.feature.chat.chatFilters.hideFireSale
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }

    private fun isFireSaleMessage(message: String?): Boolean {
        if (message == null) return false

        return message.contains("FIRE SALE") ||
                (message.contains("Selling an item for a limited time!")) ||
                (message.contains("   ♨ ")) ||
                message.contains("♨ [WARP] To Elizabeth in the next ") && message.contains("to grab yours!")
    }
}
// Regex:
// §6§k§lA§r §c§lFIRE SALE §r§6§k§lA\n
// §c♨ §eSelling an item for a limited time!\n
// §c   ♨ §5Genie Baby Goblin Skin §e(§61,184 left§e)§c\
// §c♨ §a§l[WARP] §eTo Elizabeth in the next §c0d 22h 35m §eto grab yours!
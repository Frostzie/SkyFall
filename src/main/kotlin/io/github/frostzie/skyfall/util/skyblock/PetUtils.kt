package io.github.frostzie.skyfall.util.skyblock

import com.google.gson.JsonParser
import io.github.frostzie.skyfall.util.ItemUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.ItemStack

fun isPetMenu(screen: AbstractContainerScreen<*>?): Boolean {
    if (screen !is ContainerScreen) return false
    val title = screen.title.string
    return title == "Pets" || title.endsWith(") Pets")
}


data class PetInfo(
    val active: Boolean,
    val uuid: String,
)

object PetInfoReader {
    private var lastRaw: String? = null
    private var lastResult: PetInfo? = null

    fun read(stack: ItemStack): PetInfo? {
        val raw = ItemUtils.customDataTag(stack).getString("petInfo").orElse(null) ?: return null

        if (raw == lastRaw) return lastResult

        val parsed = try {
            val json = JsonParser.parseString(raw).asJsonObject
            PetInfo(
                active = json.get("active")?.asBoolean ?: false,
                uuid = json.get("uuid").asString,
            )
        } catch (_: Exception) {
            null
        }

        lastRaw = raw
        lastResult = parsed
        return parsed
    }
}


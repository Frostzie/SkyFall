package io.github.frostzie.skyfall.feature.pets

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.events.SlotRenderListener
import io.github.frostzie.skyfall.events.SlotKeyPressListener
import io.github.frostzie.skyfall.events.SlotRenderContext
import io.github.frostzie.skyfall.util.ItemUtils
import io.github.frostzie.skyfall.util.LoggerProvider
import io.github.frostzie.skyfall.util.skyblock.PetInfoReader
import io.github.frostzie.skyfall.util.skyblock.isPetMenu
import io.github.frostzie.skyfall.util.skyblock.petSlotIds
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object FavoritePets : SlotRenderListener, SlotKeyPressListener {
    private val logger = LoggerProvider.getLogger("FavoritePets")
    private val config get() = SkyFall.features.pets.favoritePet

    private val configFile = FabricLoader.getInstance().configDir
        .resolve("skyfall").resolve("FavoritePets.json").toFile()

    private var favoriteData = PetData()

    init {
        load()
    }

    override fun onRender(context: SlotRenderContext) {
        if (!config.favEnable || !isPetMenu(context.screen)) return
        val info = PetInfoReader.read(context.slot.item) ?: return

        if (info.uuid in favoriteData.uuid) {
            val color = config.favColor.getEffectiveColourRGB()

            context.highlightColor = color
        } else if (config.hideNonFav || !info.active) {
            context.shouldRenderSlot = false
            context.shouldRenderTooltips = false
        }
    }

    override fun onKeyPress(
        screen: AbstractContainerScreen<*>,
        slot: Slot,
        key: Int
    ): Boolean {
        if (!isPetMenu(screen) || !config.favEnable) return false
        if (slot.index !in petSlotIds) return false
        //if (config.favKey != key) return false

        val uuid = ItemUtils.customDataTag(slot.item)?.getString("uuid")?.orElse(null) ?: return false
        if (uuid in favoriteData.uuid) {
            favoriteData.uuid.remove(uuid)
        } else {
            favoriteData.uuid.add(uuid)
        }
        save()
        return true
    }


    //TODO: Build a better config saving system thingy
    @Serializable
    data class PetData(val uuid: MutableSet<String> = mutableSetOf())


    fun save() {
        try {
            configFile.writeText(Json.encodeToString(PetData(uuid = favoriteData.uuid)))
        } catch (e: Exception) {
            logger.error("Failed to save FavoritePets.json", e)
        }

    }

    fun load() {
        favoriteData = try {
            if (configFile.exists()) {
                Json.decodeFromString<PetData>(configFile.readText())
            } else {
                PetData()
            }
        } catch (e: Exception) {
            logger.error("Failed to load FavoritePets.json", e)
            PetData()
        }
    }
}

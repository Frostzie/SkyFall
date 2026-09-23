package io.github.frostzie.skyfall.util.update

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.util.LoggerProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader

//TODO; Leave this for like a version or 2
object FavPetsConfig {
    private val logger = LoggerProvider.getLogger("FavPetsConfigMigration")
    private val config get() = SkyFall.features.pets.favoritePet
    private val configFile = FabricLoader.getInstance().configDir
        .resolve("skyfall").resolve("FavoritePets.json").toFile()

    @Serializable
    data class PetData(val uuid: MutableSet<String> = mutableSetOf())


    fun load() {
        try {
            if (configFile.exists()) {
                val file = Json.decodeFromString<PetData>(configFile.readText())
                config.favoritePets.addAll(file.uuid)

                configFile.delete()
            }
        } catch (e: Exception) {
            logger.error("Failed to load FavoritePets.json", e)
        }
    }
}
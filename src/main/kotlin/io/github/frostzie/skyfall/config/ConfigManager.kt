package io.github.frostzie.skyfall.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import io.github.frostzie.skyfall.util.LoggerProvider
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.LegacyStringChromaColourTypeAdapter
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import net.fabricmc.loader.api.FabricLoader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

// Sections based of SCT
// https://github.com/ChindeaOne/SkyblockCollectionTracker-fabric/blob/master/src/main/java/io/github/chindeaone/collectiontracker/config/ConfigManager.kt
class ConfigManager {
    companion object {
        val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
            .registerTypeAdapter(ChromaColour::class.java, LegacyStringChromaColourTypeAdapter(true).nullSafe())
            .enableComplexMapKeySerialization()
            .create()
    }

    private val logger = LoggerProvider.getLogger("ConfigManager")

    private val configFile = FabricLoader.getInstance().configDir.resolve("skyfall/config.json").toFile()
    var config: Features? = null
    
    var processor: MoulConfigProcessor<Features>

    init {
        readConfig()

        val config = config!!
        processor = MoulConfigProcessor(config)
        BuiltinMoulConfigGuis.addProcessors(processor)
        val driver = ConfigProcessorDriver(processor)
        driver.warnForPrivateFields = false
        driver.processConfig(config)
    }

    private fun readConfig() {
        logger.info("Loading config")
        try {
            InputStreamReader(FileInputStream(configFile), StandardCharsets.UTF_8).use { reader ->
                JsonReader(reader).use { reader ->
                    config = gson.fromJson(reader, Features::class.java)
                }
            }
        } catch (e: Exception) {
            logger.error("Could not load config", e)
        }
    }

    fun save() {
        try {
            OutputStreamWriter(FileOutputStream(configFile), StandardCharsets.UTF_8).use { writer ->
                writer.write(gson.toJson(config))
            }
        } catch (e: Exception) {
            logger.error("Could not save config", e)
        }
        logger.info("Saved config")
    }
}
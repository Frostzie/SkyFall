package io.github.frostzie.skyfall.config

import com.google.gson.GsonBuilder
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.api.feature.FeatureManager
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.concurrent.fixedRateTimer

// Taken and modified from Skyhanni
object ConfigManager {
    private val logger = LoggerProvider.getLogger("configManager")
    val gson = GsonBuilder().setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
        .enableComplexMapKeySerialization()
        .create()

    lateinit var features: Features

    private var configDirectory = File("config/skyfall")
    private var configFile: File? = null
    lateinit var processor: MoulConfigProcessor<Features>

    fun firstLoad() {
        configDirectory.mkdirs()
        configFile = File(configDirectory, "config.json")
        logger.info("Trying to load config from $configFile")

        if (configFile!!.exists()) {
            try {
                logger.info("load-config-now")
                val inputStreamReader = InputStreamReader(FileInputStream(configFile!!), StandardCharsets.UTF_8)
                val bufferedReader = BufferedReader(inputStreamReader)
                features = gson.fromJson(bufferedReader.readText(), Features::class.java)
                logger.info("Loaded config File")
            } catch (e: Exception) {
                logger.error("Exception while reading config file $configFile", e)
                val backupFile = configFile!!.resolveSibling("config-${SimpleTimeMark.now().toMillis()}-backup.json")
                logger.error("Exception while reading $configFile. Will load fresh config and save backup to $backupFile", e)
                try {
                    configFile!!.copyTo(backupFile)
                } catch (e: Exception) {
                    logger.error("Couldn't create a backup file for config", e)
                }
            }
        }

        if (!this::features.isInitialized) {
            logger.info("Creating a new config file and saving it")
            features = Features()
            saveConfig("blank config")
        }

        logger.info("Initializing MoulConfig with the loaded configuration.")
        processor = MoulConfigProcessor(SkyFall.feature)
        BuiltinMoulConfigGuis.addProcessors(processor)
        val driver = ConfigProcessorDriver(processor)
        driver.warnForPrivateFields = false
        driver.processConfig(SkyFall.feature)

        fixedRateTimer(name = "skyfall-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            try {
                saveConfig("auto-save-60s")
                logger.info(" 60s Config Save.")
            } catch (e: Throwable) {
                logger.error("Error auto-saving config!", e)
            }
        }
    }

    fun saveConfig(reason: String) {
        val file = configFile ?: throw Error("Can't save config, configFile is null")
        try {
            file.parentFile.mkdirs()
            val unit = file.parentFile.resolve("config.json.write")
            unit.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8)).use { writer ->
                writer.write(gson.toJson(SkyFall.feature))
            }

            Files.move(
                unit.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (e: IOException) {
            logger.error("Couldn't save config file to $file", e)
        }
    }
}
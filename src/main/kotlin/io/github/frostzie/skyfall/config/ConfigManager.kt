package io.github.frostzie.skyfall.config

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.SimpleTimeMark

import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor

import com.google.gson.GsonBuilder
import io.github.frostzie.skyfall.utils.LoggerProvider
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.concurrent.fixedRateTimer

object ConfigManager {
    private val logger = LoggerProvider.getLogger("configManager")
    val gson = GsonBuilder().setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
        .enableComplexMapKeySerialization()
        .create()

    lateinit var features: Features

    var configDirectory = File("config/skyfall")
    private var configFile: File? = null
    lateinit var processor: MoulConfigProcessor<Features>

    fun firstLoad() {
        if (ConfigManager::features.isInitialized) {
            println("SkyFall: Loading config despite config alr being loaded?..")
        }

        configDirectory.mkdirs()
        configFile = File(configDirectory, "config.json")
        println("Trying to load config from $configFile")
        if (configFile!!.exists()) {
            try {
                println("load-config-now")
                val inputStreamReader = InputStreamReader(FileInputStream(configFile!!), StandardCharsets.UTF_8)
                val bufferedReader = BufferedReader(inputStreamReader)
                features = gson.fromJson(bufferedReader.readText(), Features::class.java)
                println("SkyFall: Loaded config file")
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

        if (!ConfigManager::features.isInitialized) {
            println("Creating a new config file and saving it")
            features = Features()
            saveConfig("blank config")
        }

        fixedRateTimer(name = "skyfall-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            try {
                saveConfig("auto-save-60s")
            } catch (e: Throwable) {
                logger.error("Error auto-saving config!", e)
            }
        }

        //TODO: Add UpdateManager
        val features = SkyFall.feature
        processor = MoulConfigProcessor(SkyFall.feature)
        BuiltinMoulConfigGuis.addProcessors(processor)
        //UpdateManager.injectConfigProcessor(processor)
        val driver = ConfigProcessorDriver(processor)
        driver.warnForPrivateFields = false
        driver.processConfig(features)
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
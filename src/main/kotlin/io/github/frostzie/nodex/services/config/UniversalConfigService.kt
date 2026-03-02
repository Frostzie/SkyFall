package io.github.frostzie.nodex.services.config

import com.google.gson.GsonBuilder
import io.github.frostzie.nodex.domain.config.UniversalRuntimeConfig
import io.github.frostzie.nodex.loader.fabric.Folders
import io.github.frostzie.nodex.services.core.FileService
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Path

/**
 * Service that manages the universal_config.json file.
 * This file is ALWAYS located in the default Fabric config/nodex folder.
 */
class UniversalConfigService(private val fileService: FileService) {
    private val logger = LoggerProvider.getLogger("UniversalConfigService")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    private val defaultConfigDir: Path = Folders.configDir.resolve("nodex")
    private val configFile: Path = defaultConfigDir.resolve("universal_config.json")
    
    private var _config = UniversalRuntimeConfig()
    val config: UniversalRuntimeConfig get() = _config

    fun initialize() {
        if (!fileService.exists(defaultConfigDir)) {
            try {
                fileService.createDirectory(defaultConfigDir)
            } catch (e: Exception) {
                logger.error("Failed to create default config directory", e)
                return
            }
        }
        load()
    }

    private fun load() {
        if (fileService.exists(configFile)) {
            try {
                val content = fileService.readText(configFile)
                val loaded = gson.fromJson(content, UniversalRuntimeConfig::class.java)
                _config = loaded ?: UniversalRuntimeConfig()
            } catch (e: Exception) {
                logger.error("Failed to load universal_config.json, falling back to defaults", e)
                _config = UniversalRuntimeConfig()
            }
        } else {
            _config = UniversalRuntimeConfig()
        }
    }

    fun save(config: UniversalRuntimeConfig): Boolean {
        return try {
            val json = gson.toJson(config)
            fileService.writeAtomic(configFile, json)
            _config = config
            logger.debug("Saved universal_config.json")
            true
        } catch (e: Exception) {
            logger.error("Failed to save universal_config.json", e)
            false
        }
    }
}

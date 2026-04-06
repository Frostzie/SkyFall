package io.github.frostzie.nodex.services.config.stationary

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.frostzie.nodex.services.core.FileService
import io.github.frostzie.nodex.services.core.ModVersionService
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Path

/**
 * Service managing the stationary configuration file (nodex.json).
 * Contains universal paths and version tracking.
 */
class ConfigService(
    configRoot: Path,
    private val fileService: FileService,
    private val modVersionService: ModVersionService
) {
    private val logger = LoggerProvider.getLogger("ConfigService")
    private val mapper = ObjectMapper().registerKotlinModule()
    private val configPath = configRoot.resolve("nodex").resolve("nodex.json")

    private var _lastUsedModVersion: String = "0.0.0"
    private var _universalPathEnabled: Boolean = false
    private var _universalPath: String? = null

    val lastUsedModVersion: String get() = _lastUsedModVersion
    val universalPathEnabled: Boolean get() = _universalPathEnabled
    val universalPath: String? get() = _universalPath

    private data class StationaryConfig(
        val modVersion: String,
        val universalPathEnabled: Boolean = false,
        val universalPath: String? = null
    )

    /**
     * Loads from disk or creates defaults if missing.
     */
    fun initialize() {
        if (fileService.exists(configPath)) {
            load()
        } else {
            createDefault()
        }
    }

    private fun load() {
        try {
            val json = fileService.readText(configPath)
            val config = mapper.readValue(json, StationaryConfig::class.java)

            _lastUsedModVersion = config.modVersion
            _universalPathEnabled = config.universalPathEnabled
            _universalPath = config.universalPath

            logger.info("Loaded stationary config. Last version recorded: $_lastUsedModVersion")
        } catch (e: Exception) {
            logger.error("Failed to load stationary config, falling back to defaults", e)
            createDefault()
        }
    }

    private fun createDefault() {
        _lastUsedModVersion = modVersionService.currentVersion
        _universalPathEnabled = false
        _universalPath = null
        save()
    }

    /**
     * Marks that settings have been successfully loaded and migrated.
     * Updates the modVersion to current one.
     */
    fun markSettingsLoadCompleted() {
        if (_lastUsedModVersion != modVersionService.currentVersion) {
            logger.info("Settings load completed. Updating recorded version to ${modVersionService.currentVersion}")
            _lastUsedModVersion = modVersionService.currentVersion
            save()
        }
    }

    /**
     * Saves the current state to disk.
     */
    fun save() {
        try {
            val config = StationaryConfig(
                modVersion = _lastUsedModVersion,
                universalPathEnabled = _universalPathEnabled,
                universalPath = _universalPath
            )
            val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config)
            fileService.writeAtomic(configPath, json)
        } catch (e: Exception) {
            logger.error("Failed to save stationary config", e)
        }
    }
}

package io.github.frostzie.nodex.services.config

import com.google.gson.GsonBuilder
import io.github.frostzie.nodex.domain.config.ConfigState
import io.github.frostzie.nodex.loader.fabric.Folders
import io.github.frostzie.nodex.services.core.FileService
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Service that manages the application's config directory pointer.
 *
 * It determines where config files (layout, session, etc.) are stored.
 * By default, this is in the Fabric config folder, but it can be redirected
 * via a 'config_location.json' file to support universal setups.
 */
class ConfigService(private val fileService: FileService) {
    private val logger = LoggerProvider.getLogger("ConfigService")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    // The default location: <instance>/config/nodex
    private val defaultConfigDir: Path = Folders.configDir.resolve("nodex")

    // File that determines the actual config location
    private val locationFile: Path = defaultConfigDir.resolve("config_location.json")

    // TODO: Replace lateinit with an explicit initialization contract or default-backed state to avoid pre-init access.
    private lateinit var _state: ConfigState
    
    /**
     * The reactive domain state containing the resolved config directory.
     */
    val state: ConfigState get() = _state

    /**
     * The current config directory.
     */
    val configDir: Path get() = state.configDir

    // DTO for the location file
    private data class LocationConfig(var customConfigPath: String? = null)

    /**
     * Initializes the service by resolving the initial config directory.
     */
    fun initialize() {
        val resolved = resolveConfigDir()
        _state = ConfigState(resolved)
        
        if (!fileService.exists(configDir)) {
            try {
                fileService.createDirectory(configDir)
            } catch (e: Exception) {
                logger.error("Failed to create config directory: $configDir", e)
            }
        }
        logger.debug("ConfigService initialized. Root: {}", configDir)
    }

    /**
     * Reloads the config directory from the location file.
     */
    fun reload() {
        // TODO: Guard reload access when service state is not initialized yet.
        val resolved = resolveConfigDir()
        state.configDir = resolved
        
        if (!fileService.exists(configDir)) {
            try {
                fileService.createDirectory(configDir)
            } catch (e: Exception) {
                logger.error("Failed to create config directory on reload: $configDir", e)
            }
        }
        logger.debug("Config directory reloaded: {}", configDir)
    }

    /**
     * Sets a custom configuration path and saves it to the locationFile.
     * Passing null reverts to the default directory.
     * 
     * @return true if successful, false otherwise.
     */
    fun setCustomConfigPath(path: Path?): Boolean {
        // TODO: Guard setter access when service state is not initialized yet.
        return try {
            if (!fileService.exists(defaultConfigDir)) {
                fileService.createDirectory(defaultConfigDir)
            }

            val config = LocationConfig(path?.toAbsolutePath()?.toString())
            val json = gson.toJson(config)

            fileService.writeAtomic(locationFile, json)
            logger.debug("Updated config location pointer. New path: {}", path ?: "Default")
            
            // Update the state immediately
            state.configDir = path ?: defaultConfigDir
            true
        } catch (e: Exception) {
            logger.error("Failed to save config location file.", e)
            false
        }
    }

    /**
     * Resolves the config directory by reading the location file.
     */
    private fun resolveConfigDir(): Path {
        if (!fileService.exists(defaultConfigDir)) {
            try {
                fileService.createDirectory(defaultConfigDir)
            } catch (e: Exception) {
                logger.error("Failed to create default config dir: $defaultConfigDir", e)
                return defaultConfigDir
            }
        }

        if (fileService.exists(locationFile)) {
            try {
                val json = fileService.readText(locationFile)
                val config = gson.fromJson(json, LocationConfig::class.java)
                if (!config.customConfigPath.isNullOrBlank()) {
                    // TODO: Validate custom path (exists/is directory/writable) before accepting it as the active config root.
                    val customPath = Paths.get(config.customConfigPath!!)
                    logger.debug("Using custom config directory: {}", customPath)
                    return customPath
                }
            } catch (e: Exception) {
                logger.error("Failed to read config location file, falling back to default.", e)
            }
        }
        return defaultConfigDir
    }
}

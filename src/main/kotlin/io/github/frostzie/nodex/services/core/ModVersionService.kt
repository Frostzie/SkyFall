package io.github.frostzie.nodex.services.core

import io.github.frostzie.nodex.domain.entity.ModInfo
import io.github.frostzie.nodex.loader.fabric.ModVersion

/**
 * Service providing information about the current version of the mod.
 */
class ModVersionService {
    
    /**
     * Initializes the service by fetching the mod version from the loader.
     */
    fun initialize() {
        ModInfo.version = ModVersion.getModVersion()
    }

    /**
     * The current version of the mod.
     */
    val currentVersion: String get() = ModInfo.version
}

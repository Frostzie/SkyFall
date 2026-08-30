package io.github.frostzie.skyfall.util

import net.fabricmc.loader.api.FabricLoader

object DevUtils {
    fun isDevEnv(): Boolean {
        return FabricLoader.getInstance().isDevelopmentEnvironment
    }
}
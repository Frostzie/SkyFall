package io.github.frostzie.skyfall

import io.github.frostzie.skyfall.commands.CommandManager
import io.github.frostzie.skyfall.config.ConfigManager
import io.github.frostzie.skyfall.config.Features
import io.github.frostzie.skyfall.data.RepoManager
import io.github.frostzie.skyfall.features.FeatureManager
import io.github.frostzie.skyfall.hud.HudManager
import io.github.frostzie.skyfall.utils.IslandManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screen.Screen

class SkyFall : ModInitializer {

	override fun onInitialize() {
		registerTickEvent()
		configManager = ConfigManager
		configManager.firstLoad()
		Runtime.getRuntime().addShutdownHook(Thread {
			configManager.saveConfig("shutdown-hook")
		})

		IslandManager.init()
		CommandManager.loadCommands()
		FeatureManager.loadFeatures()
		HudManager.init()

		ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
			RepoManager.initializeRepo()
		}
	}

	fun registerTickEvent() {
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (screenToOpen != null) {
				client.setScreen(screenToOpen)
				screenToOpen = null
			}
		}
	}

	companion object {
		lateinit var configManager: ConfigManager

		@JvmStatic
		val feature: Features get() = configManager.features

		var screenToOpen: Screen? = null
	}
}
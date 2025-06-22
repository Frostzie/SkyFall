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
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.gui.screen.Screen
import io.github.frostzie.skyfall.utils.events.TooltipEvents

// In your SkyFall.kt file

class SkyFall : ModInitializer {
	override fun onInitialize() {
		configManager = ConfigManager
		configManager.firstLoad()
		Runtime.getRuntime().addShutdownHook(Thread {
			configManager.saveConfig("shutdown-hook")
		})
		IslandManager.init()
		CommandManager.loadCommands()
		HudManager.init()
		FeatureManager.loadFeatures()

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (screenToOpen != null) {
				client.setScreen(screenToOpen)
				screenToOpen = null
			}
		}


		ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
			if (!stack.isEmpty) {
				TooltipEvents.onTooltipRender(stack, lines)
			}
		}

		ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
			RepoManager.initializeRepo()
		}
	}

	companion object {
		lateinit var configManager: ConfigManager

		@JvmStatic
		val feature: Features get() = configManager.features

		var screenToOpen: Screen? = null
	}
}
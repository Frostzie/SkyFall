package io.github.frostzie.skyfall

import io.github.frostzie.skyfall.commands.CommandManager
import io.github.frostzie.skyfall.config.ConfigGuiManager
import io.github.frostzie.skyfall.config.ConfigManager
import io.github.frostzie.skyfall.config.Features
import io.github.frostzie.skyfall.data.RepoManager
import io.github.frostzie.skyfall.api.feature.FeatureManager
import io.github.frostzie.skyfall.hud.HudManager
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.UpdateChecker
import io.github.frostzie.skyfall.events.inventory.TooltipEvents
import io.github.frostzie.skyfall.utils.item.StackCountRenderer
import io.github.frostzie.skyfall.utils.processors.ScoreboardProcessor
import io.github.frostzie.skyfall.utils.processors.TabListProcessor
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.gui.screen.Screen

class SkyFall : ModInitializer {
	override fun onInitialize() {
		configManager = ConfigManager
		configManager.firstLoad()

		CommandManager.loadCommands()
		StackCountRenderer.initialize()
		HudManager.init()
		IslandDetector.init()
		FeatureManager.initialize()

		Runtime.getRuntime().addShutdownHook(Thread {
			configManager.saveConfig("shutdown-hook")
		})

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			ScoreboardProcessor.processScoreboard()
			TabListProcessor.processTabList()

			if (screenToOpen != null) {
				client.setScreen(screenToOpen)
				screenToOpen = null
			}

			if (ConfigGuiManager.currentScreenInstance != null && client.currentScreen != ConfigGuiManager.currentScreenInstance) {
				feature.saveNow()
				ConfigGuiManager.currentScreenInstance = null
			}
		}

		ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
			if (!stack.isEmpty) {
				TooltipEvents.onTooltipRender(stack, lines)
			}
		}

		ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
			RepoManager.initializeRepo()
			UpdateChecker.initialize()
		}
	}

	companion object {
		lateinit var configManager: ConfigManager

		@JvmStatic
		val feature: Features get() = configManager.features

		var screenToOpen: Screen? = null
	}
}
package io.github.frostzie.skyfall.hud

import com.google.gson.GsonBuilder
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.render.HudRenderEvent
import io.github.frostzie.skyfall.utils.LoggerProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import java.io.File

data class HudConfigData(
    val elements: Map<String, HudElementConfig> = emptyMap()
)

object HudManager {
    private val logger = LoggerProvider.getLogger("HudManager")
    private val elements = mutableMapOf<String, HudElement>()
    private val configFile = File(MinecraftClient.getInstance().runDirectory, "config/skyfall/location.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var loadedConfig: HudConfigData? = null
    private var isInitialized = false

    fun init() {
        if (isInitialized) return
        isInitialized = true

        loadConfigFromFile()

        EventBus.listen(HudRenderEvent.Main::class.java) { event ->
            renderAllElements(event.context, event.tickCounter)
        }
        logger.info("HudManager initialized and listening to render events.")
    }

    /**
     * Registers a new HUD element with the manager.
     * This is called by features when they initialize.
     */
    fun registerElement(element: HudElement) {
        val savedConfig = loadedConfig?.elements?.get(element.id)
        if (savedConfig != null) {
            element.updateConfig(savedConfig)
        }

        elements[element.id] = element
        logger.info("Registered HUD element: ${element.id}")
    }

    /**
     * Unregisters a HUD element from the manager.
     * This is called by features when they terminate to clean up resources.
     */
    fun unregisterElement(id: String) {
        if (elements.remove(id) != null) {
            logger.info("Unregistered HUD element: $id")
        }
    }

    fun getElements(): Collection<HudElement> = elements.values

    fun getElement(id: String): HudElement? = elements[id]

    private fun renderAllElements(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val client = MinecraftClient.getInstance()
        if (client.player == null || client.options.hudHidden || client.debugHud.shouldShowDebugHud()) {
            return
        }

        if (client.currentScreen is HudEditorScreen) {
            return
        }

        elements.values.forEach { element ->
            if (element.config.enabled) {
                element.render(drawContext, tickCounter)
            }
        }
    }

    private fun loadConfigFromFile() {
        if (!configFile.exists()) {
            loadedConfig = HudConfigData()
            return
        }
        try {
            val configText = configFile.readText()
            loadedConfig = gson.fromJson(configText, HudConfigData::class.java) ?: HudConfigData()
        } catch (e: Exception) {
            logger.error("Failed to load HUD config: ${e.message}", e)
            loadedConfig = HudConfigData() // Ensure loadedConfig is not null on failure
        }
    }

    fun saveConfig() {
        try {
            configFile.parentFile.mkdirs()
            val elementConfigs = elements.mapValues { it.value.config }
            val configData = HudConfigData(elements = elementConfigs)
            val configText = gson.toJson(configData)
            configFile.writeText(configText)
        } catch (e: Exception) {
            logger.error("Failed to save HUD config: ${e.message}", e)
        }
    }
}
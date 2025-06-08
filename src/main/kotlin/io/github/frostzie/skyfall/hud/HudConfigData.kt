package io.github.frostzie.skyfall.hud

import com.google.gson.GsonBuilder
import io.github.frostzie.skyfall.utils.LoggerProvider
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import java.io.File

data class HudConfigData(
    val elements: Map<String, HudElementConfig> = emptyMap()
)

object HudManager {
    private val logger = LoggerProvider.getLogger("hudManager")
    private val elements = mutableMapOf<Identifier, HudElement>()
    private val configFile = File(MinecraftClient.getInstance().runDirectory, "config/skyfall/location.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val HUD_LAYER_ID = Identifier.of("skyfall", "hud_elements")

    fun init() {
        loadConfig()

        HudLayerRegistrationCallback.EVENT.register(
            HudLayerRegistrationCallback { drawer: LayeredDrawerWrapper ->
                drawer.attachLayerAfter(
                    IdentifiedLayer.MISC_OVERLAYS,
                    HUD_LAYER_ID
                ) { context: DrawContext, tickCounter: RenderTickCounter ->
                    renderAllElements(context, tickCounter)
                }
            }
        )
    }

    fun registerElement(element: HudElement) {
        val savedConfig = loadedConfig?.elements?.get(element.id.toString())
        if (savedConfig != null) {
            element.updateConfig(savedConfig)
        } else {
            element.updateConfig(element.defaultConfig)
        }
        elements[element.id] = element
    }

    fun getElements(): Collection<HudElement> = elements.values

    fun getElement(id: Identifier): HudElement? = elements[id]

    private fun renderAllElements(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val client = MinecraftClient.getInstance() ?: return
        if (client.player != null && !client.options.hudHidden && !client.debugHud.shouldShowDebugHud()) {
            if (client.currentScreen !is HudEditorScreen) {
                elements.values.forEach { element ->
                    if (element.config.enabled) {
                        element.render(drawContext, tickCounter)
                    }
                }
            }
        }
    }

    private var loadedConfig: HudConfigData? = null

    private fun loadConfig() {
        try {
            if (configFile.exists()) {
                val configText = configFile.readText()
                loadedConfig = gson.fromJson(configText, HudConfigData::class.java)
                loadedConfig?.elements?.forEach { (id, config) ->
                    elements[Identifier.of(id)]?.updateConfig(config)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to load HUD config: ${e.message}", e)
            loadedConfig = HudConfigData()
        }
    }

    fun saveConfig() {
        try {
            configFile.parentFile.mkdirs()
            val configData = HudConfigData(
                elements = elements.mapKeys { it.key.toString() }
                    .mapValues { it.value.config }
            )
            val configText = gson.toJson(configData)
            configFile.writeText(configText)
        } catch (e: Exception) {
            logger.error("Failed to save HUD config: ${e.message}", e)
        }
    }
}
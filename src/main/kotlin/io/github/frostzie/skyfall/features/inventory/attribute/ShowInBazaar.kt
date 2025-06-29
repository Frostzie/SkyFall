package io.github.frostzie.skyfall.features.inventory.attribute

import com.google.gson.JsonObject
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.RepoManager
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.utils.events.TooltipEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

@Feature(name = "Show Attribute Info in Bazaar")
object ShowInBazaar : IFeature {
    override var isRunning = false
    private val logger = LoggerProvider.getLogger("ShowInBazaar")
    private val config get() = SkyFall.feature.inventory.attributeMenu

    private var attributeData: JsonObject? = null
    private var lastDataLoadTime = 0L
    private const val DATA_REFRESH_INTERVAL = 60000L

    init {
        registerTooltipEvent()
        loadAttributeData()
    }

    override fun shouldLoad(): Boolean {
        return config.showInBazaar
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }

    private fun registerTooltipEvent() {
        TooltipEvents.register { stack, lines ->
            if (!isRunning) return@register
            onTooltipRender(stack, lines)
        }
    }

    private fun loadAttributeData() {
        val currentTime = System.currentTimeMillis()
        if (attributeData == null || currentTime - lastDataLoadTime > DATA_REFRESH_INTERVAL) {
            try {
                attributeData = RepoManager.loadJsonFile("constants/AttributeMenuData.json")
                lastDataLoadTime = currentTime

                if (attributeData != null) {
                    logger.info("Successfully loaded attribute data for bazaar. Found ${attributeData!!.keySet().size} attribute entries.")
                } else {
                    logger.warn("Failed to load attribute data from 'AttributeMenuData.json'. The file might be missing or invalid.")
                }
            } catch (e: Exception) {
                logger.error("An exception occurred while loading attribute data for bazaar", e)
                attributeData = null
            }
        }
    }

    fun onTooltipRender(stack: ItemStack, lines: MutableList<Text>) {
        if (stack.isEmpty) {
            return
        }

        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen !is HandledScreen<*>) {
            return
        }

        val screenTitle = currentScreen.title.string
        if (!screenTitle.contains("Oddities ➜ Shards")) {
            return
        }

        loadAttributeData()
        if (attributeData == null) {
            return
        }

        addBazaarInfo(stack, lines)
    }

    private fun addBazaarInfo(stack: ItemStack, lines: MutableList<Text>) {
        try {
            val itemName = getCleanItemName(stack)
            if (itemName.isEmpty()) return

            val itemData = findItemDataByBazaarName(itemName) ?: return
            val maxShards = itemData.get("maxShards")?.asInt ?: return
            val statBoost = itemData.get("statBoost")?.asString ?: return

            val commodityLineIndex = lines.indexOfFirst {
                ColorUtils.stripColorCodes(it.string).contains("commodity", ignoreCase = true)
            }

            if (commodityLineIndex != -1) {
                val emptyLine = Text.literal("")
                val maxShardsLine = Text.literal("§7Max Shards: §b$maxShards")
                val statBoostLine = Text.literal(statBoost)

                lines.addAll(commodityLineIndex + 1, listOf(emptyLine, maxShardsLine, statBoostLine))
            }
        } catch (e: Exception) {
            logger.error("Failed to add bazaar info", e)
        }
    }

    private fun findItemDataByBazaarName(itemName: String): JsonObject? {
        try {
            for (key in attributeData!!.keySet()) {
                val itemData = attributeData!!.getAsJsonObject(key)
                val bazaarName = itemData.get("bazaarName")?.asString
                if (bazaarName != null && itemName.contains(bazaarName, ignoreCase = true)) {
                    return itemData
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to find item data by bazaar name", e)
        }
        return null
    }

    private fun getCleanItemName(stack: ItemStack): String {
        val rawName = stack.name.string
        return ColorUtils.stripColorCodes(rawName).trim()
    }
}
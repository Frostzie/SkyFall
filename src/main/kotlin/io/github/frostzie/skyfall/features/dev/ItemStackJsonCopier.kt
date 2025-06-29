package io.github.frostzie.skyfall.features.dev

import com.google.gson.*
import com.mojang.serialization.JsonOps
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.LoggerProvider
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import org.lwjgl.glfw.GLFW
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

// Taken and modified from Component Viewer
@Feature(name = "Item Stack JSON Copier")
object ItemStackJsonCopier : IFeature {
    override var isRunning = false
    private val logger = LoggerProvider.getLogger("ItemStackJsonCopier")

    init {
        registerTickHandler()
    }

    override fun shouldLoad(): Boolean {
        return SkyFall.feature.dev.copyItemDataKey != GLFW.GLFW_KEY_UNKNOWN
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!isRunning) return@register

            val screen = client.currentScreen
            if (screen is HandledScreen<*>) {
                handleKeyPress(screen)
            }
        }
    }

    private fun handleKeyPress(screen: HandledScreen<*>) {
        val copyKey = SkyFall.feature.dev.copyItemDataKey
        if (copyKey == GLFW.GLFW_KEY_UNKNOWN) {
            return
        }

        if (KeyboardManager.run { copyKey.isKeyClicked() }) {
            handleCopyAction(screen)
        }
    }

    private fun handleCopyAction(screen: HandledScreen<*>) {
        val hoveredSlot = getHoveredSlot(screen) ?: return

        if (hoveredSlot.stack.isEmpty) {
            return
        }

        val itemStack = hoveredSlot.stack
        copyItemStackAsJson(itemStack)
    }

    private fun getHoveredSlot(screen: HandledScreen<*>): Slot? {
        return try {
            screen.focusedSlot
        } catch (e: Exception) {
            logger.error("Failed to get hovered slot: ${e.message}", e)
            val mouseX = MinecraftClient.getInstance().mouse.x * screen.width / MinecraftClient.getInstance().window.width
            val mouseY = MinecraftClient.getInstance().mouse.y * screen.height / MinecraftClient.getInstance().window.height

            try {
                screen.getSlotAt(mouseX, mouseY)
            } catch (e2: Exception) {
                logger.error("Failed to get slot at mouse position: ${e2.message}", e2)
                null
            }
        }
    }

    private fun copyItemStackAsJson(itemStack: ItemStack) {
        try {
            logger.info("Starting to format ItemStack...")
            val jsonString = formatItemStackAsJson(itemStack)
            logger.info("Successfully formatted ItemStack, copying to clipboard...")
            setClipboard(jsonString)
            logger.info("Successfully copied to clipboard")
            ChatUtils.messageToChat("Successfully copied item to clipboard!").send()
        } catch (e: Exception) {
            logger.error("Error formatting ItemStack as JSON: ${e.message}", e)
        }
    }

    private fun formatItemStackAsJson(itemStack: ItemStack): String {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: throw IllegalStateException("Player is null")
        val registryManager = player.registryManager ?: throw IllegalStateException("Player registry manager is null")

        val jsonElement = ItemStack.CODEC.encodeStart(
            registryManager.getOps(JsonOps.INSTANCE),
            itemStack
        ).getOrThrow { error -> RuntimeException("Failed to encode ItemStack: $error") }

        return formatJsonElement(jsonElement.asJsonObject)
    }

    private fun formatJsonElement(jsonObject: JsonObject): String {
        val result = JsonObject()

        jsonObject.get("components")?.asJsonObject?.get("minecraft:custom_name")?.let { customName ->
            result.add("minecraft:custom_name", customName)
        }

        jsonObject.get("id")?.let { id ->
            result.add("id", id)
        }

        jsonObject.get("count")?.let { count ->
            result.add("count", count)
        }

        jsonObject.get("components")?.asJsonObject?.let { components ->
            val formattedComponents = JsonObject()

            components.get("minecraft:lore")?.asJsonArray?.let { lore ->
                val formattedLore = formatMinecraftLore(lore)
                formattedComponents.add("minecraft:lore", formattedLore)
            }

            components.entrySet().forEach { (key, value) ->
                if (key != "minecraft:lore" && key != "minecraft:custom_name") {
                    formattedComponents.add(key, value)
                }
            }
            result.add("components", formattedComponents)
        }
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(result)
    }

    private fun formatMinecraftLore(loreArray: JsonArray): JsonArray {
        val result = JsonArray()

        try {
            loreArray.forEach { element ->
                if (element?.isJsonObject == true) {
                    val obj = element.asJsonObject
                    val extra = obj.getAsJsonArray("extra")

                    if (extra != null) {
                        val lineText = StringBuilder()
                        extra.forEach { extraElement ->
                            try {
                                if (extraElement?.isJsonObject == true) {
                                    val extraObj = extraElement.asJsonObject
                                    val text = extraObj.get("text")?.asString ?: ""
                                    val color = extraObj.get("color")?.asString
                                    val colorCode = ColorUtils.getColorCode(color)
                                    val formatCodes = ColorUtils.formatCodes(extraObj)
                                    lineText.append(colorCode).append(formatCodes).append(text)
                                } else if (extraElement?.isJsonPrimitive == true) {
                                    lineText.append(extraElement.asString)
                                }
                            } catch (e: Exception) {
                                logger.warn("Error processing extra element: ${e.message}")
                            }
                        }
                        if (lineText.isNotEmpty()) {
                            result.add(JsonPrimitive(lineText.toString()))
                        }
                    } else {
                        val text = obj.get("text")?.asString ?: ""
                        if (text.isNotEmpty()) {
                            result.add(JsonPrimitive(text))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing lore array: ${e.message}", e)
            return loreArray
        }

        return result
    }

    private fun setClipboard(content: String) {
        try {
            val client = MinecraftClient.getInstance()
            client.keyboard?.clipboard = content
            logger.info("Successfully copied to clipboard using Minecraft's clipboard")
        } catch (e: Exception) {
            logger.error("Error accessing Minecraft clipboard: ${e.message}", e)
            try {
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(StringSelection(content), null)
                logger.info("Successfully copied to system clipboard")
            } catch (e2: java.awt.HeadlessException) {
                logger.error("Cannot access system clipboard: ${e2.message}", e2)
                logger.warn("Cannot access clipboard (headless environment)")
                logger.info("Content that would be copied:")
                logger.info(content)
            } catch (e2: Exception) {
                logger.error("Error accessing clipboard: ${e2.message}", e2)
                logger.info(content)
            }
        }
    }
}
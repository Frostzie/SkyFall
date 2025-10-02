package io.github.frostzie.skyfall.features.inventory

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.KeyboardManager
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.inventory.SlotClickEvent
import io.github.frostzie.skyfall.events.inventory.SlotRenderEvent
import io.github.frostzie.skyfall.events.inventory.SlotRenderEvents
import io.github.frostzie.skyfall.utils.item.ItemUtils
import io.github.frostzie.skyfall.utils.item.PetUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.io.File
import java.io.FileWriter

@Feature(name = "Favorite Pets")
object FavoritePet : IEventFeature {

    override var isRunning = false
    private val logger = LoggerProvider.getLogger("FavoritePet")
    private val configFile = File("config/skyfall/favorite-pets.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var highlightedItems = mutableSetOf<String>()
    private var expSharedPets = mutableSetOf<String>()
    private var favoredOnlyToggle = true
    private var currentScreen: HandledScreen<*>? = null

    private val validSlotRanges = setOf(
        10..16,
        19..25,
        28..34,
        37..43
    )

    private val FAVORITE_COLOR get() = ColorUtils.parseColorString(SkyFall.feature.inventory.petMenu.petHighlightColor)
    private val ACTIVE_PET_COLOR get() = ColorUtils.parseColorString(SkyFall.feature.inventory.petMenu.petActiveColor)
    private val XP_SHARED_COLOR get() = ColorUtils.parseColorString(SkyFall.feature.inventory.petMenu.xpSharedColor) // New color property

    init {
        loadConfig()
        registerTickHandler()
        registerSlotRenderEvent()
        registerEventHandlers()
    }

    override fun shouldLoad(): Boolean {
        val config = SkyFall.feature.inventory.petMenu
        return config.favoriteKey != GLFW.GLFW_KEY_UNKNOWN || config.activePet || config.showXPSharedPets
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
        removeToggleButton()
        currentScreen = null
    }

    private fun registerEventHandlers() {
        SlotClickEvent.subscribe { event ->
            if (!isRunning) return@subscribe

            val slot = event.slot ?: return@subscribe
            val stack = slot.stack
            val currentScreen = MinecraftClient.getInstance().currentScreen
            if (currentScreen is HandledScreen<*> && isPetMenu(currentScreen)) {
                if (isSlotInChestInventory(slot) && validSlotRanges.any { slot.index in it }) {
                    if (favoredOnlyToggle && !stack.isEmpty && PetUtils.isPet(stack)) {
                        val itemUuid = ItemUtils.getUuid(stack)
                        val config = SkyFall.feature.inventory.petMenu

                        val isFavorite = itemUuid != null && highlightedItems.contains(itemUuid)
                        val isActive = config.activePet && PetUtils.isActivePet(stack)
                        val isExpShared = config.showXPSharedPets && itemUuid != null && expSharedPets.contains(itemUuid)

                        if (!isFavorite && !isActive && !isExpShared) {
                            event.cancel()
                        }
                    }
                }
            }
        }

        SlotRenderEvent.subscribe { event ->
            if (!isRunning) return@subscribe

            val slot = event.slot
            val stack = slot.stack
            val currentScreen = MinecraftClient.getInstance().currentScreen
            if (currentScreen is HandledScreen<*> && isPetMenu(currentScreen)) {
                if (isSlotInChestInventory(slot) && validSlotRanges.any { slot.index in it }) {
                    if (favoredOnlyToggle && !stack.isEmpty && PetUtils.isPet(stack)) {
                        val itemUuid = ItemUtils.getUuid(stack)
                        val config = SkyFall.feature.inventory.petMenu

                        val isFavorite = itemUuid != null && highlightedItems.contains(itemUuid)
                        val isActive = config.activePet && PetUtils.isActivePet(stack)
                        val isExpShared = config.showXPSharedPets && itemUuid != null && expSharedPets.contains(itemUuid)

                        if (!isFavorite && !isActive && !isExpShared) {
                            event.hide()
                            event.hideTooltip()
                        }
                    }
                }
            }
        }
    }

    private fun registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!isRunning) {
                if (currentScreen != null) {
                    removeToggleButton()
                    currentScreen = null
                }
                return@register
            }

            val screen = client.currentScreen
            val config = SkyFall.feature.inventory.petMenu

            if (config.showXPSharedPets && screen is HandledScreen<*> && isExpShareMenu(screen)) {
                updateExpSharedPets(screen)
            }

            if (screen is HandledScreen<*> && isPetMenu(screen)) {
                if (currentScreen != screen) {
                    currentScreen = screen
                    setupToggleButton(screen)
                }
                handleKeyPress(screen)
            } else {
                if (currentScreen != null) {
                    removeToggleButton()
                    currentScreen = null
                }
            }
        }
    }

    private fun registerSlotRenderEvent() {
        SlotRenderEvents.listen { event ->
            if (!isRunning) return@listen
            onRenderSlot(event.context, event.slot)
        }
    }

    private fun setupToggleButton(screen: HandledScreen<*>) {
        removeToggleButton()

        val highlightKey = SkyFall.feature.inventory.petMenu.favoriteKey
        if (highlightKey == GLFW.GLFW_KEY_UNKNOWN || !isPetMenu(screen)) {
            favoredOnlyToggle = false
            return
        }

        val screenX = screen.x
        val screenY = screen.y
        val backgroundWidth = screen.backgroundWidth

        var buttonX = screenX + backgroundWidth - 16
        val buttonY = screenY + 4

        val existingButtons = Screens.getButtons(screen)
        while (existingButtons.any { it is ButtonWidget && it.x == buttonX && it.y == buttonY }) {
            buttonX -= 15
        }

        val toggleButton = ButtonWidget.builder(
            Text.literal(if (favoredOnlyToggle) "F" else "A")
        ) { _ ->
            favoredOnlyToggle = !favoredOnlyToggle
            saveConfig()
            setupToggleButton(screen)
        }
            .dimensions(buttonX, buttonY, 12, 12)
            .tooltip(Tooltip.of(Text.literal(if (favoredOnlyToggle) "Click to Show All" else "Click for Favorites Only")))
            .build()

        Screens.getButtons(screen).add(toggleButton)
    }

    private fun removeToggleButton() {
        currentScreen?.let { screen ->
            Screens.getButtons(screen).removeIf { widget ->
                widget is ButtonWidget &&
                        (widget.message.string == "F" || widget.message.string == "A")
            }
        }
    }

    private fun handleKeyPress(screen: HandledScreen<*>) {
        val highlightKey = SkyFall.feature.inventory.petMenu.favoriteKey
        if (highlightKey == GLFW.GLFW_KEY_UNKNOWN) {
            return
        }

        if (KeyboardManager.run { highlightKey.isKeyClicked() }) {
            handleKeyPressAction(screen)
        }
    }

    private fun handleKeyPressAction(screen: HandledScreen<*>) {
        val hoveredSlot = getHoveredSlot(screen) ?: return
        val slotIndex = hoveredSlot.index

        if (!isSlotInChestInventory(hoveredSlot) ||
            !validSlotRanges.any { slotIndex in it } ||
            hoveredSlot.stack.isEmpty) {
            return
        }

        if (!PetUtils.isPet(hoveredSlot.stack)) {
            return
        }

        val itemUuid = ItemUtils.getUuid(hoveredSlot.stack)
        if (itemUuid == null) {
            return
        }

        if (highlightedItems.contains(itemUuid)) {
            highlightedItems.remove(itemUuid)
        } else {
            highlightedItems.add(itemUuid)
        }
        saveConfig()
    }

    private fun isPetMenu(screen: HandledScreen<*>): Boolean {
        val title = screen.title.string
        return title.contains("Pets") && !title.contains("Choose Pet") && !title.startsWith("Pets: ") && !title.equals("Offer Pets")
    }

    fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
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

    private fun isExpShareMenu(screen: HandledScreen<*>): Boolean {
        return screen.title.string == "Exp Sharing"
    }

    private fun updateExpSharedPets(screen: HandledScreen<*>) {
        val expShareSlots = setOf(30, 31, 32)
        val currentExpPets = mutableSetOf<String>()

        for (slotIndex in expShareSlots) {
            if (slotIndex >= screen.screenHandler.slots.size) continue

            val slot = screen.screenHandler.getSlot(slotIndex)
            if (!slot.stack.isEmpty) {
                ItemUtils.getUuid(slot.stack)?.let { uuid ->
                    currentExpPets.add(uuid)
                }
            }
        }

        if (expSharedPets != currentExpPets) {
            expSharedPets = currentExpPets
            saveConfig()
        }
    }

    private fun getPetHighlightColor(itemStack: ItemStack, isFavorite: Boolean, isExpShared: Boolean): Int? {
        if (!PetUtils.isPet(itemStack)) {
            return null
        }
        val config = SkyFall.feature.inventory.petMenu

        if (config.activePet && PetUtils.isActivePet(itemStack)) {
            return ACTIVE_PET_COLOR
        }

        if (isFavorite) {
            return FAVORITE_COLOR
        }

        if (config.showXPSharedPets && isExpShared) {
            return XP_SHARED_COLOR
        }

        return null
    }

    fun onRenderSlot(context: DrawContext, slot: Slot) {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen !is HandledScreen<*> || !isPetMenu(currentScreen)) {
            return
        }

        val slotIndex = slot.index
        if (!isSlotInChestInventory(slot) || !validSlotRanges.any { slotIndex in it } || slot.stack.isEmpty) {
            return
        }

        if (!PetUtils.isPet(slot.stack)) {
            return
        }

        val highlightKey = SkyFall.feature.inventory.petMenu.favoriteKey
        val canShowFavorite = highlightKey != GLFW.GLFW_KEY_UNKNOWN

        val itemUuid = ItemUtils.getUuid(slot.stack)
        val isFavorite = canShowFavorite && itemUuid != null && highlightedItems.contains(itemUuid)
        val isExpShared = itemUuid != null && expSharedPets.contains(itemUuid)

        val highlightColor = getPetHighlightColor(slot.stack, isFavorite, isExpShared)
        if (highlightColor != null) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, highlightColor)
        }
    }

    private fun loadConfig() {
        configFile.parentFile.mkdirs()
        if (!configFile.exists() || configFile.length() == 0L) {
            return
        }

        try {
            val jsonString = configFile.readText()
            val trimmedJson = jsonString.trim()

            if (trimmedJson.startsWith("{")) {
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val configData: Map<String, Any> = gson.fromJson(trimmedJson, type) ?: emptyMap()
                highlightedItems = (configData["highlightedItems"] as? List<*>)?.filterIsInstance<String>()?.toMutableSet() ?: mutableSetOf()
                expSharedPets = (configData["expSharedPets"] as? List<*>)?.filterIsInstance<String>()?.toMutableSet() ?: mutableSetOf()
                favoredOnlyToggle = configData["favoredOnlyToggle"] as? Boolean ?: true
            } else if (trimmedJson.startsWith("[")) {
                logger.info("Old favorite pets config format detected. Migrating...")
                val listType = object : TypeToken<List<String>>() {}.type
                val oldFavorites: List<String> = gson.fromJson(trimmedJson, listType) ?: emptyList()

                highlightedItems = oldFavorites.toMutableSet()
                expSharedPets = mutableSetOf()
                favoredOnlyToggle = true

                saveConfig()
                logger.info("Successfully migrated favorite pets config to new format.")
            } else {
                logger.warn("Unknown config format in favorite-pets.json. Resetting.")
            }
        } catch (e: Exception) {
            logger.error("Failed to load favorite pets config. File may be corrupt. Resetting. Error: ${e.message}", e)
        }
    }

    private fun saveConfig() {
        try {
            configFile.parentFile.mkdirs()
            val configData = mapOf(
                "highlightedItems" to highlightedItems,
                "expSharedPets" to expSharedPets,
                "favoredOnlyToggle" to favoredOnlyToggle
            )
            FileWriter(configFile).use { writer ->
                gson.toJson(configData, writer)
            }
        } catch (e: Exception) {
            logger.error("Failed to save favorite pets config: ${e.message}", e)
        }
    }
}
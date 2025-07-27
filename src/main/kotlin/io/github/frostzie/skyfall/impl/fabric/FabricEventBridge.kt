package io.github.frostzie.skyfall.impl.fabric

import io.github.frostzie.skyfall.api.feature.FeatureEventManager
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.render.HudRenderEvent
import io.github.frostzie.skyfall.events.render.SlotClickEvent
import io.github.frostzie.skyfall.events.render.SlotRenderEvent
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Identifier

/**
 * Bridges Fabric API events to our internal event system
 * This is the only place that should directly interact with Fabric APIs
 */
object FabricEventBridge {
    private val logger = LoggerProvider.getLogger("FabricEventBridge")
    private val HUD_LAYER_ID = Identifier.of("skyfall", "event_bridge_hud")
    private var isInitialized = false

    /**
     * Initialize the bridge - register with Fabric APIs
     */
    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        // Initialize feature event manager first
        FeatureEventManager.initializeEventListeners()

        // Register HUD element with Fabric
        registerHudElement()

        logger.info("Fabric event bridge initialized")
    }

    private fun registerHudElement() {
        val hudElement = HudElement { context: DrawContext, tickCounter: RenderTickCounter ->
            val client = MinecraftClient.getInstance()

            // Skip if conditions aren't met
            if (client.player == null || client.options.hudHidden || client.debugHud.shouldShowDebugHud()) {
                return@HudElement
            }

            // Skip if in HUD editor
            if (client.currentScreen?.javaClass?.simpleName == "HudEditorScreen") {
                return@HudElement
            }

            // Emit our events
            EventBus.post(HudRenderEvent.Pre(context, tickCounter))
            EventBus.post(HudRenderEvent.Main(context, tickCounter))
            EventBus.post(HudRenderEvent.Post(context, tickCounter))
        }

        HudElementRegistry.attachElementAfter(
            VanillaHudElements.MISC_OVERLAYS,
            HUD_LAYER_ID,
            hudElement
        )
    }

    /**
     * Call this from your slot rendering mixin
     */
    fun onSlotRenderPre(context: DrawContext, slot: Slot, originalStack: ItemStack) {
        EventBus.post(SlotRenderEvent.Pre(context, slot, originalStack))
    }

    /**
     * Call this from your slot rendering mixin - returns modified render data
     */
    fun onSlotRenderMain(context: DrawContext, slot: Slot, originalStack: ItemStack): SlotRenderEvent.Main {
        val event = SlotRenderEvent.Main(context, slot, originalStack)
        EventBus.post(event)
        return event
    }

    /**
     * Call this from your slot rendering mixin
     */
    fun onSlotRenderPost(context: DrawContext, slot: Slot, originalStack: ItemStack) {
        EventBus.post(SlotRenderEvent.Post(context, slot, originalStack))
    }

    /**
     * Call this from your slot click mixin
     */
    fun onSlotClick(
        slot: Slot?,
        slotId: Int,
        button: Int,
        actionType: SlotActionType,
        screenTitle: String,
        cursorStack: ItemStack,
        originalStack: ItemStack?
    ): SlotClickEvent {
        val event = SlotClickEvent(slot, slotId, button, actionType, screenTitle, cursorStack, originalStack)
        EventBus.post(event)
        return event
    }

    /**
     * Cleanup method - removes Fabric registrations
     */
    fun cleanup() {
        if (!isInitialized) return

        try {
            HudElementRegistry.removeElement(HUD_LAYER_ID)
        } catch (e: Exception) {
            logger.error("Error cleaning up HUD element: ${e.message}")
        }

        isInitialized = false
        logger.info("Fabric event bridge cleaned up")
    }

    private inline fun <T> safeEventCall(action: () -> T): T? {
        return try {
            action()
        } catch (e: Exception) {
            logger.error("Event bridge error: ${e.message}", e)
            null
        }
    }
}
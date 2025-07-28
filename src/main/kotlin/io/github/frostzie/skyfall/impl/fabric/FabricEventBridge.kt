package io.github.frostzie.skyfall.impl.fabric

import io.github.frostzie.skyfall.api.feature.FeatureEventManager
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.render.HudRenderEvent
import io.github.frostzie.skyfall.events.render.SlotClickEvent
import io.github.frostzie.skyfall.events.render.SlotRenderEvent
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
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

        // Register HUD layer with Fabric using the callback system
        registerHudLayer()

        logger.info("Fabric event bridge initialized")
    }

    private fun registerHudLayer() {
        HudLayerRegistrationCallback.EVENT.register { layeredDrawer ->
            val hudLayer = IdentifiedLayer.of(HUD_LAYER_ID) { context: DrawContext, tickCounter: RenderTickCounter ->
                val client = MinecraftClient.getInstance()

                if (client.player == null || client.options.hudHidden || client.debugHud.shouldShowDebugHud()) {
                    return@of
                }

                val currentScreen = client.currentScreen
                if (currentScreen?.javaClass?.simpleName == "HudEditorScreen") {
                    return@of
                }

                safeEventCall {
                    EventBus.post(HudRenderEvent.Pre(context, tickCounter))
                    EventBus.post(HudRenderEvent.Main(context, tickCounter))
                    EventBus.post(HudRenderEvent.Post(context, tickCounter))
                }
            }

            // Attach after MISC_OVERLAYS (same position as before)
            layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, hudLayer)
        }
    }

    /**
     * Call this from your slot rendering mixin
     */
    fun onSlotRenderPre(context: DrawContext, slot: Slot, originalStack: ItemStack) {
        safeEventCall {
            EventBus.post(SlotRenderEvent.Pre(context, slot, originalStack))
        }
    }

    /**
     * Call this from your slot rendering mixin - returns modified render data
     */
    fun onSlotRenderMain(context: DrawContext, slot: Slot, originalStack: ItemStack): SlotRenderEvent.Main {
        return safeEventCall {
            val event = SlotRenderEvent.Main(context, slot, originalStack)
            EventBus.post(event)
            event
        } ?: SlotRenderEvent.Main(context, slot, originalStack)
    }

    /**
     * Call this from your slot rendering mixin
     */
    fun onSlotRenderPost(context: DrawContext, slot: Slot, originalStack: ItemStack) {
        safeEventCall {
            EventBus.post(SlotRenderEvent.Post(context, slot, originalStack))
        }
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
        return safeEventCall {
            val event = SlotClickEvent(slot, slotId, button, actionType, screenTitle, cursorStack, originalStack)
            EventBus.post(event)
            event
        } ?: SlotClickEvent(slot, slotId, button, actionType, screenTitle, cursorStack, originalStack)
    }

    /**
     * Cleanup method - removes Fabric registrations
     * Note: In the older system, layers are registered via callback and cannot be easily removed
     * This method is kept for API compatibility but has limited functionality
     */
    fun cleanup() {
        if (!isInitialized) return

        // In the older Fabric rendering system, there's no direct way to remove registered layers
        // The layers are registered via callbacks during initialization
        // This is a limitation of the older system compared to the newer HudElementRegistry

        isInitialized = false
        logger.info("Fabric event bridge cleaned up (note: layer removal not supported in older Fabric versions)")
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
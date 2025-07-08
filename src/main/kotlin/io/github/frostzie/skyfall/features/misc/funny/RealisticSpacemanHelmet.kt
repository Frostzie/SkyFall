package io.github.frostzie.skyfall.features.misc.funny
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.features.Feature
import io.github.frostzie.skyfall.features.IFeature
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import io.github.frostzie.skyfall.utils.item.ItemUtils
import io.github.frostzie.skyfall.utils.item.SkyBlockItemData
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.util.Identifier

@Feature(name = "Realistic Spaceman Helmet")
object RealisticSpacemanHelmet : IFeature {

    override var isRunning = false

    private val config get() = SkyFall.feature.miscFeatures.funny.spacemanHelmetConfig
    private val COLORS = listOf(
        Identifier.ofVanilla("textures/block/red_stained_glass.png"),
        Identifier.ofVanilla("textures/block/orange_stained_glass.png"),
        Identifier.ofVanilla("textures/block/yellow_stained_glass.png"),
        Identifier.ofVanilla("textures/block/lime_stained_glass.png"),
        Identifier.ofVanilla("textures/block/green_stained_glass.png"),
        Identifier.ofVanilla("textures/block/cyan_stained_glass.png"),
        Identifier.ofVanilla("textures/block/light_blue_stained_glass.png"),
        Identifier.ofVanilla("textures/block/blue_stained_glass.png"),
        Identifier.ofVanilla("textures/block/purple_stained_glass.png"),
        Identifier.ofVanilla("textures/block/magenta_stained_glass.png"),
        Identifier.ofVanilla("textures/block/pink_stained_glass.png")
    )

    private var lastUpdateTime = SimpleTimeMark.now()
    private var currentColorIndex = 0
    private val HUD_ELEMENT_ID = Identifier.of("skyfall", "spaceman_helmet_overlay")

    override fun shouldLoad(): Boolean {
        return config.realisticSpacemanHelmet
    }

    override fun init() {
        if (isRunning) return
        isRunning = true

        val hudElement = HudElement { context, _ ->
            renderOverlay(context)
        }

        HudElementRegistry.attachElementAfter(
            VanillaHudElements.MISC_OVERLAYS,
            HUD_ELEMENT_ID,
            hudElement
        )
    }

    override fun terminate() {
        if (!isRunning) return
        isRunning = false

        HudElementRegistry.removeElement(HUD_ELEMENT_ID)
    }

    private fun isWearing(): Identifier? {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return null
        val onHead = player.getEquippedStack(EquipmentSlot.HEAD)

        if (onHead.isEmpty) return null

        val skyblockId = ItemUtils.getSkyblockId(onHead)
        if (skyblockId == "DCTR_SPACE_HELM") {
            val glassColors = mapOf(
                "minecraft:red_stained_glass" to COLORS[0],
                "minecraft:orange_stained_glass" to COLORS[1],
                "minecraft:yellow_stained_glass" to COLORS[2],
                "minecraft:lime_stained_glass" to COLORS[3],
                "minecraft:green_stained_glass" to COLORS[4],
                "minecraft:cyan_stained_glass" to COLORS[5],
                "minecraft:light_blue_stained_glass" to COLORS[6],
                "minecraft:blue_stained_glass" to COLORS[7],
                "minecraft:purple_stained_glass" to COLORS[8],
                "minecraft:magenta_stained_glass" to COLORS[9],
                "minecraft:pink_stained_glass" to COLORS[10]
            )

            for ((itemId, texture) in glassColors) {
                if (SkyBlockItemData.isItem(onHead, itemId)) {
                    return texture
                }
            }
        }
        return null
    }

    private fun renderOverlay(context: DrawContext) {
        if (!isRunning) return

        if (lastUpdateTime.passedSince().inWholeMilliseconds >= 250) {
            lastUpdateTime = SimpleTimeMark.now()
            currentColorIndex = (currentColorIndex + 1) % COLORS.size
        }
        val currentColor = if (config.onlyIfEquipped) {
            isWearing()
        } else {
            COLORS[currentColorIndex]
        }

        currentColor?.let { textureId ->
            val width = context.scaledWindowWidth
            val height = context.scaledWindowHeight

            // The old drawTexture method is gone. The new API requires you to pass
            // a RenderPipeline to tell the game how to draw the texture.
            // For a transparent overlay, ENTITY_TRANSLUCENT is the correct pipeline.
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                0,      // x
                0,      // y
                0.0f,   // u
                0.0f,   // v
                width,  // width
                height, // height
                width,  // textureWidth
                height  // textureHeight
            )
        }
    }
}
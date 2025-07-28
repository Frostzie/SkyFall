package io.github.frostzie.skyfall.features.garden.map

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.features.garden.GardenConfig
import io.github.frostzie.skyfall.data.GardenPlot
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.hud.FeatureHudElement
import io.github.frostzie.skyfall.hud.HudElementConfig
import io.github.frostzie.skyfall.hud.HudManager
import io.github.frostzie.skyfall.impl.minecraft.SkyfallRenderPipelines.Gui.GUI_TEXTURED
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.garden.PestData
import io.github.frostzie.skyfall.utils.garden.PestDetector
import io.github.frostzie.skyfall.utils.garden.VisitorUtils
import io.github.frostzie.skyfall.utils.garden.SprayUtils
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import kotlin.math.min

@Feature(name = "Garden Map")
object GardenMap : IEventFeature {

    override var isRunning = false
    private val config get() = SkyFall.feature.garden.gardenMap

    private val PLOT_NUMBER_MAP = listOf(
        listOf(21, 13,  9, 14, 22),
        listOf(15,  5,  1,  6, 16),
        listOf(10,  2,  0,  3, 11),
        listOf(17,  7,  4,  8, 18),
        listOf(23, 19, 12, 20, 24)
    )
    private const val WORLD_MIN_X = -240.0
    private const val WORLD_MAX_X = 240.0
    private const val WORLD_MIN_Z = -240.0
    private const val WORLD_MAX_Z = 240.0
    private const val WORLD_SIZE_X = WORLD_MAX_X - WORLD_MIN_X
    private const val WORLD_SIZE_Z = WORLD_MAX_Z - WORLD_MIN_Z

    override fun shouldLoad(): Boolean = config.enabled

    override fun init() {
        if (isRunning) return
        isRunning = true

        PestDetector.init()
        VisitorUtils.init()

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (!overlay && IslandDetector.isOnIsland(IslandType.GARDEN)) {
                SprayUtils.processChatMessage(message.string)
            }
            true
        }

        HudManager.registerElement(
            FeatureHudElement(
                id = "skyfall:garden_map",
                name = "Garden Map",
                defaultConfig = HudElementConfig(x = 10, y = 10, width = 120, height = 145),
                advancedSizingOverride = false,
                minWidthOverride = 80,
                minHeightOverride = 100,
                renderAction = { drawContext, element ->
                    renderHud(
                        drawContext,
                        element.config.x,
                        element.config.y,
                        element.config.width,
                        element.config.height
                    )
                }
            )
        )
    }

    override fun terminate() {
        if (!isRunning) return
        isRunning = false
        HudManager.unregisterElement("skyfall:garden_map")
    }

    private fun renderHud(drawContext: DrawContext, x: Int, y: Int, width: Int, height: Int) {
        val client = MinecraftClient.getInstance()
        if (!IslandDetector.isOnIsland(IslandType.GARDEN) || client.player == null) {
            return
        }

        val player = client.player!!
        val textRenderer = client.textRenderer

        val pestData = PestDetector.getPestData()
        val visitorData = VisitorUtils.getVisitorData()
        val backgroundColor = ColorUtils.parseColorString(config.backgroundColor)
        val headerTextColor = ColorUtils.parseColorString(config.textColor)

        drawContext.fill(x, y, x + width, y + height, backgroundColor)

        val padding = 3
        val textHeight = textRenderer.fontHeight
        val headerHeight = textHeight

        val pestText = "§lPests: ${pestData.totalAlive}"
        val visitorText = "§lVisitors: ${visitorData.count}"

        drawContext.drawText(textRenderer, pestText, x + padding, y + padding, headerTextColor, false)
        drawContext.drawText(textRenderer, visitorText, x + padding, y + padding + textHeight, headerTextColor, false)

        val gridAvailableWidth = width - (padding + 2)
        val gridAvailableHeight = height - headerHeight - padding
        val gridContentSize = min(gridAvailableWidth, gridAvailableHeight)
        if (gridContentSize < 20) return

        val gridStartX = x + padding + (gridAvailableWidth - gridContentSize) / 2
        val gridStartY = y + height - padding - gridContentSize
        val gridCount = 5
        val gapSize = 1
        val smallBoxSize = (gridContentSize - (gapSize * (gridCount - 1))) / gridCount

        val sprayedPlots: List<GardenPlot> = SprayUtils.getSprayedPlots()

        if (smallBoxSize > 0) {
            renderGrid(
                drawContext,
                gridStartX,
                gridStartY,
                smallBoxSize,
                gapSize,
                gridCount,
                pestData,
                sprayedPlots,
                textRenderer
            )
            drawPlayerLocation(drawContext, player.x, player.z, gridStartX, gridStartY, gridContentSize, gridContentSize)
        }
    }

    private fun renderGrid(
        drawContext: DrawContext,
        gridStartX: Int,
        gridStartY: Int,
        smallBoxSize: Int,
        gapSize: Int,
        gridCount: Int,
        pestData: PestData,
        sprayedPlots: List<GardenPlot>,
        textRenderer: TextRenderer
    ) {
        val sprayedColor = ColorUtils.parseColorString(config.sprayColor)
        val pestColor = ColorUtils.parseColorString(config.pestColor)
        val defaultColor = ColorUtils.parseColorString(config.defaultPlotColor)

        for (row in 0 until gridCount) {
            for (col in 0 until gridCount) {
                val x = gridStartX + col * (smallBoxSize + gapSize)
                val y = gridStartY + row * (smallBoxSize + gapSize)
                val plotNumber = PLOT_NUMBER_MAP[row][col]
                val hasPest = pestData.infestedPlots.contains(plotNumber)
                val isSprayed = sprayedPlots.any { it.id == plotNumber }

                if (isSprayed) {
                    if (hasPest) {
                        val halfHeight = smallBoxSize / 2
                        drawContext.fill(x, y, x + smallBoxSize, y + halfHeight, sprayedColor)
                        drawContext.fill(x, y + halfHeight, x + smallBoxSize, y + smallBoxSize, pestColor)
                    } else {
                        drawContext.fill(x, y, x + smallBoxSize, y + smallBoxSize, sprayedColor)
                    }
                } else {
                    val boxColor = if (hasPest) pestColor else defaultColor
                    drawContext.fill(x, y, x + smallBoxSize, y + smallBoxSize, boxColor)
                }

                if (plotNumber > 0) {
                    renderPlotNumber(drawContext, textRenderer, x, y, smallBoxSize, plotNumber)
                }
            }
        }
    }

    private fun renderPlotNumber(
        drawContext: DrawContext,
        textRenderer: TextRenderer,
        x: Int,
        y: Int,
        smallBoxSize: Int,
        plotNumber: Int
    ) {
        if (smallBoxSize < 12) return
        val numberText = "$plotNumber"
        val plotTextColor = ColorUtils.parseColorString(config.plotTextColor)
        val textWidth = textRenderer.getWidth(numberText)
        val textX = x + smallBoxSize - textWidth - 2
        val textY = y + smallBoxSize - textRenderer.fontHeight - 2
        drawContext.drawText(textRenderer, numberText, textX, textY, plotTextColor, false)
    }

    private fun drawPlayerLocation(
        drawContext: DrawContext,
        playerX: Double,
        playerZ: Double,
        mapStartX: Int,
        mapStartY: Int,
        mapWidth: Int,
        mapHeight: Int
    ) {
        val client = MinecraftClient.getInstance()
        val clampedX = playerX.coerceIn(WORLD_MIN_X, WORLD_MAX_X)
        val clampedZ = playerZ.coerceIn(WORLD_MIN_Z, WORLD_MAX_Z)
        val normalizedX = (clampedX - WORLD_MIN_X) / WORLD_SIZE_X
        val normalizedZ = (clampedZ - WORLD_MIN_Z) / WORLD_SIZE_Z
        val pixelX = mapStartX + (normalizedX * mapWidth).toInt()
        val pixelZ = mapStartY + (normalizedZ * mapHeight).toInt()
        val baseSize = (mapWidth / 40.0).coerceIn(2.0, 7.0)
        val dotSize = (baseSize * 4).toInt()

        if (config.playerIconType == GardenConfig.GardenMapConfig.PlayerIcon.ARROW) {
            val texture = Identifier.ofVanilla("textures/map/decorations/player.png")
            drawContext.matrices.push()
            drawContext.matrices.translate(pixelX.toFloat(), pixelZ.toFloat(), 0f)
            drawContext.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(client.player!!.yaw - 360))
            drawContext.matrices.translate(pixelX.toFloat(), pixelZ.toFloat(), 0f)
            drawContext.drawTexture(
                { tex: Identifier -> RenderLayer.getGuiTexturedOverlay(tex)},
                texture,
                (pixelX - dotSize / 2),
                (pixelZ - dotSize / 2),
                0.0f,
                0.0f,
                dotSize,
                dotSize,
                dotSize,
                -dotSize
            )
            drawContext.matrices.pop()
        } else {
            val playerColor = ColorUtils.parseColorString(config.playerIconColor)
            drawContext.fill(
                (pixelX - dotSize / 4),
                (pixelZ - dotSize / 4),
                (pixelX + dotSize / 4) + 1,
                (pixelZ + dotSize / 4) + 1,
                playerColor
            )
        }
    }
}
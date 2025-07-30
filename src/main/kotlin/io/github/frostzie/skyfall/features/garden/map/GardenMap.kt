package io.github.frostzie.skyfall.features.garden.map

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.features.garden.GardenConfig
import io.github.frostzie.skyfall.data.GardenPlot
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.HudFeature
import io.github.frostzie.skyfall.hud.HudElement
import io.github.frostzie.skyfall.hud.HudElementConfig
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
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import kotlin.math.min

@Feature(name = "Garden Map")
object GardenMap : HudFeature() {

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

    private const val GRID_COUNT = 5
    private const val GAP_SIZE = 1
    private const val PADDING = 3
    private const val PLAYER_ICON_BASE_SIZE_DIVISOR = 40.0
    private const val PLAYER_ICON_MIN_SIZE = 2.0
    private const val PLAYER_ICON_MAX_SIZE = 7.0
    private const val PLAYER_DOT_SIZE_MULTIPLIER = 4
    private val PLAYER_ARROW_TEXTURE = Identifier.ofVanilla("textures/map/decorations/player.png")

    override fun shouldLoad(): Boolean = config.enabled
    override val isMovable: Boolean = true

    override val elementId: String = "skyfall:garden_map"
    override val elementName: String = "Garden Map"
    override val defaultElementConfig = HudElementConfig(x = 10, y = 10, width = 120, height = 145)
    override val elementMinWidth: Int = 80
    override val elementMinHeight: Int = 100

    override fun onInit() {
        PestDetector.init()
        VisitorUtils.init()

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (!overlay && IslandDetector.isOnIsland(IslandType.GARDEN)) {
                SprayUtils.processChatMessage(message.string)
            }
            true
        }
    }

    override fun onTerminate() {}

    override fun onMovableHudRender(drawContext: DrawContext, element: HudElement) {
        val client = MinecraftClient.getInstance()
        if (!IslandDetector.isOnIsland(IslandType.GARDEN) || client.player == null) {
            return
        }

        val player = client.player!!
        val textRenderer = client.textRenderer
        val config = element.config // Get current position and size from the element

        val pestData = PestDetector.getPestData()
        val visitorData = VisitorUtils.getVisitorData()
        val backgroundColor = ColorUtils.parseColorString(this.config.backgroundColor)
        val headerTextColor = ColorUtils.parseColorString(this.config.textColor)

        drawContext.fill(config.x, config.y, config.x + config.width, config.y + config.height, backgroundColor)

        val textHeight = textRenderer.fontHeight
        val headerHeight = textHeight * 2 + PADDING // Correctly calculate height for two lines

        val pestText = "§lPests: ${pestData.totalAlive}"
        val visitorText = "§lVisitors: ${visitorData.count}"

        drawContext.drawText(textRenderer, pestText, config.x + PADDING, config.y + PADDING, headerTextColor, false)
        drawContext.drawText(textRenderer, visitorText, config.x + PADDING, config.y + PADDING + textHeight, headerTextColor, false)

        val gridAvailableWidth = config.width - (PADDING * 2)
        val gridAvailableHeight = config.height - headerHeight - (PADDING * 2)
        val gridContentSize = min(gridAvailableWidth, gridAvailableHeight)
        if (gridContentSize < 20) return // Don't render if too small

        val gridStartX = config.x + PADDING + (gridAvailableWidth - gridContentSize) / 2
        val gridStartY = config.y + config.height - PADDING - gridContentSize
        val smallBoxSize = (gridContentSize - (GAP_SIZE * (GRID_COUNT - 1))) / GRID_COUNT

        if (smallBoxSize > 0) {
            renderGrid(drawContext, gridStartX, gridStartY, smallBoxSize, pestData, textRenderer)
            drawPlayerLocation(drawContext, player, gridStartX, gridStartY, gridContentSize)
        }
    }

    private fun renderGrid(
        drawContext: DrawContext,
        gridStartX: Int,
        gridStartY: Int,
        smallBoxSize: Int,
        pestData: PestData,
        textRenderer: TextRenderer
    ) {
        val sprayedPlots: List<GardenPlot> = SprayUtils.getSprayedPlots()
        val sprayedColor = ColorUtils.parseColorString(config.sprayColor)
        val pestColor = ColorUtils.parseColorString(config.pestColor)
        val defaultColor = ColorUtils.parseColorString(config.defaultPlotColor)

        for (row in 0 until GRID_COUNT) {
            for (col in 0 until GRID_COUNT) {
                val x = gridStartX + col * (smallBoxSize + GAP_SIZE)
                val y = gridStartY + row * (smallBoxSize + GAP_SIZE)
                val plotNumber = PLOT_NUMBER_MAP[row][col]
                val hasPest = pestData.infestedPlots.contains(plotNumber)
                val isSprayed = sprayedPlots.any { it.id == plotNumber }

                val boxColor = when {
                    isSprayed && hasPest -> null // Special case for split color
                    isSprayed -> sprayedColor
                    hasPest -> pestColor
                    else -> defaultColor
                }

                if (boxColor != null) {
                    drawContext.fill(x, y, x + smallBoxSize, y + smallBoxSize, boxColor)
                } else {
                    val halfHeight = smallBoxSize / 2
                    drawContext.fill(x, y, x + smallBoxSize, y + halfHeight, sprayedColor)
                    drawContext.fill(x, y + halfHeight, x + smallBoxSize, y + smallBoxSize, pestColor)
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
        player: ClientPlayerEntity,
        mapStartX: Int,
        mapStartY: Int,
        mapSize: Int
    ) {
        val client = MinecraftClient.getInstance()
        val clampedX = player.x.coerceIn(WORLD_MIN_X, WORLD_MAX_X)
        val clampedZ = player.z.coerceIn(WORLD_MIN_Z, WORLD_MAX_Z)
        val normalizedX = (clampedX - WORLD_MIN_X) / WORLD_SIZE_X
        val normalizedZ = (clampedZ - WORLD_MIN_Z) / WORLD_SIZE_Z
        val pixelX = mapStartX + (normalizedX * mapSize).toInt()
        val pixelZ = mapStartY + (normalizedZ * mapSize).toInt()

        val baseSize = (mapSize / PLAYER_ICON_BASE_SIZE_DIVISOR).coerceIn(PLAYER_ICON_MIN_SIZE, PLAYER_ICON_MAX_SIZE)
        val iconSize = (baseSize * PLAYER_DOT_SIZE_MULTIPLIER).toInt()

        if (config.playerIconType == GardenConfig.GardenMapConfig.PlayerIcon.ARROW) {
            val matrices = drawContext.matrices
            matrices.pushMatrix()
            matrices.translate(pixelX.toFloat(), pixelZ.toFloat())
            matrices.rotate(kotlin.math.PI.toFloat() * (client.player!!.yaw - 360) / 180.0f)
            matrices.translate(-pixelX.toFloat(), -pixelZ.toFloat())
            drawContext.drawTexture(
                GUI_TEXTURED,
                PLAYER_ARROW_TEXTURE,
                (pixelX - iconSize / 2),
                (pixelZ - iconSize / 2),
                0.0f,
                0.0f,
                iconSize,
                iconSize,
                iconSize,
                -iconSize
            )
            drawContext.matrices.popMatrix()
        } else {
            val playerColor = ColorUtils.parseColorString(config.playerIconColor)
            drawContext.fill(
                (pixelX - iconSize / 4),
                (pixelZ - iconSize / 4),
                (pixelX + iconSize / 4) + 1,
                (pixelZ + iconSize / 4) + 1,
                playerColor
            )
        }
    }
}
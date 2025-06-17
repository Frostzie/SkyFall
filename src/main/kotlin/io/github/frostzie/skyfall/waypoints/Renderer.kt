package io.github.frostzie.skyfall.waypoints

import io.github.notenoughupdates.moulconfig.platform.next
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

object Renderer {
    fun renderBlock(
        context: WorldRenderContext,
        pos: Vec3d,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        throughWalls: Boolean = true,
        useBlockShape: Boolean = false
    ) {
        val matrices = context.matrixStack()
        val camera = context.camera().pos
        val world = context.world()
        val blockPos = BlockPos(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
        val blockState: BlockState? = world?.getBlockState(blockPos)

        val waypointMin = Vec3d(pos.x - 0.5, pos.y - 0.5, pos.z - 0.5)
        val waypointMax = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        val isCameraInside = camera.x in waypointMin.x..waypointMax.x &&
                camera.y in waypointMin.y..waypointMax.y &&
                camera.z in waypointMin.z..waypointMax.z

        matrices?.push()
        matrices?.translate(-camera.x, -camera.y, -camera.z)

        val consumers = context.consumers() as VertexConsumerProvider.Immediate

        val buffer = if (throughWalls && !isCameraInside) {
            consumers.getBuffer(SimpleRenderLayers.BLOCK_NO_CULL)
        } else {
            consumers.getBuffer(SimpleRenderLayers.BLOCK)
        }

        val normalizedRed = red / 255f
        val normalizedGreen = green / 255f
        val normalizedBlue = blue / 255f

        val shape: VoxelShape = if (useBlockShape && blockState != null && !blockState.isAir) {
            blockState.getOutlineShape(world, blockPos)
        } else {
            VoxelShapes.fullCube()
        }

        for (box in shape.boundingBoxes) {
            renderWallsOnly(
                buffer,
                matrices,
                pos.x + box.minX,
                pos.y + box.minY,
                pos.z + box.minZ,
                pos.x + box.maxX,
                pos.y + box.maxY,
                pos.z + box.maxZ,
                normalizedRed,
                normalizedGreen,
                normalizedBlue,
                alpha
            )
        }

        consumers.draw(if (throughWalls && !isCameraInside) SimpleRenderLayers.BLOCK_NO_CULL else SimpleRenderLayers.BLOCK)

        matrices?.pop()
    }

    fun renderArea(
        context: WorldRenderContext,
        startPos: Vec3d,
        endPos: Vec3d,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        throughWalls: Boolean = true
    ) {
        val matrices = context.matrixStack()
        val camera = context.camera().pos

        val minX = startPos.x.coerceAtMost(endPos.x)
        val minY = startPos.y.coerceAtMost(endPos.y)
        val minZ = startPos.z.coerceAtMost(endPos.z)
        val maxX = startPos.x.coerceAtLeast(endPos.x)
        val maxY = startPos.y.coerceAtLeast(endPos.y)
        val maxZ = startPos.z.coerceAtLeast(endPos.z)

        val isCameraInside = camera.x in minX..maxX &&
                camera.y in minY..maxY &&
                camera.z in minZ..maxZ

        matrices?.push()
        matrices?.translate(-camera.x, -camera.y, -camera.z)

        val consumers = context.consumers() as VertexConsumerProvider.Immediate

        val buffer = if (throughWalls && !isCameraInside) {
            consumers.getBuffer(SimpleRenderLayers.AREA_NO_CULL)
        } else {
            consumers.getBuffer(SimpleRenderLayers.AREA)
        }

        renderWallsOnly(
            buffer,
            matrices,
            minX,
            minY,
            minZ,
            maxX,
            maxY,
            maxZ,
            red / 255f,
            green / 255f,
            blue / 255f,
            alpha
        )

        consumers.draw(if (throughWalls && !isCameraInside) SimpleRenderLayers.AREA_NO_CULL else SimpleRenderLayers.AREA)

        matrices?.pop()
    }

    private fun renderWallsOnly(
        buffer: VertexConsumer,
        matrices: net.minecraft.client.util.math.MatrixStack?,
        minX: Double,
        minY: Double,
        minZ: Double,
        maxX: Double,
        maxY: Double,
        maxZ: Double,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val matrix = matrices?.peek()?.positionMatrix ?: return

        val x1 = minX.toFloat()
        val y1 = minY.toFloat()
        val z1 = minZ.toFloat()
        val x2 = maxX.toFloat()
        val y2 = maxY.toFloat()
        val z2 = maxZ.toFloat()

        // Bottom face (Y = minY) - both sides
        // Front facing
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()

        // Back facing
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()

        // Top face (Y = maxY) - both sides
        // Front facing
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()

        // Back facing
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

        // North face (Z = minZ) - both sides
        // Front facing
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

        // Back facing
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()

        // South face (Z = maxZ) - both sides
        // Front facing
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()

        // Back facing
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()

        // West face (X = minX) - both sides
        // Front facing
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

        // Back facing
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()

        // East face (X = maxX) - both sides
        // Front facing
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()

        // Back facing
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()

        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
    }
}
package io.github.frostzie.skyfall.util.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.frostzie.skyfall.core.BoxStyle
import io.github.frostzie.skyfall.core.MobStore
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

// Credit to PolyHitbox couldn't have figured this out without them
// https://github.com/Polyfrost/PolyHitbox/blob/main/src/main/kotlin/org/polyfrost/polyhitbox/render/HitboxRenderer.kt

//TODO: add culling
object HitboxUtils {
    private val mc = Minecraft.getInstance()
    private const val OFF_SET = 0.00005f

    fun submitBoxes(collector: SubmitNodeCollector) {
        val tasks = MobStore.boxes
        if (tasks.isEmpty()) return

        val cam = mc.entityRenderDispatcher.camera ?: return
        val camPos = cam.position()
        val partial = mc.deltaTracker.getGameTimeDeltaPartialTick(false).toDouble()

        val wantsFilled = tasks.any { it.style == BoxStyle.FILLED || it.style == BoxStyle.FILLED_OUTLINE }
        val wantsOutlined = tasks.any { it.style == BoxStyle.OUTLINE || it.style == BoxStyle.FILLED_OUTLINE }

        if (wantsFilled) {
            collector.submitCustomGeometry(PoseStack(), RenderTypes.debugQuads()) { _, vc ->
                for (i in tasks) {
                    if (i.style != BoxStyle.FILLED && i.style != BoxStyle.FILLED_OUTLINE) continue

                    val pos = boxPositions(i.entity, camPos, partial) ?: continue

                    fillBox(
                        vc,
                        pos.minX, pos.minY, pos.minZ,
                        pos.maxX + OFF_SET, pos.maxY + OFF_SET, pos.maxZ + OFF_SET,
                        i.fillColor
                    )
                }
            }
        }

        if (wantsOutlined) {
            collector.submitCustomGeometry(PoseStack(), RenderTypes.LINES) { pose, vc ->
                for (i in tasks) {
                    if (i.style != BoxStyle.OUTLINE && i.style != BoxStyle.FILLED_OUTLINE) continue

                    val pos = boxPositions(i.entity, camPos, partial) ?: continue

                    lineBox(
                        pose, vc,
                        pos.minX, pos.minY, pos.minZ,
                        pos.maxX + OFF_SET, pos.maxY + OFF_SET, pos.maxZ + OFF_SET,
                        i.outlineColor
                    )
                }
            }
        }
    }

    private fun fillBox(
        vc: VertexConsumer,
        minX: Double, minY: Double, minZ: Double,
        maxX: Double, maxY: Double, maxZ: Double,
        argb: Int,
    ) {
        quad(vc, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, argb)
        quad(vc, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, argb)
        quad(vc, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, argb)
        quad(vc, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, argb)
        quad(vc, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, argb)
        quad(vc, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, argb)
    }

    private fun quad(
        vc: VertexConsumer,
        x1: Double, y1: Double, z1: Double,
        x2: Double, y2: Double, z2: Double,
        x3: Double, y3: Double, z3: Double,
        x4: Double, y4: Double, z4: Double,
        argb: Int,
    ) {
        vc.addVertex(x1.toFloat(), y1.toFloat(), z1.toFloat()).setColor(argb)
        vc.addVertex(x2.toFloat(), y2.toFloat(), z2.toFloat()).setColor(argb)
        vc.addVertex(x3.toFloat(), y3.toFloat(), z3.toFloat()).setColor(argb)
        vc.addVertex(x4.toFloat(), y4.toFloat(), z4.toFloat()).setColor(argb)
    }

    // Credit to Odin
    // https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/utils/render/RenderUtils.kt
    private fun lineBox(
        pose: PoseStack.Pose, vc: VertexConsumer,
        minX: Double, minY: Double, minZ: Double,
        maxX: Double, maxY: Double, maxZ: Double,
        argb: Int
    ) {
        val corners = floatArrayOf(
            minX.toFloat(), minY.toFloat(), minZ.toFloat(),
            maxX.toFloat(), minY.toFloat(), minZ.toFloat(),
            maxX.toFloat(), maxY.toFloat(), minZ.toFloat(),
            minX.toFloat(), maxY.toFloat(), minZ.toFloat(),
            minX.toFloat(), minY.toFloat(), maxZ.toFloat(),
            maxX.toFloat(), minY.toFloat(), maxZ.toFloat(),
            maxX.toFloat(), maxY.toFloat(), maxZ.toFloat(),
            minX.toFloat(), maxY.toFloat(), maxZ.toFloat(),
        )
        val edges = intArrayOf(
            0, 1, 1, 5, 5, 4, 4, 0,
            3, 2, 2, 6, 6, 7, 7, 3,
            0, 3, 1, 2, 5, 6, 4, 7
        )

        for (i in edges.indices step 2) {
            val i0 = edges[i] * 3
            val i1 = edges[i + 1] * 3
            val dx = corners[i1] - corners[i0]
            val dy = corners[i1 + 1] - corners[i0 + 1]
            val dz = corners[i1 + 2] - corners[i0 + 2]

            vc.addVertex(pose, corners[i0], corners[i0 + 1], corners[i0 + 2])
                .setColor(argb)
                .setNormal(pose, dx, dy, dz)
                .setLineWidth(3f)

            vc.addVertex(pose, corners[i1], corners[i1 + 1], corners[i1 + 2])
                .setColor(argb)
                .setNormal(pose, dx, dy, dz)
                .setLineWidth(3f)
        }
    }


    private fun boxPositions(entity: Entity, camPos: Vec3, partial: Double): BoxPositions? {
        if (!entity.isAlive) return null

        val px = Mth.lerp(partial, entity.xo, entity.x)
        val py = Mth.lerp(partial, entity.yo, entity.y)
        val pz = Mth.lerp(partial, entity.zo, entity.z)
        val bb = entity.boundingBox
        val dx = px - entity.x - camPos.x
        val dy = py - entity.y - camPos.y
        val dz = pz - entity.z - camPos.z

        return BoxPositions(
            bb.minX + dx, bb.minY + dy, bb.minZ + dz,
            bb.maxX + dx, bb.maxY + dy, bb.maxZ + dz,
        )
    }

    private data class BoxPositions(
        val minX: Double, val minY: Double, val minZ: Double,
        val maxX: Double, val maxY: Double, val maxZ: Double
    )
}
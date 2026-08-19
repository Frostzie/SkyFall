package io.github.frostzie.skyfall.feature

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.frostzie.skyfall.SkyFall
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import kotlin.math.sqrt

// Taken from PolyHitboxes originally but heavily edited
object CustomHitbox {
    private const val CULL_MARGIN = 0.5

    private var camX = 0.0
    private var camY = 0.0
    private var camZ = 0.0

    private var fwdX = 0.0
    private var fwdY = 0.0
    private var fwdZ = 0.0

    private var partialTicks = 0f

    private var lastFrameKey = Double.NaN

    private var cullFrustum: Frustum? = null
    private var viewer: Player? = null
    private var selfInFirstPerson: Entity? = null

    private val config get() = SkyFall.features.hitbox

    private fun readCamera(camera: Camera) {
        val pos = camera.position()
        camX = pos.x
        camY = pos.y
        camZ = pos.z
        val forward = camera.forwardVector()
        fwdX = forward.x().toDouble()
        fwdY = forward.y().toDouble()
        fwdZ = forward.z().toDouble()
    }

    private fun partialTick(): Float = Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(false)

    private fun quadsType() = RenderTypes.debugQuads()

    private fun beginFrame(cull: Frustum?): Boolean {
        if (!config.enabled) return false
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return false
        val player = mc.player ?: return false
        val camera = mc.entityRenderDispatcher.camera ?: return false
        readCamera(camera)
        partialTicks = partialTick()
        val frameKey = level.gameTime.toDouble() + partialTicks
        if (frameKey != lastFrameKey) {
            lastFrameKey = frameKey
        }
        cullFrustum = cull
        viewer = player
        selfInFirstPerson = if (mc.options.cameraType.isFirstPerson) mc.cameraEntity else null
        return true
    }

    private fun drawLevel(vc: VertexConsumer) {
        val level = Minecraft.getInstance().level ?: return
        val player = viewer ?: return
        for (entity in level.entitiesForRendering()) {
            if (entity === selfInFirstPerson || entity.isInvisible) continue
            if (culled(entity)) continue
            if (entity.isInvisibleTo(player)) continue
            drawEntity(vc, entity)
        }
    }

    private fun culled(entity: Entity): Boolean {
        val bb = entity.boundingBox
        if (bb.hasNaN()) return true
        val ex = (bb.minX + bb.maxX) * 0.5 - camX
        val ey = (bb.minY + bb.maxY) * 0.5 - camY
        val ez = (bb.minZ + bb.maxZ) * 0.5 - camZ
        if (!entity.shouldRenderAtSqrDistance(ex * ex + ey * ey + ez * ez)) return true
        val reach = CULL_MARGIN
        val rx = (bb.maxX - bb.minX) * 0.5
        val ry = (bb.maxY - bb.minY) * 0.5
        val rz = (bb.maxZ - bb.minZ) * 0.5
        val margin = sqrt(rx * rx + ry * ry + rz * rz) + reach
        if (ex * fwdX + ey * fwdY + ez * fwdZ < -margin) return true
        val frustum = cullFrustum
        return frustum != null && !frustum.isVisible(bb.inflate(reach))
    }


    private fun drawEntity(vc: VertexConsumer, entity: Entity) {
        val delta = partialTicks.toDouble()
        val px = Mth.lerp(delta, entity.xo, entity.x)
        val py = Mth.lerp(delta, entity.yo, entity.y)
        val pz = Mth.lerp(delta, entity.zo, entity.z)
        val bb = entity.boundingBox
        val dx = px - entity.x - camX
        val dy = py - entity.y - camY
        val dz = pz - entity.z - camZ
        val minX = bb.minX + dx
        val minY = bb.minY + dy
        val minZ = bb.minZ + dz
        val maxX = bb.maxX + dx
        val maxY = bb.maxY + dy
        val maxZ = bb.maxZ + dz

        fillBox(vc, minX, minY, minZ, maxX, maxY, maxZ, config.color.getEffectiveColourRGB())
    }


    private fun fillBox(
        vc: VertexConsumer,
        minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double,
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

    private val identityPose = PoseStack()

    private val geometry = SubmitNodeCollector.CustomGeometryRenderer { _, buffer -> drawLevel(buffer) }

    fun submitHitboxes(camera: CameraRenderState, collector: SubmitNodeCollector) {
        if (!beginFrame(camera.cullFrustum)) return
        collector.submitCustomGeometry(identityPose, quadsType(), geometry)
    }
}
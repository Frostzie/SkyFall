package io.github.frostzie.skyfall.util.render

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

// Credit to PolyHitbox couldn't have figured this out without them
// https://github.com/Polyfrost/PolyHitbox/blob/main/src/main/kotlin/org/polyfrost/polyhitbox/render/HitboxRenderer.kt

//TODO: add culling
object HitboxUtils {

    fun drawEntityBox(vc: VertexConsumer, entity: Entity, camPos: Vec3, partial: Double, argb: Int) {
        // Hides death animation highlighting
        if (!entity.isAlive) return

        val px = Mth.lerp(partial, entity.xo, entity.x)
        val py = Mth.lerp(partial, entity.yo, entity.y)
        val pz = Mth.lerp(partial, entity.zo, entity.z)
        val bb = entity.boundingBox
        val dx = px - entity.x - camPos.x
        val dy = py - entity.y - camPos.y
        val dz = pz - entity.z - camPos.z
        fillBox(
            vc,
            bb.minX + dx,
            bb.minY + dy,
            bb.minZ + dz,

            bb.maxX + dx,
            bb.maxY + dy,
            bb.maxZ + dz,
            argb
        )
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
}
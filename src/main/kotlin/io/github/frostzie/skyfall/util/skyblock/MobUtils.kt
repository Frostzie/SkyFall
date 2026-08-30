package io.github.frostzie.skyfall.util.skyblock

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand

object MobUtils {
    private val mc = Minecraft.getInstance()

    data class MobMatch(
        val nameTag: ArmorStand,
        val entity: Entity
    )

    fun findMatches(
        level: ClientLevel,
        nameTags: List<ArmorStand>
    ): List<MobMatch> {
        val match = mutableListOf<MobMatch>()

        for (tag in nameTags) {
            val box = tag.boundingBox.move(0.0, -1.0, 0.0)
            val closest = level.getEntities(tag, box) { it !== tag && isValidCandidate(it) }
                .minByOrNull { it.distanceToSqr(tag) }
                ?: continue

            match += MobMatch(tag, closest)
        }
        return match
    }

    private fun isValidCandidate(entity: Entity): Boolean {
        if (entity is ArmorStand) return false
        if (entity is WitherBoss) return false
        if (entity.isInvisible) return false
        if (entity == mc.player) return false
        if (entity.uuid.version() == 2) return false // No NPC
        return true
    }
}
package io.github.frostzie.skyfall.util.skyblock

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.Items

object MobUtils {
    private val mc = Minecraft.getInstance()

    data class MobMatch(
        val nameTag: ArmorStand,
        val entity: Entity,
        val extras: List<Entity> = emptyList() // For extra entities outside the actual one. Example being pests having their head item on a different entity then their hitbox
    )

    fun resolve(tag: ArmorStand, level: ClientLevel): MobMatch? {
        // This is the most accurate I could get with the bounding box settings, tested mostly with Ants, but some mobs might still be missed/wrong needs more playtesting lol
        val box = tag.boundingBox.move(0.0, -1.0, 0.0).inflate(0.0, 0.75, 0.0)

        val candidates = level.getEntities(tag, box) {
            it !== tag && (isValidCandidate(it) || isSkullStand(it))
        }

        val body = candidates.minByOrNull { it.distanceToSqr(tag) } ?: return null
        val extra = if (body is ArmorStand) emptyList() else candidates.filter { isSkullStand(it) }

        return MobMatch(tag, body, extra)
    }

    fun isSkullStand(entity: Entity) = entity is ArmorStand &&
            entity.getItemBySlot(EquipmentSlot.HEAD).item == Items.PLAYER_HEAD

    private fun isValidCandidate(entity: Entity): Boolean {
        if (entity is ArmorStand) return false
        if (entity == mc.player) return false
        return true
    }
}
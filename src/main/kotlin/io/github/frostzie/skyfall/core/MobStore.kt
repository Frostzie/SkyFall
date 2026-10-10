package io.github.frostzie.skyfall.core

import io.github.frostzie.skyfall.util.skyblock.MobUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand

object MobStore {
    private val mc = Minecraft.getInstance()

    val boxes = ArrayList<BoxTask>()
    private val hiddenIds = HashSet<Int>()

    fun isHidden(id: Int): Boolean = hiddenIds.contains(id)

    fun refresh(req: MobScanRequest) {
        boxes.clear()
        hiddenIds.clear()

        if (req.hypixelName.isEmpty() && req.vanillaName.isEmpty()) return
        val level = mc.level ?: return
        val player = mc.player ?: return

        for (e in level.entitiesForRendering()) {
            val isTag = e is ArmorStand
            val name = e.name.string
            if (name.isBlank()) continue

            if (isTag) {
                if (req.hypixelName.isEmpty()) continue

                var want = false
                for (s in req.hypixelName) {
                    if (name.contains(s, true)) {
                        want = true
                        break
                    }
                }

                if (!want) continue
                val m = MobUtils.resolve(e, level) ?: continue
                boxes.add(BoxTask(m.entity, req.color, req.style))

                if (req.hideModel) {
                    hiddenIds.add(m.entity.id)
                    for (x in m.extras) {
                        hiddenIds.add(x.id)
                    }
                }

            } else {
                if (req.vanillaName.isEmpty() || e === player || e.isInvisible) continue

                var want = false
                for (s in req.vanillaName) {
                    if (name.contains(s, true)) {
                        want = true
                        break
                    }
                }

                if (!want) continue
                boxes.add(BoxTask(e, req.color, req.style))

                if (req.hideModel) {
                    hiddenIds.add(e.id)
                }
            }

        }
    }

}

data class MobScanRequest(
    val hypixelName: Set<String>,
    val vanillaName: Set<String>,
    val color: Int,
    val style: BoxStyle,
    val hideModel: Boolean
)

enum class BoxStyle {
    FILLED,
    OUTLINE,
    FILLED_OUTLINE,
    TWO_DIMENSIONAL
}

data class BoxTask(
    val entity: Entity,
    val color: Int,
    val style: BoxStyle
)

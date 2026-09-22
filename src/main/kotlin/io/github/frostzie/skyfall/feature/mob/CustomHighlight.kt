package io.github.frostzie.skyfall.feature.mob

import com.github.stivais.commodore.Commodore
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.util.CommandUtils
import io.github.frostzie.skyfall.util.render.HitboxUtils
import io.github.frostzie.skyfall.util.skyblock.Location
import io.github.frostzie.skyfall.util.skyblock.MobUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand

object CustomHighlight {
    private val config get() = SkyFall.features.hitbox
    private val mc = Minecraft.getInstance()

    private var selectedMobNames: MutableSet<String> = mutableSetOf()
    private var selectedVanillaNames: MutableSet<String> = mutableSetOf()

    private var armorStandMatches: List<MobUtils.MobMatch> = emptyList() // For hypixel old mobs
    private var nametagMatches: List<Entity> = emptyList() // For vanilla mobs and I think some new ones

    fun registerTick() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            if (!Location.isSkyblock() || !config.enabled) {
                armorStandMatches = emptyList(); nametagMatches = emptyList()
                return@EndTick
            }
            if (selectedMobNames.isEmpty() && selectedVanillaNames.isEmpty()) {
                armorStandMatches = emptyList(); nametagMatches = emptyList()
                return@EndTick
            }

            val level = mc.level ?: return@EndTick
            val player = mc.player ?: return@EndTick

            // Hypixel
            if (selectedMobNames.isNotEmpty()) {
                val nameTags = level.entitiesForRendering()
                    .filterIsInstance<ArmorStand>()
                    .filter { tag ->
                        val name = tag.name.string
                        name.isNotBlank()
                                && selectedMobNames.any { name.contains(it, ignoreCase = true) }
                                && (!config.lineOfSight || player.hasLineOfSight(tag)) //TODO: prob should make my own lineOfSight since this isn't too great

                    }
                armorStandMatches = MobUtils.findMatches(level, nameTags)
            } else {
                armorStandMatches = emptyList()
            }

            // Vanilla
            nametagMatches = if (selectedVanillaNames.isNotEmpty()) {
                level.entitiesForRendering()
                    .filter { e ->
                        e !is ArmorStand
                            && e !== player
                            && !e.isInvisible
                            && e.name.string.let { name ->
                                name.isNotBlank() && selectedVanillaNames.any { name.contains(it, ignoreCase = true) }
                                && (!config.lineOfSight || player.hasLineOfSight(e))
                        }
                    }
            } else {
                emptyList()
            }
        })
    }

    private val identityPose = PoseStack()
    private fun quadsType() = RenderTypes.debugQuads()
    private val geometry = SubmitNodeCollector.CustomGeometryRenderer { _, buffer -> drawOverlay(buffer) }

    fun submitHitboxes(collector: SubmitNodeCollector) {
        if (!config.enabled) return
        collector.submitCustomGeometry(identityPose, quadsType(), geometry)
    }

    private fun drawOverlay(vc: VertexConsumer) {
        val cam = mc.entityRenderDispatcher.camera ?: return
        val camPos = cam.position()
        val partial = mc.deltaTracker.getGameTimeDeltaPartialTick(false).toDouble()
        val color = config.color.getEffectiveColourRGB()

        for (n in armorStandMatches) HitboxUtils.drawEntityBox(vc, n.entity, camPos, partial, color)
        for (e in nametagMatches) HitboxUtils.drawEntityBox(vc, e, camPos, partial, color)
    }

    val highlightCommands = Commodore("skyfallHighlight") {

        literal("hypixel").runs { name: String ->
            if (selectedMobNames.add(name)) {
                config.hypixelMobs.add(name)
                CommandUtils.clientMessage("Added hypixel mob: $name")
            } else {
                selectedMobNames.remove(name)
                config.hypixelMobs.remove(name)
                CommandUtils.clientMessage("Removed hypixel mob: $name")
            }
        }

        literal("vanilla").runs { name: String ->
            if (selectedVanillaNames.add(name)) {
                config.vanillaMobs.add(name)
                CommandUtils.clientMessage("Added vanilla mob: $name")
            } else {
                selectedVanillaNames.remove(name)
                config.vanillaMobs.remove(name)
                CommandUtils.clientMessage("Removed vanilla mob: $name")
            }
        }

        literal("list").runs {

            CommandUtils.clientMessage(
                "\nHypixel: " + if (selectedMobNames.isEmpty()) "None" else selectedMobNames.joinToString(", ") +
                "\nVanilla: " + if (selectedVanillaNames.isEmpty()) "None" else selectedVanillaNames.joinToString(", ")
            )
        }
    }
}

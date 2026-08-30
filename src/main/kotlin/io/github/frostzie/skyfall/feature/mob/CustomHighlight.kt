package io.github.frostzie.skyfall.feature.mob

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.util.CommandUtils
import io.github.frostzie.skyfall.util.LoggerProvider
import io.github.frostzie.skyfall.util.render.HitboxUtils
import io.github.frostzie.skyfall.util.skyblock.Location
import io.github.frostzie.skyfall.util.skyblock.MobUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import java.io.File

object CustomHighlight {
    private val logger = LoggerProvider.getLogger("CustomHighlight")
    private val config get() = SkyFall.features.hitbox
    private val mc = Minecraft.getInstance()

    private var selectedMobNames: MutableSet<String> = mutableSetOf()
    private var selectedVanillaNames: MutableSet<String> = mutableSetOf()

    private var armorStandMatches: List<MobUtils.MobMatch> = emptyList() // For hypixel old mobs
    private var nametagMatches: List<Entity> = emptyList() // For vanilla mobs and I think some new ones

    private val configFile = FabricLoader.getInstance().configDir
        .resolve("skyfall").resolve("CustomHighlight.json").toFile()

    init {
        highlightMobCommand()

        if (!configFile.exists()) save(configFile, selectedMobNames, selectedVanillaNames)
        val saved = load(configFile)
        selectedMobNames = saved.hypixelMobs
        selectedVanillaNames = saved.vanillaMobs
    }

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
                    .filter { e -> // Filter go brrr
                        // Allowing NPCs since I think some new mobs count as that now
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

    //TODO: Figure out DSL and build it so this... doesn't happen again
    private fun highlightMobCommand() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommands.literal("skyfallHighlight")
                .then(ClientCommands.literal("hypixel")
                    .then(ClientCommands.argument("mob_hypixel", StringArgumentType.string())
                        .executes { ctx ->
                            val name = ctx.getArgument("mob_hypixel", String::class.java)
                            if (selectedMobNames.add(name)) {
                                CommandUtils.clientMessage("$name added!")
                                save(configFile, selectedMobNames, selectedVanillaNames)
                            } else {
                                selectedMobNames.remove(name)
                                CommandUtils.clientMessage("$name removed!")
                                save(configFile, selectedMobNames, selectedVanillaNames)
                            }
                            1
                        }
                    )
                )
                .then(ClientCommands.literal("vanilla")
                    .then(ClientCommands.argument("mob_vanilla", StringArgumentType.string())
                        .executes { ctx ->
                            val nameVanilla = ctx.getArgument("mob_vanilla", String::class.java)
                            if (selectedVanillaNames.add(nameVanilla)) {
                                CommandUtils.clientMessage("$nameVanilla added!")
                                save(configFile, selectedMobNames, selectedVanillaNames)
                            } else {
                                selectedVanillaNames.remove(nameVanilla)
                                CommandUtils.clientMessage("$nameVanilla removed!")
                                save(configFile, selectedMobNames, selectedVanillaNames)
                            }
                            1
                        }
                    )
                )
                .then(ClientCommands.literal("list")
                    .executes {
                        CommandUtils.clientMessage("Hypixel names: $selectedMobNames\nVanilla$selectedVanillaNames")
                        1
                    }
                )
            )
        }
    }

    @Serializable
    data class SavedMobs(
        val hypixelMobs: MutableSet<String> = mutableSetOf(),
        val vanillaMobs: MutableSet<String> = mutableSetOf()
    )


    fun save(file: File, hypixelMobs: Set<String>, vanillaMobs: Set<String>) {
        try {
            file.writeText(Json.encodeToString(SavedMobs(hypixelMobs.toMutableSet(), vanillaMobs.toMutableSet())))
        } catch (e: Exception) {
            logger.error("Failed to save CustomHighlight.json", e)
        }

    }

    fun load(file: File): SavedMobs = try {
        Json.decodeFromString<SavedMobs>(file.readText())
    } catch (e: Exception) {
        logger.error("Failed to load CustomHighlight.json", e)
        SavedMobs()
    }
}

package io.github.frostzie.skyfall.feature.mob

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.frostzie.skyfall.SkyFall
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
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand
import java.io.File

object CustomHighlight {
    private val logger = LoggerProvider.getLogger("CustomHighlight")
    private val config get() = SkyFall.features.hitbox
    private val mc = Minecraft.getInstance()

    private var selectedMobNames: MutableSet<String> = mutableSetOf()
    private var currentMatches: List<MobUtils.MobMatch> = emptyList()

    private val configFile = FabricLoader.getInstance().configDir
        .resolve("skyfall").resolve("CustomHighlight.json").toFile()

    init {
        highlightMobCommand()

        if (!configFile.exists()) save(configFile, selectedMobNames)
        val saved = load(configFile)
        selectedMobNames = saved.hypixelMobs
    }

    fun registerTick() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            if (!Location.isSkyblock() || !config.enabled || selectedMobNames.isEmpty()) {
                currentMatches = emptyList()
                return@EndTick
            }

            val level = mc.level ?: return@EndTick
            val player = mc.player ?: return@EndTick

            val nameTags = level.entitiesForRendering()
                .filterIsInstance<ArmorStand>()
                .filter { tag ->
                    val name = tag.name.string
                    name.isNotBlank()
                        && selectedMobNames.any { name.contains(it, ignoreCase = true) }
                        && (!config.lineOfSight || player.hasLineOfSight(tag)) //TODO: prob should make my own lineOfSight since this isn't too great

                }

            currentMatches = MobUtils.findMatches(level, nameTags)
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

        for (match in currentMatches) {
            HitboxUtils.drawEntityBox(vc, match.entity, camPos,partial, color)
        }
    }


    private fun highlightMobCommand() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommands.literal("skyfallHighlight")
                .then(ClientCommands.argument("mob_hypixel", StringArgumentType.string())
                    .executes { context ->
                        val name = context.getArgument("mob_hypixel", String::class.java)
                        if (selectedMobNames.contains(name)) {
                            logger.info("$name removed!")
                            selectedMobNames.remove(name)
                            save(configFile, selectedMobNames)
                        } else {
                            logger.info("Added $name")
                            selectedMobNames.add(name)
                            save(configFile, selectedMobNames)
                        }
                        1
                    }
                )
                .then(ClientCommands.literal("list")
                    .executes { context ->
                        context.source.sendFeedback(Component.literal("Selected Mobs: $selectedMobNames"))
                        1
                    }
                )
            )
        }
    }

    @Serializable
    data class SavedMobs (
        val hypixelMobs: MutableSet<String>,
    )


    fun save(file: File, hypixelMobs: MutableSet<String>) {
        try {
            val data = SavedMobs(hypixelMobs)
            val jsonString = Json.encodeToString(data)
            file.writeText(jsonString)
        } catch (e: Exception) {
            logger.error("Failed to save CustomHighlight.json", e)
        }

    }

    fun load(file: File): SavedMobs {
        try {
            val jsonString = file.readText()
            return Json.decodeFromString<SavedMobs>(jsonString)
        } catch (e: Exception) {
            logger.error("Failed to load CustomHighlight.json", e)
            return SavedMobs(mutableSetOf())
        }
    }
}

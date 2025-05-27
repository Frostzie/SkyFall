package io.github.frostzie.skyfall.features.misc

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*

object HoppityCallNotifier { //TODO: move to event features when we get more of them
    private val config get() = SkyFall.feature.miscFeatures

    private var alertStartTime: SimpleTimeMark? = null
    private var isPlayingSound = false
    private val alertDuration = 7.seconds

    private val hoppityQuestionRegex = Regex("§e✆ §r§[ab]Hoppity§r§e ✆.*")
    private val hoppityAnswerRegex = Regex("§e\\[NPC\\] §aHoppity§f: §b✆")
    private val testRegex1 = Regex("§e✆")
    private val testRegex2 = Regex("Test")


    private var bellJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    fun init() {
        // Register HUD overlay
        HudLayerRegistrationCallback.EVENT.register { drawer: LayeredDrawerWrapper ->
            drawer.attachLayerAfter(
                IdentifiedLayer.MISC_OVERLAYS,
                Identifier.of("skyfall", "hoppity_alert")
            ) { context: DrawContext, _ ->
                renderRedOverlay(context)
            }
        }

        // Register chat message listener
        ClientReceiveMessageEvents.GAME.register { message: Text, overlay: Boolean ->
            handleChatMessage(message.string)
        }
    }

    // Add this property to HoppityCallNotifier
    private var currentOverlayColor: Int? = null

    private fun handleChatMessage(message: String) {
        if (!config.hoppityCallNotifier) return

        val cleanMessage = message.replace(" ", "").trim()

        // Green overlay for testRegex1
        if (testRegex1.containsMatchIn(cleanMessage)) {
            currentOverlayColor = 0x8000FF00.toInt() // 50% transparent green
            startAlert()
        }
        // Yellow overlay for testRegex2
        else if (testRegex2.containsMatchIn(cleanMessage)) {
            currentOverlayColor = 0x80FFFF00.toInt() // 50% transparent yellow
            startAlert()
        }
        // Red overlay for Hoppity question
        else if (hoppityQuestionRegex.containsMatchIn(cleanMessage)) {
            currentOverlayColor = 0x80FF0000.toInt() // 50% transparent red
            startAlert()
        }
        // Stop alert for Hoppity answer
        else if (hoppityAnswerRegex.containsMatchIn(cleanMessage)) {
            stopAlert()
        }
    }

    private fun startAlert() {
        alertStartTime = SimpleTimeMark.now()
        isPlayingSound = true
        bellJob?.cancel()
        bellJob = coroutineScope.launch {
            while (isPlayingSound && isAlertActive()) {
                playBellSoundOnce()
                delay(1000)
            }
        }
    }

    private fun stopAlert() {
        alertStartTime = null
        isPlayingSound = false
        currentOverlayColor = null
        bellJob?.cancel()
        bellJob = null
    }


    private fun renderRedOverlay(context: DrawContext) {
        if (!config.hoppityCallNotifier || !isAlertActive() || currentOverlayColor == null) return

        context.fill(
            0, 0,
            context.scaledWindowWidth,
            context.scaledWindowHeight,
            currentOverlayColor!!
        )
    }

    private fun isAlertActive(): Boolean {
        val startTime = alertStartTime ?: return false
        return startTime.passedSince() < alertDuration
    }

    private var shouldPlayBellSound = false

    private fun playBellSoundOnce() {
        shouldPlayBellSound = true
    }

    fun onClientTick() {
        if (shouldPlayBellSound) {
            val client = MinecraftClient.getInstance()
            val player = client.player
            if (player != null) {
                player.playSound(SoundEvents.BLOCK_BELL_USE, 1.0f, 1.0f)
            }
            shouldPlayBellSound = false
        }
    }
}
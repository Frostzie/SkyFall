package io.github.frostzie.skyfall.util.skyblock

import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot

object Location {

    fun isSkyblock(): Boolean {
        val title = getScoreboardTitle().lowercase()
        return title.contains("skyblock") // || DevUtils.isDevEnv()
    }

    private fun getScoreboardTitle(): String {
        val scoreboard = Minecraft.getInstance().level?.scoreboard ?: return "Unknown Scoreboard"
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return "Unknown Objective"

        return objective.displayName.string
    }
}

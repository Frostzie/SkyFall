package io.github.frostzie.nodex.ui

import io.github.frostzie.nodex.domain.uicontract.PanelPosition
import io.github.frostzie.nodex.domain.uicontract.ToolPolicy
import io.github.frostzie.nodex.domain.uicontract.ToolWindow

/**
 * Registry for workbench tool windows and their default behaviors.
 */
object ToolRegistry {

    private val tools = mapOf(
        ToolWindow.FILES to ToolPolicy(
            title = "Files",
            defaultAnchor = PanelPosition.LEFT,
            defaultSizeRatio = 0.25,
            defaultVisible = true
        )
    )

    fun getProfile(kind: ToolWindow): ToolPolicy =
        tools[kind] ?: throw IllegalArgumentException("No profile found for tool window: $kind")

    fun getAllKinds(): List<ToolWindow> = tools.keys.toList()
}

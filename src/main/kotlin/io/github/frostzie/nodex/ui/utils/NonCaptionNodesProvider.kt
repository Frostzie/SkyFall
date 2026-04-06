package io.github.frostzie.nodex.ui.utils

import javafx.scene.Node

/**
 * Provides a list of nodes that should be excluded from FxStage caption dragging.
 */
interface NonCaptionNodesProvider {
    fun getNonCaptionNodes(): List<Node>
}

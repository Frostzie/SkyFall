package io.github.frostzie.nodex.ui.view.projectManager

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox

/**
 * The top bar for the Project Manager screen.
 */
class ProjectManagerTopBarView : HBox(10.0) {

    init {
        alignment = Pos.CENTER_LEFT
        padding = Insets(5.0, 10.0, 5.0, 10.0)
        prefHeight = 40.0

        val icon = ImageView().apply {
            try {
                image = Image("assets/nodex/icon.png")
            } catch (_: Exception) {}
            fitWidth = 20.0
            fitHeight = 20.0
            isPreserveRatio = true
        }

        val title = Label("Project Manager")

        children.addAll(icon, title)
    }
}

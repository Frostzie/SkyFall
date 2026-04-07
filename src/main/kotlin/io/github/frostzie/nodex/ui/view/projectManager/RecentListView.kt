package io.github.frostzie.nodex.ui.view.projectManager

import atlantafx.base.theme.Styles
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * The sidebar containing the list of recent projects.
 */
class RecentListView : VBox() {
    init {
        prefWidth = 300.0
        maxWidth = 300.0
        minWidth = 200.0

        val titleLabel = Label("Recent Projects").apply {
            styleClass.addAll(Styles.TITLE_4, Styles.TEXT_MUTED)
            padding = Insets(10.0)
            maxWidth = Double.MAX_VALUE
        }

        val listView = ListView<String>().apply {
            styleClass.add(Styles.BORDERED)
            setVgrow(this, Priority.ALWAYS)
        }

        children.addAll(titleLabel, listView)
    }
}

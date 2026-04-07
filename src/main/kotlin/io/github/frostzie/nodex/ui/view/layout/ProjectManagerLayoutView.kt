package io.github.frostzie.nodex.ui.view.layout

import io.github.frostzie.nodex.ui.view.projectManager.MainAreaView
import io.github.frostzie.nodex.ui.view.projectManager.ProjectManagerTopBarView
import io.github.frostzie.nodex.ui.view.projectManager.RecentListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane

/**
 * Layout for the Project Manager screen.
 */
class ProjectManagerLayoutView(
    projectManagerTopBarView: ProjectManagerTopBarView,
    recentListView: RecentListView,
    mainAreaView: MainAreaView
) : StackPane() {

    init {
        val shell = BorderPane()

        shell.top = projectManagerTopBarView
        shell.left = recentListView
        shell.center = mainAreaView

        children.addAll(shell)
    }
}
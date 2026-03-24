package io.github.frostzie.nodex.ui.builder

import io.github.frostzie.nodex.ui.view.layout.ProjectManagerLayoutView
import io.github.frostzie.nodex.ui.view.projectManager.MainAreaView
import io.github.frostzie.nodex.ui.view.projectManager.ProjectManagerTopBarView
import io.github.frostzie.nodex.ui.view.projectManager.RecentListView

/**
 * Builds the Project Manager screen layout.
 */
class ProjectManagerScreenBuilder {

    fun build(): ProjectManagerLayoutView {
        val topBarView = ProjectManagerTopBarView()
        val recentListView = RecentListView()
        val mainAreaView = MainAreaView()

        return ProjectManagerLayoutView(
            topBarView,
            recentListView,
            mainAreaView
        )
    }
}

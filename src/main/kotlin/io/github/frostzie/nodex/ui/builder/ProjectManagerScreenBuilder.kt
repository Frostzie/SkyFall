package io.github.frostzie.nodex.ui.builder

import io.github.frostzie.nodex.api.navigation.Navigation
import io.github.frostzie.nodex.api.workspace.ProjectRuntime
import io.github.frostzie.nodex.ui.view.layout.ProjectManagerLayoutView
import io.github.frostzie.nodex.ui.view.projectManager.MainAreaView
import io.github.frostzie.nodex.ui.view.projectManager.ProjectManagerTopBarView
import io.github.frostzie.nodex.ui.view.projectManager.RecentListView
import io.github.frostzie.nodex.ui.viewmodel.projectManager.ProjectManagerViewModel

/**
 * Builds the Project Manager screen layout.
 */
class ProjectManagerScreenBuilder(
    private val navigationService: Navigation,
    private val projectRuntimeService: ProjectRuntime
) {

    fun build(): ProjectManagerLayoutView {
        val topBarView = ProjectManagerTopBarView()
        val recentListView = RecentListView()

        val projectManagerViewModel = ProjectManagerViewModel(navigationService, projectRuntimeService)
        val mainAreaView = MainAreaView(projectManagerViewModel.getMainAreaViewModel())

        return ProjectManagerLayoutView(
            topBarView,
            recentListView,
            mainAreaView
        )
    }
}

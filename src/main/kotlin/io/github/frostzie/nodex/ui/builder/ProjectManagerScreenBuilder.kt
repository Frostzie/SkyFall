package io.github.frostzie.nodex.ui.builder

import io.github.frostzie.nodex.services.ui.NavigationService
import io.github.frostzie.nodex.services.workspace.ProjectRuntimeService
import io.github.frostzie.nodex.ui.view.layout.ProjectManagerLayoutView
import io.github.frostzie.nodex.ui.view.projectManager.MainAreaView
import io.github.frostzie.nodex.ui.view.projectManager.ProjectManagerTopBarView
import io.github.frostzie.nodex.ui.view.projectManager.RecentListView
import io.github.frostzie.nodex.ui.viewmodel.projectManager.ProjectManagerViewModel

/**
 * Builds the Project Manager screen layout.
 */
class ProjectManagerScreenBuilder(
    private val navigationService: NavigationService,
    private val projectRuntimeService: ProjectRuntimeService
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

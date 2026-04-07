package io.github.frostzie.nodex.ui.viewmodel.projectManager

import io.github.frostzie.nodex.domain.entity.Project
import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.services.ui.NavigationService
import io.github.frostzie.nodex.services.workspace.ProjectRuntimeService
import java.nio.file.Path

class ProjectManagerViewModel(
    private val navigationService: NavigationService,
    private val projectRuntimeService: ProjectRuntimeService
) {
    private val mainAreaViewModel = MainAreaViewModel(this)

    fun getMainAreaViewModel(): MainAreaViewModel = mainAreaViewModel

    fun onImportProject(path: Path) {
        val project = Project(path)
        projectRuntimeService.setProject(project)
        navigationService.navigateTo(AppScreen.IDE)
    }
}

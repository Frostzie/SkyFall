package io.github.frostzie.nodex.ui.viewmodel.projectManager

import io.github.frostzie.nodex.domain.entity.Project
import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.api.navigation.Navigation
import io.github.frostzie.nodex.api.workspace.ProjectRuntime
import java.nio.file.Path

class ProjectManagerViewModel(
    private val navigationService: Navigation,
    private val projectRuntimeService: ProjectRuntime
) {
    private val mainAreaViewModel = MainAreaViewModel(this)

    fun getMainAreaViewModel(): MainAreaViewModel = mainAreaViewModel

    fun onImportProject(path: Path) {
        val project = Project(path)
        projectRuntimeService.setProject(project)
        navigationService.navigateTo(AppScreen.IDE)
    }
}

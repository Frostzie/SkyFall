package io.github.frostzie.nodex.ui.builder

import io.github.frostzie.nodex.domain.uicontract.ToolWindow
import io.github.frostzie.nodex.services.core.LayoutService
import io.github.frostzie.nodex.services.core.PerformanceService
import io.github.frostzie.nodex.services.files.FileTreePersistenceService
import io.github.frostzie.nodex.services.files.FileTreeService
import io.github.frostzie.nodex.services.ui.NavigationService
import io.github.frostzie.nodex.services.workspace.ProjectRuntimeService
import io.github.frostzie.nodex.ui.view.ide.bottombar.BottomBarView
import io.github.frostzie.nodex.ui.view.ide.leftbar.LeftBarView
import io.github.frostzie.nodex.ui.view.ide.overlay.ToolWindowDropOverlayView
import io.github.frostzie.nodex.ui.view.ide.topbar.TopBarView
import io.github.frostzie.nodex.ui.view.ide.workbench.WorkbenchView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.CodeEditorView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.EmptyCodeEditorView
import io.github.frostzie.nodex.ui.view.ide.workbench.tree.FileTreeView
import io.github.frostzie.nodex.ui.view.layout.IdeLayoutView
import io.github.frostzie.nodex.ui.viewmodel.ide.bottombar.BottomBarViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.leftbar.LeftBarViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.topbar.TopBarViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.DockLayerViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.EditorAreaViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.pane.CodeEditorViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.tree.FileTreeViewModel

/**
 * Builds the IDE screen layout.
 */
class IdeScreenBuilder(
    private val layoutService: LayoutService,
    private val navigationService: NavigationService,
    private val performanceService: PerformanceService,
    private val fileTreeService: FileTreeService,
    private val projectRuntimeService: ProjectRuntimeService,
    private val fileTreePersistenceService: FileTreePersistenceService
) {

    fun build(): IdeLayoutView {
        val dockLayerViewModel = DockLayerViewModel(layoutService)
        val fileTreeViewModel = FileTreeViewModel(fileTreeService, projectRuntimeService, fileTreePersistenceService)
        val editorAreaViewModel = EditorAreaViewModel()
        val codeEditorViewModel = CodeEditorViewModel()
        val leftBarViewModel = LeftBarViewModel(layoutService)
        val topBarViewModel = TopBarViewModel(navigationService)
        val bottomBarViewModel = BottomBarViewModel(performanceService)

        val codeEditorView = CodeEditorView(codeEditorViewModel)
        val emptyCodeEditorView = EmptyCodeEditorView()

        //TODO: Move it out of the builder
        val toolViews = mapOf(
            ToolWindow.FILES to FileTreeView(fileTreeViewModel)
        )

        val workbenchView = WorkbenchView(
            dockLayerViewModel,
            editorAreaViewModel,
            codeEditorView,
            emptyCodeEditorView,
            toolViews
        )

        val overlayView = ToolWindowDropOverlayView(dockLayerViewModel.currentDropTarget)

        val leftBarView = LeftBarView(leftBarViewModel)
        val topBarView = TopBarView(topBarViewModel)
        val bottomBarView = BottomBarView(bottomBarViewModel)

        return IdeLayoutView(
            workbenchView,
            overlayView,
            topBarView,
            leftBarView,
            bottomBarView
        )
    }
}

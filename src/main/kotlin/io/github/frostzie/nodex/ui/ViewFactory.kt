package io.github.frostzie.nodex.ui

import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.domain.uicontract.ToolWindow
import io.github.frostzie.nodex.services.core.LayoutService
import io.github.frostzie.nodex.services.core.PerformanceService
import io.github.frostzie.nodex.services.ui.NavigationService
import io.github.frostzie.nodex.services.settings.SettingsService
import io.github.frostzie.nodex.ui.view.layout.IdeLayoutView
import io.github.frostzie.nodex.ui.view.layout.ProjectManagerLayoutView
import io.github.frostzie.nodex.ui.view.layout.SettingsLayoutView
import io.github.frostzie.nodex.ui.view.settings.SettingsActionsBarView
import io.github.frostzie.nodex.ui.view.settings.SettingsContentView
import io.github.frostzie.nodex.ui.viewmodel.settings.SettingsActionsBarViewModel
import io.github.frostzie.nodex.ui.viewmodel.settings.SettingsContentViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.bottombar.BottomBarViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.leftbar.LeftBarViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.topbar.TopBarViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.EditorAreaViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.pane.CodeEditorViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.DockLayerViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.tree.FileTreeViewModel
import io.github.frostzie.nodex.ui.view.ide.bottombar.BottomBarView
import io.github.frostzie.nodex.ui.view.ide.leftbar.LeftBarView
import io.github.frostzie.nodex.ui.view.ide.overlay.ToolWindowDropOverlayView
import io.github.frostzie.nodex.ui.view.ide.topbar.TopBarView
import io.github.frostzie.nodex.ui.view.ide.workbench.WorkbenchView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.CodeEditorView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.EmptyCodeEditorView
import io.github.frostzie.nodex.ui.view.ide.workbench.tree.FileTreeView
import io.github.frostzie.nodex.ui.view.intro.IntroView
import io.github.frostzie.nodex.ui.view.layout.IntroLayoutView
import io.github.frostzie.nodex.ui.viewmodel.intro.IntroViewModel
import io.github.frostzie.nodex.ui.view.projectManager.MainAreaView
import io.github.frostzie.nodex.ui.view.projectManager.ProjectManagerTopBarView
import io.github.frostzie.nodex.ui.view.projectManager.RecentListView
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane

/**
 * Factory for creating and assembling UI components.
 */
class ViewFactory(
    private val layoutService: LayoutService,
    private val navigationService: NavigationService,
    private val performanceService: PerformanceService,
    private val settingsService: SettingsService
) {

    fun createIdeLayout(): IdeLayoutView {
        // ViewModels
        val dockLayerViewModel = DockLayerViewModel(layoutService)
        val fileTreeViewModel = FileTreeViewModel()
        val editorAreaViewModel = EditorAreaViewModel()
        val codeEditorViewModel = CodeEditorViewModel()
        val leftBarViewModel = LeftBarViewModel(layoutService)
        val topBarViewModel = TopBarViewModel(navigationService)
        val bottomBarViewModel = BottomBarViewModel(performanceService)

        // Views
        val codeEditorView = CodeEditorView(codeEditorViewModel)
        val emptyCodeEditorView = EmptyCodeEditorView()

        //TODO: Move it out of ViewFactory
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

    fun createProjectManagerLayout(): ProjectManagerLayoutView {
        val topBarView = ProjectManagerTopBarView()
        val recentListView = RecentListView()
        val mainAreaView = MainAreaView()

        return ProjectManagerLayoutView(
            topBarView,
            recentListView,
            mainAreaView
        )
    }

    fun createIntroLayout(): IntroLayoutView {
        val introViewModel = IntroViewModel(navigationService)
        val introView = IntroView(introViewModel)

        return IntroLayoutView(introView)
    }

    fun createSettingsLayout(): SettingsLayoutView {
        settingsService.discard()

        val contentViewModel = SettingsContentViewModel(settingsService)
        val actionsViewModel = SettingsActionsBarViewModel(settingsService, contentViewModel, navigationService)

        val contentView = SettingsContentView(contentViewModel)
        val actionsBarView = SettingsActionsBarView(actionsViewModel)

        return SettingsLayoutView(contentView, actionsBarView)
    }

    fun createOverlayContent(screen: OverlayScreen): Region {
        return when (screen) {
            OverlayScreen.SETTINGS -> createSettingsLayout()
        }
    }

    fun createScreenHost(): ScreenHost {
        val ideLayout = createIdeLayout()
        val projectManagerLayout = createProjectManagerLayout()
        val introLayoutView = createIntroLayout()
        return ScreenHost(ideLayout, introLayoutView, projectManagerLayout, navigationService)
    }

    fun createRootView(screenHost: ScreenHost): Region {
        return StackPane(screenHost)
    }
}
package io.github.frostzie.nodex.ui

import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.services.core.ConcurrencyService
import io.github.frostzie.nodex.services.core.LayoutService
import io.github.frostzie.nodex.services.core.ModInfoService
import io.github.frostzie.nodex.services.core.PerformanceService
import io.github.frostzie.nodex.services.ui.NavigationService
import io.github.frostzie.nodex.services.workspace.EditorService
import io.github.frostzie.nodex.services.workspace.WorkspaceService
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
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.pane.EmptyCodeEditorViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.WorkbenchViewModel
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.tree.FileTreeViewModel
import io.github.frostzie.nodex.services.files.FileWatcherService
import io.github.frostzie.nodex.ui.view.ide.bottombar.BottomBarView
import io.github.frostzie.nodex.ui.view.ide.leftbar.LeftBarView
import io.github.frostzie.nodex.ui.view.ide.overlay.FileTreeDropOverlayView
import io.github.frostzie.nodex.ui.view.ide.topbar.TopBarView
import io.github.frostzie.nodex.ui.view.ide.workbench.WorkbenchView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.EditorAreaView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.CodeEditorView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.EmptyCodeEditorView
import io.github.frostzie.nodex.ui.view.ide.workbench.tree.FileTreeView
import io.github.frostzie.nodex.ui.view.intro.IntroView
import io.github.frostzie.nodex.ui.view.layout.IntroLayoutView
import io.github.frostzie.nodex.ui.viewmodel.intro.IntroViewModel
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane

/**
 * Factory for creating and assembling UI components.
 */
class ViewFactory(
    private val layoutService: LayoutService,
    private val navigationService: NavigationService,
    private val editorService: EditorService,
    private val workspaceService: WorkspaceService,
    private val fileWatcherService: FileWatcherService,
    private val concurrencyService: ConcurrencyService,
    private val modInfoService: ModInfoService,
    private val performanceService: PerformanceService,
    private val settingsService: SettingsService
) {

    fun createIdeLayout(): IdeLayoutView {
        // ViewModels
        val workbenchViewModel = WorkbenchViewModel(layoutService)
        val fileTreeViewModel = FileTreeViewModel(
            workspaceService,
            fileWatcherService,
            concurrencyService,
            editorService
        )
        val editorAreaViewModel = EditorAreaViewModel(editorService)
        val codeEditorViewModel = CodeEditorViewModel(editorService)
        val emptyCodeEditorViewModel = EmptyCodeEditorViewModel()
        val leftBarViewModel = LeftBarViewModel(layoutService)
        val topBarViewModel = TopBarViewModel(workspaceService, navigationService)
        val bottomBarViewModel = BottomBarViewModel(
            modInfoService,
            editorService,
            performanceService
        )

        // Views
        val codeEditorView = CodeEditorView(codeEditorViewModel)
        val emptyCodeEditorView = EmptyCodeEditorView(emptyCodeEditorViewModel)

        val editorAreaView = EditorAreaView(
            editorAreaViewModel,
            codeEditorView,
            emptyCodeEditorView
        )

        val fileTreeView = FileTreeView(fileTreeViewModel)

        val workbenchView = WorkbenchView(
            workbenchViewModel,
            editorAreaView,
            fileTreeView
        )

        val overlayView = FileTreeDropOverlayView(workbenchViewModel.currentDropTarget)
        
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

    //TODO: add actual views
    fun createProjectManagerLayout(): ProjectManagerLayoutView {

        return ProjectManagerLayoutView(

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
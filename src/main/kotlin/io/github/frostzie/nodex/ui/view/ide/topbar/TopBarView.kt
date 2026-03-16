package io.github.frostzie.nodex.ui.view.ide.topbar

import io.github.frostzie.nodex.ui.viewmodel.ide.topbar.TopBarViewModel
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox

/**
 * The TopBar of the IDE, containing action bar and window controls.
 */
class TopBarView(private val viewModel: TopBarViewModel) : HBox() {
    private val menuBar = createMenuBar()

    /**
     * Nodes that must stay interactive and should not be treated as caption area by FxStage.
     */
    val nonCaptionNodes: List<Node>
        get() = listOf(menuBar)

    init {
        prefHeight = 35.0
        minHeight = 35.0
        maxHeight = 35.0
        styleClass.add("top-bar")

        children.add(menuBar)
    }

    //TODO: Remove this and add an actual menu bar
    private fun createMenuBar(): MenuBar {
        val menuBar = MenuBar()
        menuBar.style = "-fx-background-color: transparent;" // Oh, got that stupid bg box I hate it temp for now but yeah just had to say it
        menuBar.menus.addAll(
            Menu("Dev", null,
                Menu(
                    "Screen Switching", null,
                    MenuItem("Intro").apply { setOnAction { viewModel.openIntro() } },
                    MenuItem("ProjectManager").apply { setOnAction { viewModel.openProjectManager() } },
                    MenuItem("IDE").apply { setOnAction { viewModel.openIde() } },
                    MenuItem("Settings").apply { setOnAction { viewModel.openSettings() } }
                )
            )
        )
        return menuBar
    }
}

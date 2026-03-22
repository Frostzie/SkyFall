package io.github.frostzie.nodex.ui.view.ide.workbench.tree

import io.github.frostzie.nodex.domain.uicontract.ToolWindow
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.tree.FileTreeItem
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.tree.FileTreeViewModel
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class FileTreeView(private val viewModel: FileTreeViewModel) : VBox() {
    private val header = HBox()
    private val treeView = TreeView<FileTreeItem>()

    init {
        setupHeader()
        setupTreeView()
        
        children.addAll(header, treeView)
    }

    private fun setupHeader() {
        header.minHeight = 30.0
        header.prefHeight = 30.0
        header.maxHeight = 30.0
        //TODO: remove this
        header.style = "-fx-background-color: #ffffff;" // Just styling the draggable area so easier to tell the diff

        // Reimplementing the drag logic for the Workbench
        header.setOnDragDetected { event ->
            val db = header.startDragAndDrop(TransferMode.MOVE)
            val content = ClipboardContent()
            content.putString(ToolWindow.FILES.name)
            db.setContent(content)
            event.consume()
        }
    }

    private fun setupTreeView() {
        treeView.rootProperty().bind(viewModel.root)
        treeView.isShowRoot = true //TODO: Need to add Project name next to root similar to IntelliJ
        
        treeView.selectionModel.selectedItemProperty().addListener { _, _, newItem ->
            viewModel.selectedPath.set(newItem?.value?.path)
        }

        viewModel.selectedPath.addListener { _, _, newPath ->
            if (newPath == null) {
                treeView.selectionModel.clearSelection()
            } else {
                val item = viewModel.nodeCache[newPath.toAbsolutePath().toString()]
                if (item != null) {
                    treeView.selectionModel.select(item)
                } else {
                    treeView.selectionModel.clearSelection()
                }
            }
        }

        treeView.setCellFactory {
            object : TreeCell<FileTreeItem>() {
                override fun updateItem(item: FileTreeItem?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        graphic = null
                    } else {
                        text = item.displayName
                    }
                }
            }
        }

        setVgrow(treeView, Priority.ALWAYS)
    }
}

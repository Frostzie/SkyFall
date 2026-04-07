package io.github.frostzie.nodex.ui.view.ide.workbench.editor

import io.github.frostzie.nodex.domain.uicontract.EditorPaneState
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.CodeEditorView
import io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane.EmptyCodeEditorView
import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.EditorAreaViewModel
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

//TODO: Re-add
class EditorAreaView(
    viewModel: EditorAreaViewModel,
    private val codeEditorView: CodeEditorView,
    private val emptyEditorView: EmptyCodeEditorView
) : VBox() {

    private val editorContentArea = StackPane()

    // Placeholder for tabs
    private val tabAreaPlaceholder = Pane().apply {
        minHeight = 30.0
        prefHeight = 30.0
        maxHeight = 30.0
        style = "-fx-background-color: #f0f0f0;" // Temp color for clarity
    }

    init {
        setVgrow(editorContentArea, Priority.ALWAYS)
        children.addAll(tabAreaPlaceholder, editorContentArea)

        viewModel.editorState.addListener { _, _, state ->
            updateView(state)
        }
        updateView(viewModel.editorState.get())
    }

    private fun updateView(state: EditorPaneState) {
        editorContentArea.children.setAll(
            when (state) {
                is EditorPaneState.Active -> codeEditorView
                is EditorPaneState.Empty -> emptyEditorView
            }
        )
    }
}
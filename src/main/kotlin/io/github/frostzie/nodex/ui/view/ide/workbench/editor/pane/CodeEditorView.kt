package io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane

import io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.pane.CodeEditorViewModel
import javafx.scene.layout.StackPane
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory

//TODO: Re-add
class CodeEditorView(private val viewModel: CodeEditorViewModel) : StackPane() {

    private val codeArea = CodeArea()

    init {
        codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
        val scrollPane = VirtualizedScrollPane(codeArea)
        children.add(scrollPane)

        setupBindings()
    }

    private fun setupBindings() {
        codeArea.editableProperty().bind(viewModel.isLocked.not())
    }
}

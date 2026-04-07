package io.github.frostzie.nodex.ui.view.ide.workbench.editor.pane

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.StackPane

//TODO: Re-add
class EmptyCodeEditorView : StackPane() {

    init {
        val label = Label("No File Selected!\nNo point in trying trust me\nyou won't open anything haha\n-Frost")

        this.children.add(label)
        setAlignment(label, Pos.CENTER)
    }
}
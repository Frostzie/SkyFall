package io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor

import io.github.frostzie.nodex.domain.uicontract.EditorPaneState
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty

//TODO: Re-add
class EditorAreaViewModel {
    val editorState: ObjectProperty<EditorPaneState> = SimpleObjectProperty(EditorPaneState.Empty)
}

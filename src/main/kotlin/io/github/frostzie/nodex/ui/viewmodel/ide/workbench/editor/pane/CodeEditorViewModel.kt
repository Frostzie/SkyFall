package io.github.frostzie.nodex.ui.viewmodel.ide.workbench.editor.pane

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

//TODO: Re-add
class CodeEditorViewModel {
    val content: StringProperty = SimpleStringProperty("")
    val isLocked: BooleanProperty = SimpleBooleanProperty(false)
}

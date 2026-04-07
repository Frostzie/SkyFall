package io.github.frostzie.nodex.domain.uicontract

import java.nio.file.Path

//TODO: Re-add
@Deprecated("Remove from uicontract")
sealed interface EditorPaneState {
    data object Empty : EditorPaneState
    data class Active(val path: Path) : EditorPaneState
}

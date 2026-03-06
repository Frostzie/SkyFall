package io.github.frostzie.nodex.ui.util

import io.github.frostzie.nodex.domain.config.WindowState
import javafx.stage.Stage

/**
 * A UI utility that tracks the geometry of a JavaFX Stage and gives
 * callbacks when the geometry changes.
 */
class WindowGeometryTracker(
    private val stage: Stage,
    private val onGeometryChanged: (WindowState) -> Unit
) {
    init {
        setupListeners()
    }

    private fun setupListeners() {
        // Track position
        stage.xProperty().addListener { _, _, newValue ->
            if (shouldTrack()) onGeometryChanged(getCurrentState().copy(x = newValue.toDouble()))
        }
        stage.yProperty().addListener { _, _, newValue ->
            if (shouldTrack()) onGeometryChanged(getCurrentState().copy(y = newValue.toDouble()))
        }

        // Track size
        stage.widthProperty().addListener { _, _, newValue ->
            if (shouldTrack()) onGeometryChanged(getCurrentState().copy(width = newValue.toDouble()))
        }
        stage.heightProperty().addListener { _, _, newValue ->
            if (shouldTrack()) onGeometryChanged(getCurrentState().copy(height = newValue.toDouble()))
        }

        // Track maximized state
        stage.maximizedProperty().addListener { _, _, maximized ->
            onGeometryChanged(getCurrentState().copy(isMaximized = maximized))
        }
    }

    /**
     * Prevent tracking while maximized, hidden, or during invalid states (NaN).
     */
    private fun shouldTrack(): Boolean {
        return stage.isShowing && !stage.isMaximized &&
                stage.x.isFinite() && stage.y.isFinite() &&
                stage.width.isFinite() && stage.height.isFinite()
    }

    /**
     * Captures the current state of the stage.
     */
    private fun getCurrentState(): WindowState {
        return WindowState(
            x = stage.x,
            y = stage.y,
            width = stage.width,
            height = stage.height,
            isMaximized = stage.isMaximized
        )
    }

    /**
     * Helper to apply a state to the stage.
     */
    fun applyState(state: WindowState) {
        stage.width = state.width
        stage.height = state.height
        stage.isMaximized = state.isMaximized

        if (state.x != -1.0 && state.y != -1.0) {
            stage.x = state.x
            stage.y = state.y
        } else if (!state.isMaximized) {
            stage.centerOnScreen()
        }
    }
}

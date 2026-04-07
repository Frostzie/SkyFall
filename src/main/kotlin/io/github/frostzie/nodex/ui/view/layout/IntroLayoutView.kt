package io.github.frostzie.nodex.ui.view.layout

import io.github.frostzie.nodex.ui.view.intro.IntroView
import javafx.scene.layout.StackPane

/**
 * The Intro Layout View.
 */
class IntroLayoutView(
    introView: IntroView
) : StackPane() {

    init {
        children.addAll(introView)
    }
}
package io.github.frostzie.nodex.services.ui

import atlantafx.base.theme.PrimerDark
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.application.Application
import java.util.concurrent.ConcurrentHashMap

//TODO: Remake just temp to get styles loaded.
class StylingService {
    private val logger = LoggerProvider.getLogger("StylingService")
    private val stylesheets = ConcurrentHashMap<String, String>()

    init {
        Application.setUserAgentStylesheet(PrimerDark().userAgentStylesheet)
        registerBuiltInCss()
    }

    fun getStylesheetUrls(): List<String> = stylesheets.values.toList()


    fun registerCss(id: String, classpathPath: String) {
        StylingService::class.java
            .getResource(classpathPath)
            ?.toExternalForm()
            ?.let { stylesheets[id] = it }
            ?: logger.warn("CSS resource not found: $classpathPath")
    }

    private fun registerBuiltInCss() {
        val base = "/assets/nodex/styling/components"
        registerCss("code-area", "$base/controls/code-area.css")
        registerCss("menu-button", "$base/controls/menu-button.css")
        registerCss("rectangle", "$base/primitives/rectangle.css")
        registerCss("settings", "$base/views/settings.css")
    }
}

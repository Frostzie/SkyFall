package io.github.frostzie.nodex.bootstrap

import io.github.frostzie.nodex.api.misc.Styling
import io.github.frostzie.nodex.ui.ScreenHost
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.Region
import javafx.stage.Stage

/**
 * Responsible for starting the UI layer.
 */
object UiBootstrap {
    private val logger = LoggerProvider.getLogger("UiBootstrap")

    fun start() {
        logger.info("Starting UI Layer...")

        ServiceBootstrap.concurrencyService.runOnUI {
            try {
                Platform.setImplicitExit(false)

                val stylingService = ServiceBootstrap.stylingService
                val viewFactory = ServiceBootstrap.viewFactory

                val screenHost = viewFactory.createScreenHost()
                val rootView = viewFactory.createRootView(screenHost)

                val scene = createScene(rootView, stylingService)

                initializeStageServices(screenHost, rootView, scene)

            } catch (e: Exception) {
                logger.error("Failed to initialize UI", e)
            }
        }
    }

    private fun createScene(rootView: Region, stylingService: Styling): Scene {
        val scene = Scene(rootView)
        scene.stylesheets.addAll(stylingService.getStylesheetUrls())
        return scene
    }

    private fun initializeStageServices(
        screenHost: ScreenHost,
        rootView: Region,
        scene: Scene
    ) {
        val primaryStage = createPrimaryStage()

        ServiceBootstrap.mainStage.registerNonCaptionNodes(screenHost.getNonCaptionNodes())
        ServiceBootstrap.mainStage.initialize(primaryStage, rootView, scene)

        ServiceBootstrap.overlayStage.setPrimaryStage(primaryStage)
        ServiceBootstrap.overlayStage.initialize()
    }

    private fun createPrimaryStage(): Stage {
        val primaryStage = Stage()
        primaryStage.title = "Nodex"
        try {
            primaryStage.icons.add(Image("assets/nodex/icon.png"))
        } catch (e: Exception) {
            logger.warn("Failed to load application icon: ${e.message}")
        }
        return primaryStage
    }

    fun toggleWindow() {
        ServiceBootstrap.concurrencyService.runOnUI {
            val service = ServiceBootstrap.mainStage
            if (service.isShowing() && !service.isIconified()) {
                service.hide()
            } else {
                service.show()
            }
        }
    }

    fun showAndFocusWindow() {
        ServiceBootstrap.concurrencyService.runOnUI {
            ServiceBootstrap.mainStage.show()
        }
    }
}

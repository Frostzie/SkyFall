package io.github.frostzie.nodex.bootstrap

import io.github.frostzie.nodex.services.ui.MainStageService
import io.github.frostzie.nodex.services.ui.OverlayStageService
import io.github.frostzie.nodex.services.ui.StylingService
import io.github.frostzie.nodex.ui.ScreenHost
import io.github.frostzie.nodex.ui.ViewFactory
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
                val viewFactory = createViewFactory()

                val screenHost = viewFactory.createScreenHost()
                val rootView = viewFactory.createRootView(screenHost)

                val scene = createScene(rootView, stylingService)

                initializeStageServices(screenHost, rootView, scene, viewFactory)

            } catch (e: Exception) {
                logger.error("Failed to initialize UI", e)
            }
        }
    }

    private fun createViewFactory(): ViewFactory {
        return ViewFactory(
            layoutService = ServiceBootstrap.layoutService,
            navigationService = ServiceBootstrap.navigationService,
            performanceService = ServiceBootstrap.performanceService,
            settingsService = ServiceBootstrap.settingsService,
            fileTreeService = ServiceBootstrap.fileTreeService,
            projectRuntimeService = ServiceBootstrap.projectRuntimeService,
            fileTreePersistenceService = ServiceBootstrap.fileTreePersistenceService,
            settingsRegistry = SettingsBootstrap.settingsRegistry
        )
    }

    private fun createScene(rootView: Region, stylingService: StylingService): Scene {
        val scene = Scene(rootView)
        scene.stylesheets.addAll(stylingService.getStylesheetUrls())
        return scene
    }

    private fun initializeStageServices(
        screenHost: ScreenHost,
        rootView: Region,
        scene: Scene,
        viewFactory: ViewFactory
    ) {
        val layoutService = ServiceBootstrap.layoutService
        val navigationService = ServiceBootstrap.navigationService
        val focusService = ServiceBootstrap.focusService
        val stylingService = ServiceBootstrap.stylingService

        val primaryStage = createPrimaryStage()

        val mainStageService = MainStageService(layoutService, navigationService, focusService)
        mainStageService.registerNonCaptionNodes(screenHost.getNonCaptionNodes())
        mainStageService.initialize(primaryStage, rootView, scene)
        this.mainStageService = mainStageService

        val overlayStageService = OverlayStageService(
            layoutService = layoutService,
            navigationService = navigationService,
            focusService = focusService,
            stylingService = stylingService,
            viewFactory = viewFactory
        )
        overlayStageService.setPrimaryStage(primaryStage)
        overlayStageService.initialize()
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

    private var mainStageService: MainStageService? = null

    fun toggleWindow() {
        ServiceBootstrap.concurrencyService.runOnUI {
            val service = mainStageService ?: return@runOnUI
            if (service.isShowing() && !service.isIconified()) {
                service.hide()
            } else {
                service.show()
            }
        }
    }

    fun showAndFocusWindow() {
        ServiceBootstrap.concurrencyService.runOnUI {
            mainStageService?.show()
        }
    }
}

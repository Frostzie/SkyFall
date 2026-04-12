package io.github.frostzie.nodex.bootstrap

import io.github.frostzie.nodex.api.concurrency.Concurrency
import io.github.frostzie.nodex.api.config.Config
import io.github.frostzie.nodex.api.config.FileTreePersistence
import io.github.frostzie.nodex.api.file.FileWatcher
import io.github.frostzie.nodex.api.misc.ModVersion
import io.github.frostzie.nodex.api.misc.PerformanceMonitor
import io.github.frostzie.nodex.api.misc.Styling
import io.github.frostzie.nodex.api.navigation.FocusTracker
import io.github.frostzie.nodex.api.navigation.Layout
import io.github.frostzie.nodex.api.navigation.MainStage
import io.github.frostzie.nodex.api.navigation.Navigation
import io.github.frostzie.nodex.api.navigation.OverlayStage
import io.github.frostzie.nodex.api.settings.Settings
import io.github.frostzie.nodex.api.workspace.ProjectRuntime

import io.github.frostzie.nodex.domain.entity.Project
import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.ui.ViewFactory
import io.github.frostzie.nodex.utils.LoggerProvider

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.nio.file.Path

/**
 * Services are resolved from Koin.
 * Exposes typed getters so nobody needs to import Koin directly.
 */
object ServiceBootstrap : KoinComponent {
    private val logger = LoggerProvider.getLogger("ServiceBootstrap")

    val modVersionService: ModVersion by inject()
    val concurrencyService: Concurrency by inject()
    val focusService: FocusTracker by inject()
    val fileWatcherService: FileWatcher by inject()
    val performanceService: PerformanceMonitor by inject()
    val configService: Config by inject()
    val settingsService: Settings by inject()
    val navigationService: Navigation by inject()
    val layoutService: Layout by inject()
    val fileTreePersistenceService: FileTreePersistence by inject()
    val projectRuntimeService: ProjectRuntime by inject()
    val stylingService: Styling by inject()
    val mainStage: MainStage by inject()
    val overlayStage: OverlayStage by inject()
    val viewFactory: ViewFactory by inject()

    fun start() {
        logger.info("Starting services...")
        startKoin { modules(appModule) }

        modVersionService.initialize()
        performanceService.initialize()
        navigationService.initialize(AppScreen.INTRO)
        fileWatcherService.initialize()
        configService.initialize()
        settingsService.initialize()

        // TODO: Remove temp
        val tempProjectPath = ""
        if (tempProjectPath.isNotBlank()) {
            projectRuntimeService.setProject(Project(Path.of(tempProjectPath)))
        }
    }

    fun stop() {
        logger.info("Shutting down services...")
        fileTreePersistenceService.flushPending()
        fileWatcherService.shutdown()
        concurrencyService.shutdown()
    }
}

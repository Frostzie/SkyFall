package io.github.frostzie.nodex.bootstrap

import io.github.frostzie.nodex.services.core.ConcurrencyService
import io.github.frostzie.nodex.services.core.FileService
import io.github.frostzie.nodex.services.core.PerformanceService
import io.github.frostzie.nodex.services.ui.NavigationService
import io.github.frostzie.nodex.services.ui.StylingService
import io.github.frostzie.nodex.services.ui.FocusService
import io.github.frostzie.nodex.services.files.FileWatcherService
import io.github.frostzie.nodex.services.files.FileTreeService
import io.github.frostzie.nodex.services.files.FileTreePersistenceService
import io.github.frostzie.nodex.services.workspace.ProjectRuntimeService
import io.github.frostzie.nodex.domain.entity.Project
import io.github.frostzie.nodex.services.settings.SettingsService
import io.github.frostzie.nodex.services.ui.ToolWindowService
import io.github.frostzie.nodex.services.core.LayoutService
import io.github.frostzie.nodex.services.core.ModVersionService
import io.github.frostzie.nodex.services.config.stationary.ConfigService
import io.github.frostzie.nodex.services.config.MigrationService
import io.github.frostzie.nodex.services.config.ConfigMoveService
import io.github.frostzie.nodex.services.config.BackupService
import io.github.frostzie.nodex.services.settings.SettingsValidationService
import io.github.frostzie.nodex.services.config.global.SettingsConfigService
import io.github.frostzie.nodex.services.config.global.ProjectsConfigService
import io.github.frostzie.nodex.services.config.project.ProjectConfigService
import io.github.frostzie.nodex.services.config.project.LayoutConfigService
import io.github.frostzie.nodex.services.config.project.TreeConfigService
import io.github.frostzie.nodex.loader.fabric.Folders
import io.github.frostzie.nodex.services.config.ConfigLocationService
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Path

/**
 * Responsible for initializing core logic services.
 */
object ServiceBootstrap {
    private val logger = LoggerProvider.getLogger("ServiceBootstrap")

    lateinit var concurrencyService: ConcurrencyService
    lateinit var focusService: FocusService
    lateinit var fileWatcherService: FileWatcherService
    lateinit var fileService: FileService
    lateinit var performanceService: PerformanceService
    lateinit var modVersionService: ModVersionService

    lateinit var configService: ConfigService
    lateinit var migrationService: MigrationService
    lateinit var configMoveService: ConfigMoveService
    lateinit var backupService: BackupService
    lateinit var settingsValidationService: SettingsValidationService
    lateinit var configLocationService: ConfigLocationService
    lateinit var settingsConfigService: SettingsConfigService
    lateinit var projectsConfigService: ProjectsConfigService
    lateinit var projectConfigService: ProjectConfigService
    lateinit var layoutConfigService: LayoutConfigService
    lateinit var treeConfigService: TreeConfigService

    lateinit var settingsService: SettingsService
    lateinit var navigationService: NavigationService
    lateinit var toolWindowService: ToolWindowService
    lateinit var layoutService: LayoutService
    lateinit var fileTreePersistenceService: FileTreePersistenceService

    lateinit var fileTreeService: FileTreeService
    lateinit var projectRuntimeService: ProjectRuntimeService

    lateinit var stylingService: StylingService

    fun start() {
        logger.info("Starting services...")
        initCore()
        initConfigServices()
        initPersistenceServices()
        initWorkspaceServices()
        initStyleServices()
        initFeatureServices()

        //TODO: Remove
        initDevHook()
    }

    private fun initCore() {
        concurrencyService = ConcurrencyService()
        focusService = FocusService()
        fileWatcherService = FileWatcherService(focusService, concurrencyService)
        fileWatcherService.initialize()
        fileService = FileService(fileWatcherService)

        performanceService = PerformanceService()
        performanceService.initialize()
        modVersionService = ModVersionService()
        modVersionService.initialize()
    }

    private fun initConfigServices() {
        val configRoot = Folders.configDir
        configService = ConfigService(configRoot, fileService, modVersionService)
        configService.initialize()

        migrationService = MigrationService()
        configMoveService = ConfigMoveService(fileService)
        backupService = BackupService(fileService)

        settingsValidationService = SettingsValidationService(SettingsBootstrap.settingsRegistry)

        configLocationService = ConfigLocationService(fileService, configService, configMoveService)
        val localNodexDir = configRoot.resolve("nodex")
        val effectiveNodexDir = configLocationService.resolveEffectiveNodexDir(localNodexDir)

        val settingsPath = effectiveNodexDir.resolve("settings.json")
        val backupDir = effectiveNodexDir.resolve("backups")
        val projectsPath = effectiveNodexDir.resolve("projects.json")

        settingsConfigService = SettingsConfigService(
            settingsPath,
            backupDir,
            fileService,
            configService,
            migrationService,
            modVersionService,
            backupService,
            settingsValidationService
        )

        projectsConfigService = ProjectsConfigService(projectsPath, backupDir, fileService, fileWatcherService, backupService)

        projectConfigService = ProjectConfigService(fileService, fileWatcherService)
        layoutConfigService = LayoutConfigService(projectConfigService, fileService)
        treeConfigService = TreeConfigService(fileService)
    }

    private fun initPersistenceServices() {
        settingsService = SettingsService(settingsConfigService, settingsValidationService)
        settingsService.initialize()
        navigationService = NavigationService(concurrencyService)
        toolWindowService = ToolWindowService()
        layoutService = LayoutService(toolWindowService)
        fileTreePersistenceService = FileTreePersistenceService(concurrencyService, treeConfigService)
    }

    private fun initWorkspaceServices() {
        fileTreeService = FileTreeService(fileWatcherService)
        projectRuntimeService = ProjectRuntimeService(fileWatcherService, fileTreeService, fileTreePersistenceService)
    }

    private fun initStyleServices() {
        stylingService = StylingService()
    }

    private fun initFeatureServices() {
        //TODO: Eventually
    }

    //TODO: Remove temp
    private fun initDevHook() {
        val tempProjectPath = ""
        if (tempProjectPath.isNotBlank()) {
            projectRuntimeService.setProject(Project(Path.of(tempProjectPath)))
        }
    }

    fun stop() {
        logger.info("Shutting down services...")

        fileTreePersistenceService.flushPending()

        if (::fileWatcherService.isInitialized) {
            fileWatcherService.shutdown()
        }

        if (::concurrencyService.isInitialized) {
            concurrencyService.shutdown()
        }
    }
}

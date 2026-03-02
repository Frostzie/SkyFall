package io.github.frostzie.nodex.services.config

import io.github.frostzie.nodex.domain.config.UniversalRuntimeConfig
import io.github.frostzie.nodex.loader.fabric.Folders
import io.github.frostzie.nodex.services.core.FileService
import io.github.frostzie.nodex.services.core.LayoutService
import io.github.frostzie.nodex.services.workspace.WorkspaceService
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Service responsible for managing the universal config lifecycle.
 */
class UniversalService(
    private val configService: ConfigService,
    private val universalConfigService: UniversalConfigService,
    private val layoutService: LayoutService,
    private val sessionService: SessionService,
    private val fileService: FileService,
    private val workspaceService: () -> WorkspaceService
) {
    private val logger = LoggerProvider.getLogger("UniversalService")

    /**
     * Computes the target universal config directory.
     * Returns null if universal config is disabled or path is invalid.
     */
    fun getUniversalConfigPath(): Path? {
        val runtimeConfig = universalConfigService.config
        if (!runtimeConfig.enabled) {
            return null
        }

        val basePath = runtimeConfig.basePath
        if (basePath.isBlank()) return null

        return try {
            Paths.get(basePath).resolve("Nodex").resolve("config")
        } catch (e: Exception) {
            logger.error("Invalid universal base path: $basePath", e)
            null
        }
    }

    /**
     * Initializes the universal config.
     * If enabled and target is empty, copies current configs to the target.
     * Then updates ConfigService with the new path.
     * 
     * @return true if initialization/migration succeeded, false otherwise.
     */
    fun initialize(): Boolean {
        val targetPath = getUniversalConfigPath()

        if (targetPath != null) {
            val currentPath = configService.configDir.toAbsolutePath()
            val targetAbsPath = targetPath.toAbsolutePath()

            if (currentPath == targetAbsPath) {
                logger.debug("Already at universal path: {}", targetAbsPath)
                return true
            }

            if (!fileService.exists(targetPath)) {
                try {
                    fileService.createDirectory(targetPath)
                } catch (e: Exception) {
                    logger.error("Failed to create universal config directory: $targetPath", e)
                    return false
                }
            }

            if (fileService.exists(targetPath) && fileService.isDirectoryEmpty(targetPath)) {
                logger.debug("Universal target is empty. Performing one-time migration to: {}", targetPath)
                if (!migrateConfigs(currentPath, targetPath)) {
                    logger.error("Migration failed. Aborting universal config switch.")
                    return false
                }
            }

            if (fileService.exists(targetPath)) {
                return configService.setCustomConfigPath(targetPath)
            }
            return false
        } else {
            // Use default if not enabled
            val defaultPath = Folders.configDir.resolve("nodex").toAbsolutePath()
            if (configService.configDir.toAbsolutePath() != defaultPath) {
                logger.debug("Universal config disabled, reverting to default.")
                return configService.setCustomConfigPath(null)
            }
            return true
        }
    }

    /**
     * Performs a runtime switch of the config directory.
     * 
     * Follows these steps:
     * 1. Flush: Forces all in-memory states (tabs, window size, session) to save to the old path.
     *    If this step fails, switching is aborted.
     * 2. Update Pointer: Changes the universal_config.json to point to the new path.
     * 3. Initialize: Ensures the new path exists and performs a migration if the target is empty.
     *    If this step fails, runtime config + config pointer are rolled back.
     * 4. Reload: Tells all dependent services to re-read their files from the new path.
     */
    fun switchTo(enabled: Boolean, basePath: String) {
        val oldConfigRoot = configService.configDir.toAbsolutePath()
        if (!flushCurrentState(oldConfigRoot)) {
            logger.error("Switch aborted because pre-switch state flush failed.")
            return
        }

        val oldConfig = universalConfigService.config
        if (!universalConfigService.save(UniversalRuntimeConfig(enabled, basePath))) {
            logger.error("Switch aborted because universal runtime config could not be persisted.")
            return
        }

        if (!initialize()) {
            logger.error("Switch failed during initialization. Rolling back config pointer.")
            val runtimeRollbackOk = universalConfigService.save(oldConfig)
            val pointerRollbackOk = rollbackConfigPointer(oldConfigRoot)
            if (!runtimeRollbackOk || !pointerRollbackOk) {
                logger.error(
                    "Rollback was incomplete. runtimeRollbackOk={}, pointerRollbackOk={}",
                    runtimeRollbackOk,
                    pointerRollbackOk
                )
            }
            reloadDependentServices()
            return
        }

        reloadDependentServices()
    }

    /**
     * @return true if migration succeeded or was not needed.
     */
    private fun migrateConfigs(source: Path, target: Path): Boolean {
        return try {
            if (!fileService.exists(source)) return true

            // Guard against recursion/infinite copy:
            // Ensure the target is not a sub-folder of the source.
            if (target.toAbsolutePath().startsWith(source.toAbsolutePath())) {
                logger.warn("Source is parent of target, skipping migration to avoid recursion.")
                return false
            }

            fileService.copy(source, target)
            logger.debug("Successfully migrated configs from {} to {}", source, target)
            true
        } catch (e: Exception) {
            logger.error("Failed to migrate configs", e)
            false
        }
    }

    private fun reloadDependentServices() {
        configService.reload()
        layoutService.reload()
        sessionService.initialize() // This also acts as reload
        workspaceService().load()
    }

    private fun rollbackConfigPointer(oldConfigRoot: Path): Boolean {
        val defaultPath = Folders.configDir.resolve("nodex").toAbsolutePath()
        val oldCustomPath = if (oldConfigRoot == defaultPath) null else oldConfigRoot
        return configService.setCustomConfigPath(oldCustomPath)
    }

    private fun flushCurrentState(configRoot: Path): Boolean {
        return try {
            // Snapshot current persisted state before save calls
            val expectedLayoutFile = configRoot.resolve("layout.json")
            val expectedSessionFile = configRoot.resolve("session.json")
            val hadLayoutBefore = fileService.exists(expectedLayoutFile)
            val hadSessionBefore = fileService.exists(expectedSessionFile)

            layoutService.save()
            sessionService.save()
            workspaceService().save()

            // If files already existed, they must still be present after flush
            val layoutMissingAfterFlush = hadLayoutBefore && !fileService.exists(expectedLayoutFile)
            val sessionMissingAfterFlush = hadSessionBefore && !fileService.exists(expectedSessionFile)
            if (layoutMissingAfterFlush || sessionMissingAfterFlush) {
                logger.error(
                    "Flush verification failed. layoutMissingAfterFlush={}, sessionMissingAfterFlush={}, configRoot={}",
                    layoutMissingAfterFlush,
                    sessionMissingAfterFlush,
                    configRoot
                )
                return false
            }
            true
        } catch (e: Exception) {
            logger.error("Failed to flush states before switch.", e)
            false
        }
    }
}

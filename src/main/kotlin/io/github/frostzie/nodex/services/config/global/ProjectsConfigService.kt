package io.github.frostzie.nodex.services.config.global

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.frostzie.nodex.domain.entity.RecentProject
import io.github.frostzie.nodex.services.config.BackupService
import io.github.frostzie.nodex.services.core.FileService
import io.github.frostzie.nodex.services.files.FileWatcherService
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Path

/**
 * Service responsible for reading the projects history (projects.json).
 * Supports silent reloads when external changes are detected.
 */
class ProjectsConfigService(
    private val projectsPath: Path,
    private val backupDir: Path,
    private val fileService: FileService,
    private val fileWatcherService: FileWatcherService,
    private val backupService: BackupService
) {
    private val logger = LoggerProvider.getLogger("ProjectsConfigService")
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    private var onReload: ((List<RecentProject>) -> Unit)? = null

    /**
     * Initializes the service by setting up a file watcher for silent reloads.
     */
    fun initialize(onReload: (List<RecentProject>) -> Unit) {
        this.onReload = onReload
        fileWatcherService.watchFile(projectsPath) { _, _ ->
            logger.info("External change detected in projects history, reloading")
            try {
                val projects = load()
                onReload(projects)
            } catch (e: Exception) {
                logger.error("Failed to reload projects history after external change", e)
            }
        }
    }

    /**
     * Loads projects history from disk.
     */
    fun load(): List<RecentProject> {
        if (!fileService.exists(projectsPath)) {
            return emptyList()
        }

        return try {
            val json = fileService.readText(projectsPath)
            mapper.readValue(json, object : TypeReference<List<RecentProject>>() {})
        } catch (e: Exception) {
            logger.error("Failed to load projects history from: $projectsPath", e)
            emptyList()
        }
    }

    /**
     * Saves [projects] history to disk.
     */
    fun save(projects: List<RecentProject>) {
        try {
            fileWatcherService.ignorePath(projectsPath)

            val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(projects)
            fileService.writeAtomic(projectsPath, json)
            logger.info("Projects history saved successfully to: $projectsPath")

            backupService.backup(projectsPath, backupDir, 3)
        } catch (e: Exception) {
            logger.error("Failed to save projects history to: $projectsPath", e)
            throw e
        }
    }
}

package io.github.frostzie.nodex.services.workspace

import io.github.frostzie.nodex.domain.entity.Project
import io.github.frostzie.nodex.services.files.FileTreePersistenceService
import io.github.frostzie.nodex.services.files.FileTreeService
import io.github.frostzie.nodex.services.files.FileWatcherService
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import java.nio.file.Path

/**
 * Manages the active project lifecycle at runtime.
 */
class ProjectRuntimeService(
    private val fileWatcherService: FileWatcherService,
    private val fileTreeService: FileTreeService,
    private val fileTreePersistenceService: FileTreePersistenceService
) {
    private val logger = LoggerProvider.getLogger("ProjectRuntimeService")
    private val _currentProject = SimpleObjectProperty<Project?>()

    private val _loadedExpandedPaths = SimpleObjectProperty<Set<Path>>(emptySet())
    val loadedExpandedPathsProperty: ReadOnlyObjectProperty<Set<Path>> = _loadedExpandedPaths

    private var isBuilding = false
    private var pendingTick = false

    private val tickListener = ChangeListener<Number> { _, _, _ ->
        val project = _currentProject.get()
        if (project != null) {
            if (isBuilding) {
                pendingTick = true
            } else {
                fileTreeService.onFilesystemTick(project)
            }
        }
    }

    /**
     * Switches the active project, rebuilding the file tree and watcher state.
     */
    fun setProject(project: Project) {
        val oldProject = _currentProject.get()
        if (oldProject == project) return

        logger.debug("Setting current project to: {}", project.path)

        clearProject()

        _currentProject.set(project)
        _loadedExpandedPaths.set(fileTreePersistenceService.loadOnProjectOpen(project.path))

        isBuilding = true
        pendingTick = false
        try {
            fileTreeService.build(project)
        } finally {
            isBuilding = false
        }

        fileWatcherService.watch(project)

        project.filesystemTick.addListener(tickListener)

        if (pendingTick) {
            pendingTick = false
            fileTreeService.onFilesystemTick(project)
        }
    }

    /**
     * Clears the current project, stopping its watcher and flushing state.
     */
    fun clearProject() {
        val project = _currentProject.get() ?: return

        project.filesystemTick.removeListener(tickListener)
        fileTreePersistenceService.flushPending()
        fileWatcherService.unwatch(project.path)

        _currentProject.set(null)
        _loadedExpandedPaths.set(emptySet())
        pendingTick = false

        logger.debug("Project cleared: {}", project.path)
    }

    fun getCurrentProject(): Project? = _currentProject.get()
}

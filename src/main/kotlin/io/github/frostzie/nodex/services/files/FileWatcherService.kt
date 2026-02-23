package io.github.frostzie.nodex.services.files

import io.github.frostzie.nodex.domain.entity.Project
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.file.FileSystemWatcher
import io.methvin.watcher.DirectoryChangeEvent.EventType
import javafx.application.Platform
import java.nio.file.Path
import java.util.concurrent.*

/**
 * Manages file system synchronization for active projects.
 *
 * This service interprets raw disk changes into state updates.
 * It makes sure the external changes sync with the mod.
 */
class FileWatcherService {
    private val logger = LoggerProvider.getLogger("FileWatcherService")
    private val watchers = ConcurrentHashMap<Path, ProjectWatcher>()

    private val syncExecutor = Executors.newSingleThreadScheduledExecutor()
    private val ignoredPaths = ConcurrentHashMap<Path, Long>()
    private val ignoreDurationMs = 2000L
    private val projectChangeQueues = ConcurrentHashMap<Path, ConcurrentLinkedQueue<FileTreeChange>>()
    private val debounceDelayMs = 500L
    private val maxQueueSize = 50 // Possibly allow changing this through settings

    @Volatile
    private var isWindowFocused = true // Uses StageService for fucus listener
    private val pendingProjectsToSync = ConcurrentHashMap.newKeySet<Path>()
    private val pendingSyncs = ConcurrentHashMap<Path, ScheduledFuture<*>>()

    private data class ProjectWatcher(
        val project: Project,
        val watcher: FileSystemWatcher
    )

    /**
     * Starts monitoring disk state for a project.
     *
     * This method triggers project-level invalidation (via filesystemTick) upon disk changes.
     * It provides file-level change details through [drainChanges], signaling that
     * parts of the project need to be re-synced.
     *
     * The [Project.filesystemTick] acts as the primary trigger; listeners should call
     * [drainChanges] to retrieve the actual modification details.
     */
    fun watch(project: Project) {
        val root = project.path
        if (watchers.containsKey(root)) return

        val watcher = FileSystemWatcher(root) { path, action ->
            onRawAction(project, path, action)
        }
        watcher.start()
        watchers[root] = ProjectWatcher(project, watcher)
        logger.debug("Monitoring project state at: {}", root)
    }

    /**
     * Drains the current change queue for a given project root.
     *
     * This clears the queue and ensures events are returned in the exact order they were sent.
     *
     * @param projectRoot The root path of the project.
     * @return A list of [FileTreeChange] events that have occurred since the last drain.
     */
    fun drainChanges(projectRoot: Path): List<FileTreeChange> {
        val queue = projectChangeQueues[projectRoot] ?: return emptyList()
        val changes = mutableListOf<FileTreeChange>()
        while (true) {
            val change = queue.poll() ?: break
            changes.add(change)
        }
        return changes
    }

    /**
     * Updates focus state to sync queued changes upon gaining focus.
     *
     * When focus is regained, any projects that had filesystem activity while the app
     * was backgrounder are notified. This triggers a drain of all stored changes.
     */
    fun setWindowFocused(focused: Boolean) {
        isWindowFocused = focused
        if (focused) {
            val iterator = pendingProjectsToSync.iterator()
            while (iterator.hasNext()) {
                val root = iterator.next()
                iterator.remove()
                val watcher = watchers[root]
                if (watcher != null) {
                    notifySync(watcher.project)
                }
            }
        }
    }

    /**
     * Temporarily ignores a path to prevent reload loops.
     */
    fun ignorePath(path: Path) {
        ignoredPaths[path] = System.currentTimeMillis()
    }

    /**
     * Entrypoint for raw file system events, intended for internal use and testing.
     * Interprets raw [EventType] actions into high-level [FileTreeChange] events.
     */
    internal fun onRawAction(project: Project, path: Path, action: EventType) {
        if (shouldIgnore(path)) {
            logger.debug("Ignoring internal change at: {}", path)
            return
        }

        val root = project.path

        // Always track specific events to avoid forced full rescans
        handleRawEvent(project, path, action)

        if (!isWindowFocused) {
            pendingProjectsToSync.add(root)
        }
    }

    private fun handleRawEvent(project: Project, path: Path, action: EventType) {
        val root = project.path
        when (action) {
            EventType.MODIFY -> {
                enqueueChange(root, FileTreeChange.FileModified(path))
                scheduleSync(project)
            }

            EventType.DELETE -> {
                enqueueChange(root, FileTreeChange.FileDeleted(path))
                scheduleSync(project)
            }

            EventType.CREATE -> {
                enqueueChange(root, FileTreeChange.FileCreated(path))
                scheduleSync(project)
            }

            else -> {}
        }
    }

    /**
     * Enqueues a change for a project, with a measures to prevent UI flooding.
     *
     * If too many changes accumulate before a sync, the nearest parent folder is invalidated
     * instead of tracking hundreds of specific events. This triggers a targeted refresh
     * rather than a whole-project rescan.
     */ // First time using a word like "enqueue" but sure google ig it describes it the best lol
    private fun enqueueChange(root: Path, change: FileTreeChange) {
        val queue = projectChangeQueues.computeIfAbsent(root) { ConcurrentLinkedQueue() }

        val first = queue.peek()
        if (first is FileTreeChange.FileSystemRescanRequired) return

        if (queue.size >= maxQueueSize || change is FileTreeChange.FileSystemRescanRequired) {
            queue.clear()
            queue.add(FileTreeChange.FileSystemRescanRequired)
            return
        }

        queue.add(change)
    }

    private fun shouldIgnore(path: Path): Boolean {
        val now = System.currentTimeMillis()
        val iterator = ignoredPaths.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value > ignoreDurationMs) {
                iterator.remove()
                continue
            }
            if (path.startsWith(entry.key)) return true
        }
        return false
    }

    private fun scheduleSync(project: Project) {
        val root = project.path
        pendingSyncs[root]?.cancel(false)
        pendingSyncs[root] = syncExecutor.schedule({
            pendingSyncs.remove(root)
            if (isWindowFocused) {
                notifySync(project)
            } else {
                pendingProjectsToSync.add(root)
            }
        }, debounceDelayMs, TimeUnit.MILLISECONDS)
    }

    private fun notifySync(project: Project) {
        // Increment state tick on UI thread to notify observers
        Platform.runLater {
            project.filesystemTick.set(project.filesystemTick.get() + 1)
        }
    }

    /**
     * Stops monitoring all projects.
     *
     * This resets the watcher state but does not shut down the background executor.
     */
    fun stopAll() {
        watchers.values.forEach { it.watcher.stop() }
        watchers.clear()

        pendingSyncs.values.forEach { it.cancel(false) }
        pendingSyncs.clear()

        pendingProjectsToSync.clear()
        ignoredPaths.clear()
        projectChangeQueues.clear()

        logger.debug("Stopped all monitoring and cleared pending state")
    }

    /**
     * Permanently shuts down the file watcher service and its background threads.
     */
    fun shutdown() {
        stopAll()
        syncExecutor.shutdownNow()
    }
}

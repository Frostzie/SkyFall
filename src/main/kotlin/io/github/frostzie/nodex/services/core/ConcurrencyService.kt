package io.github.frostzie.nodex.services.core

import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.application.Platform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Centralizes thread management and coroutine scopes for the app.
 *
 * This service provides specific dispatchers and scopes for I/O, CPU-intensive tasks,
 * and UI updates, ensuring consistent threading policies across the app.
 * It also manages the lifecycle of global background tasks.
 *
 * @see kotlinx.coroutines.Dispatchers
 */
class ConcurrencyService {
    private val logger = LoggerProvider.getLogger("ConcurrencyService")

    // Master Job that controls the lifecycle of the entire app's background tasks
    private val masterJob = SupervisorJob()

    /**
     * Dispatcher for Input/Output operations (File reading, Network).
     * Backed by [Dispatchers.IO] (Elastic thread pool).
     */
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    /**
     * Dispatcher for UI operations (Main = JavaFX App Thread).
     * Use this within coroutines to switch to the UI thread.
     */
    val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    /**
     * Dispatcher for CPU-intensive operations (Data parsing, calculations).
     */
    val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * A global scope for IO operations that should live as long as the app is running.
     * Use this for firing background tasks in Services.
     */
    val ioScope = CoroutineScope(ioDispatcher + masterJob)

    /**
     * A global scope for CPU operations.
     */
    val cpuScope = CoroutineScope(cpuDispatcher + masterJob)

    /**
     * Helper to run a block on the UI thread safely.
     * If already on the UI thread, run immediately. Otherwise, queues it.
     * Use this for simple data updates (e.g., updating a Label text or a List).
     */
    fun runOnUI(action: () -> Unit) {
        if (Platform.isFxApplicationThread()) {
            action()
        } else {
            Platform.runLater(action)
        }
    }

    /**
     * Helper to run a block on the I/O thread pool asynchronously.
     */
    fun runOnIO(action: () -> Unit) {
        ioScope.launch { action() }
    }

    /**
     * Helper to run a block on the CPU-intensive thread pool asynchronously.
     */
    fun runOnCPU(action: () -> Unit) {
        cpuScope.launch { action() }
    }

    /**
     * Forces the action to run on the next UI pulse, even if currently on the UI thread.
     * Use this for focus requests, scrolling, or something that need the Scene Graph to settle first.
     */
    fun runLater(action: () -> Unit) {
        Platform.runLater(action)
    }

    /**
     * Cancels all active coroutines in the application.
     * Call this when the application is shut down to ensure clean exit.
     */
    fun shutdown() {
        logger.info("Shutting down ConcurrencyService.")
        masterJob.cancel()
        logger.debug("All background scopes cancelled.")
    }
}

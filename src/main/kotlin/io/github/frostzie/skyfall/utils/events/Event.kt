package io.github.frostzie.skyfall.utils.events

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Base class for events
 */
open class Event {
    var isCancelled: Boolean = false
        private set

    fun post() {
        EventBus.post(this)
    }

    fun setCancelled(cancelled: Boolean) {
        this.isCancelled = cancelled
    }

    /**
     * Marks this event as canceled.
     */
    fun cancel() {
        this.isCancelled = true
    }
}

/**
 * Event fired when a key is initially pressed down
 */
class KeyDownEvent(val keyCode: Int) : Event()

/**
 * Event fired each tick that a key is held
 */
class KeyPressEvent(val keyCode: Int) : Event()

/**
 * Simple event bus implementation with functional listeners
 */
object EventBus {
    private val listeners = mutableMapOf<Class<out Event>, CopyOnWriteArrayList<EventListener<*>>>()

    /**
     * Register a listener for an event type
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Event> listen(eventClass: Class<T>, priority: Int = 0, handler: (T) -> Unit) {
        val listener = EventListener(handler, priority)
        listeners.computeIfAbsent(eventClass) { CopyOnWriteArrayList() }
            .add(listener as EventListener<*>)

        listeners[eventClass]?.sortBy { it.priority }
    }

    /**
     * Post an event to all registered listeners
     */
    @Suppress("UNCHECKED_CAST")
    fun post(event: Event) {
        val eventClass = event.javaClass
        listeners[eventClass]?.forEach { listener ->
            try {
                (listener as EventListener<Event>).handler(event)
            } catch (e: Exception) {
                println("Error invoking event handler for ${eventClass.simpleName}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Clear all event listeners
     */
    fun clearAll() {
        listeners.clear()
    }

    /**
     * Data class representing an event listener
     */
    private data class EventListener<T : Event>(
        val handler: (T) -> Unit,
        val priority: Int
    )
}
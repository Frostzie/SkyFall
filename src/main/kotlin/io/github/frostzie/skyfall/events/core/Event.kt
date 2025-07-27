package io.github.frostzie.skyfall.events.core

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Base class for events
 */
open class Event {
    /**
     * The cancellation state of the event.
     * The setter is `internal` to enforce safer coding practices.
     */
    var isCancelled: Boolean = false
        internal set

    /**
     * Posts this event to the EventBus.
     */
    fun post() {
        EventBus.post(this)
    }

    /**
     * Marks this event as canceled. This is the only intended way to cancel an event.
     * A debug stack trace has been added to find the source of the movement bug.
     */
    fun cancel() {
        this.isCancelled = true
    }
}

/**
 * Event fired when a key is initially pressed down
 */
class KeyDownEvent(val keyCode: Int) : Event() //TODO: Remove?

/**
 * Event fired each tick that a key is held
 */
class KeyPressEvent(val keyCode: Int) : Event() //TODO: Remove?

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

        listeners[eventClass]?.sortByDescending { it.priority }
    }

    /**
     * Post an event to all registered listeners
     */
    @Suppress("UNCHECKED_CAST")
    fun post(event: Event) {
        val eventClass = event.javaClass
        listeners[eventClass]?.forEach { listener ->
            try {
                if (event.isCancelled) return

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
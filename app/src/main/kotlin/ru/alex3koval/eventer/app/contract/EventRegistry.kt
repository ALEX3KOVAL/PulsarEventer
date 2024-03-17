package ru.alex3koval.eventer.app.contract

import kotlinx.serialization.KSerializer

/**
 * Обертка для обработчика событий
 */
data class EventHandler(
    val serializer: Any,
    val handler: Any
)

interface EventRegistry {
    /**
     * Регистрация события
     */
    fun <T : Any> register(
        eventName: String,
        serializer: KSerializer<T>,
        handler: (T) -> Result<Boolean>
    )

    fun run()

    fun handlersForEvent(eventName: String): List<EventHandler>
}
package ru.alex3koval.eventer.app.vo

import kotlinx.serialization.Serializable
import alex3koval.contract.EventProducer
import ru.alex3koval.eventer.app.extensions.DateTimeAsStringSerializer
import java.time.LocalDateTime

/**
 * Событие
 */
class Event<T>(
    @Serializable(with = DateTimeAsStringSerializer::class)
    val date: LocalDateTime,
    val payload: T,
    val producer: EventProducer,
    val key: String,
    val description: String? = null
)
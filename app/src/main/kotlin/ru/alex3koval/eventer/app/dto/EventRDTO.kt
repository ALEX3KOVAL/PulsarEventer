package ru.alex3koval.eventer.app.dto

import ru.alex3koval.eventer.app.vo.EventID
import java.time.LocalDateTime

data class EventRDTO(
    val id: EventID,
    val aggregateId: String,
    val hash: String,
    val jsonPayload: String,
    val producer: String,
    val type: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val comment: String?,
    val topic: String?
)
package ru.alex3koval.eventer.app.persister

import kotlinx.serialization.json.JsonElement
import ru.alex3koval.eventer.app.contract.EventProducer
import java.time.LocalDateTime

data class CreateEventDTO(
    val aggregateID: String,
    val hash: String,
    val json: JsonElement,
    val producerName: EventProducer,
    val typeClass: String,
    val createdDate: LocalDateTime,
    val updateDate: LocalDateTime,
    val comment: String?,
    val topic: String
)
package ru.alex3koval.eventer.storage

import org.jetbrains.exposed.sql.select
import ru.alex3koval.eventer.app.dto.EventRDTO
import ru.alex3koval.eventer.app.repository.EventRepository
import ru.alex3koval.eventer.app.vo.EventID

class EventRepositoryImpl : EventRepository {
    override fun get(id: EventID): EventRDTO? =
        EventStoreTable
            .select { EventStoreTable.id eq id.value }
            .limit(1)
            .singleOrNull()
            ?.let { row ->
                EventRDTO(
                    id = row[EventStoreTable.id].value.let { EventID(it).getOrThrow() },
                    aggregateId = row[EventStoreTable.aggregateId],
                    topic = row[EventStoreTable.topic],
                    hash = row[EventStoreTable.hash],
                    jsonPayload = row[EventStoreTable.jsonPayload],
                    producer = row[EventStoreTable.producer],
                    type = row[EventStoreTable.type],
                    createdAt = row[EventStoreTable.createdAt],
                    updatedAt = row[EventStoreTable.updatedAt],
                    comment = row[EventStoreTable.comment]
                )
            }
}
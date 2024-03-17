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
            ?.run {
                EventRDTO(
                    id = get(EventStoreTable.id).value.let { EventID(it).getOrThrow() },
                    aggregateId = get(EventStoreTable.aggregateId),
                    topic = get(EventStoreTable.topic),
                    hash = get(EventStoreTable.hash),
                    jsonPayload = get(EventStoreTable.jsonPayload),
                    producer = get(EventStoreTable.producer),
                    type = get(EventStoreTable.type),
                    comment = get(EventStoreTable.comment),
                    createdAt = get(EventStoreTable.createdAt),
                    updatedAt = get(EventStoreTable.updatedAt),
                )
            }
}
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
                    row[EventStoreTable.id].value.let { EventID(it).getOrThrow() }
                )
            }
}
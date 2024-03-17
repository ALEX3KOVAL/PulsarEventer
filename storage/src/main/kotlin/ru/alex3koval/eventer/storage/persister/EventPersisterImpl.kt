package ru.alex3koval.eventer.storage.persister

import ru.alex3koval.eventer.app.contract.EventPersister
import ru.alex3koval.eventer.storage.extensions.singleUpdate
import ru.alex3koval.eventer.app.persister.CreateEventDTO
import ru.alex3koval.eventer.app.vo.EventStatus
import ru.alex3koval.eventer.storage.EventStoreTable
import java.time.LocalDateTime

class EventPersisterImpl : EventPersister {
    override fun add(dto: CreateEventDTO): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun updateStatus(hash: String, newStatus: EventStatus, updatedAt: LocalDateTime): Result<Unit> =
        EventStoreTable.singleUpdate(where = { EventStoreTable.hash eq hash }) {
            it[this.status] = newStatus.value
            it[this.updatedAt] = updatedAt
        }
}
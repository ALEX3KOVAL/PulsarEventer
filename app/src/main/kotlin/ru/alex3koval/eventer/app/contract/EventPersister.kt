package ru.alex3koval.eventer.app.contract

import ru.alex3koval.eventer.app.persister.CreateEventDTO
import ru.alex3koval.eventer.app.vo.EventStatus
import java.time.LocalDateTime

interface EventPersister {
    fun add(dto: CreateEventDTO): Result<Unit>

    fun updateStatus(hash: String, newStatus: EventStatus, updatedAt: LocalDateTime): Result<Unit>
}
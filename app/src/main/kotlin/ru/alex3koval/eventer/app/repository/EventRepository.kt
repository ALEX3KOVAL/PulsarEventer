package ru.alex3koval.eventer.app.repository

import ru.alex3koval.eventer.app.dto.EventRDTO
import ru.alex3koval.eventer.app.vo.EventID

interface EventRepository {
    fun get(id: EventID): EventRDTO?
}
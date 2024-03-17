package ru.alex3koval.eventer.app.vo

import ru.alex3koval.eventer.app.extensions.Checker.Companion.message
import ru.alex3koval.eventer.app.extensions.failIf

class EventID private constructor(val value: Int) {
    init {
        failIf { value < 0 } message "ID события должен быть больше 0"
    }

    companion object {
        operator fun invoke(value: Int): Result<EventID> = runCatching {
            EventID(value)
        }
    }
}
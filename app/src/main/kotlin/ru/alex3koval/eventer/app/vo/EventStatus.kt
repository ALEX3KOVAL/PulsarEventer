package ru.alex3koval.eventer.app.vo

enum class EventStatus(val value: Int) {
    CREATED(0),
    IN_PROCESS(1),
    ERROR(2),
    WAITING(3),
    PROCESSED(4);
}
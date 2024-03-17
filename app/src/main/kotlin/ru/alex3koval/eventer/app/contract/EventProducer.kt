package ru.alex3koval.eventer.app.contract

sealed interface EventProducer {
    val value: String

    enum class Domain(override val value: String) : EventProducer {
        MODULE("domain.module"),
        COMMAND("domain.command");
    }
}
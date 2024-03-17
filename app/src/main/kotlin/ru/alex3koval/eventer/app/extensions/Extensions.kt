package ru.alex3koval.eventer.app.extensions

import ru.alex3koval.eventer.app.EventerException

internal fun failIf(block: () -> Boolean): Checker = Checker(block)

internal class Checker(private val condition: () -> Boolean) {
    private fun check(message: String) {
        if (condition()) {
            throw EventerException(message)
        }
    }

    companion object {
        infix fun Checker.message(value: String) = check(value)
    }
}

fun <T> T?.ifNull(block: () -> T): T = when (this) {
    null -> block()
    else -> this
}


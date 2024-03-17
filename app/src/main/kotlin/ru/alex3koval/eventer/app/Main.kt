package ru.alex3koval.eventer.app

import kotlinx.coroutines.*
import ru.coderocket.sza.configure.settings.SystemSettings
import kotlin.concurrent.thread

fun main() {
    val appName = "eventer-${(1..999).random()}-${(1..999).random()}"
    val numberOfCoroutines = System.getenv("NUMBER_OF_COROUTINES")!!.toInt()

    val workers = mutableMapOf<Job, EventFlowConsumer>()
    val scope = CoroutineScope(Dispatchers.IO)

    EventerDI.init()

    (1..numberOfCoroutines).forEach { _ ->
        val efc = EventerDI.get<EventFlowConsumer>().getOrThrow()
        val topics = EventerDI
            .get<SystemSettings>()
            .getOrThrow()
            .pulsar
            .topics
        val job = scope.launch {
            efc.run(appName = appName, topics = topics)
        }

        workers[job] = efc
    }

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        println("STOPPING")
        workers.forEach { (_, worker) ->
            worker.stop()
        }

        println("WAITING")
        runBlocking {
            workers.keys.forEach { it.join() }
        }

        println("STOPPED")
    })
}
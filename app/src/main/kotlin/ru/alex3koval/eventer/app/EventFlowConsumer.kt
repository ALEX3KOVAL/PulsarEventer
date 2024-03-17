package ru.alex3koval.eventer.app

import com.sun.org.slf4j.internal.Logger
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import org.apache.pulsar.client.api.*
import org.jetbrains.exposed.sql.transactions.transaction
import ru.alex3koval.eventer.app.contract.EventPersister
import ru.alex3koval.eventer.app.contract.EventRegistry
import ru.alex3koval.eventer.app.vo.EventStatus
import ru.alex3koval.eventer.app.vo.EventTransportContainer
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.measureTimedValue

/**
 * Обработчик событий
 */
class EventFlowConsumer(
    private val pulsarClient: PulsarClient,
    private val persister: EventPersister,
    private val eventRegistry: EventRegistry,
    private val logger: Logger
) {
    private var needStop: Boolean = false

    private lateinit var name: String
    private lateinit var consumer: Consumer<String>

    fun run(
        appName: String,
        topics: List<String>,
        subscription: String = "main"
    ) {
        name = "$appName-flow-consumer-${hashCode()}"

        println("RUN FLOW CONSUMER: $name")

        consumer = pulsarClient
            .newConsumer(Schema.STRING)
            .topic(*topics.toTypedArray())
            .subscriptionName(subscription)
            .consumerName(name)
            .negativeAckRedeliveryDelay(20, TimeUnit.SECONDS)
            .subscriptionType(SubscriptionType.Key_Shared)
            .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
            .subscribe()

        while (true) {
            if (needStop) {
                println("FlowConsumer $name is stopped")
                consumer.close()
                return
            }

            val msgR = runCatching { consumer.receive(2, TimeUnit.SECONDS) }

            if (msgR.isFailure) {
                logger.error("${msgR.exceptionOrNull()}")
                continue
            }

            val msg = msgR.getOrThrow() ?: continue

            val hash = msg.getProperty("hash")
            persister.updateStatus(hash, EventStatus.IN_PROCESS, LocalDateTime.now()).getOrThrow()

            transaction {
                val result = measureTimedValue { msg.process() }.run {
                    value
                }

                try {
                    result
                        .onSuccess {
                            persister.updateStatus(hash, EventStatus.PROCESSED, LocalDateTime.now()).getOrThrow()
                            consumer.acknowledge(msg)
                            commit()
                        }
                        .onFailure { err ->
                            consumer.negativeAcknowledge(msg)
                            rollback()
                            persister.updateStatus(hash, EventStatus.ERROR, LocalDateTime.now()).getOrThrow()
                            logger.error("Ошибка при обработке $msg: ${err.message}")
                        }
                } catch (err: Exception) {
                    logger.error("Ошибка при обработке $msg: ${err.message}")
                }
            }
        }
    }

    private fun Message<String>.process(): Result<Message<String>> = runCatching {
        val eventTC = Json.decodeFromString<EventTransportContainer>(value)

        eventRegistry.handlersForEvent(eventTC.eventName).forEach { eh ->
            val event = Json.decodeFromString(eh.serializer as DeserializationStrategy<Any>, eventTC.jsonPayload)
            val handler = eh.handler as (Any) -> Result<Boolean>

            handler.invoke(event)
        }

        this
    }

    fun stop() {
        println("Flow consumer receive signal for stop")
        needStop = true
    }
}
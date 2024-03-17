package ru.alex3koval.eventer.app.factory

import org.apache.pulsar.client.api.PulsarClient
import org.slf4j.Logger
import ru.alex3koval.eventer.app.contract.EventPersister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.Schema
import org.apache.pulsar.client.api.SubscriptionInitialPosition
import org.apache.pulsar.client.api.SubscriptionType
import org.jetbrains.exposed.sql.transactions.transaction
import ru.alex3koval.eventer.app.contract.EventHandler
import ru.alex3koval.eventer.app.contract.EventRegistry
import ru.alex3koval.eventer.app.vo.EventStatus
import ru.alex3koval.eventer.app.vo.EventTransportContainer
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.measureTimedValue

class EventRegistryFactory(
    private val pulsarClient: PulsarClient,
    private val logger: Logger,
    private val eventPersister: EventPersister
) {
    val mapConsumer = mutableMapOf<String, Consumer<String>>()

    private var handlers: MutableMap<String, MutableList<EventHandler>> = mutableMapOf()

    val scope = CoroutineScope(Dispatchers.IO)

    private fun Message<String>.process(): Result<Message<String>> = runCatching {
        val eventTC = Json.decodeFromString<EventTransportContainer>(this.value)

        handlers[eventTC.eventName]?.forEach { eh ->
            val event = Json.decodeFromString(eh.serializer as DeserializationStrategy<Any>, eventTC.jsonPayload)
            val handler = eh.handler as (Any) -> Result<Boolean>

            handler.invoke(event)
        }

        this
    }

    private fun addToMap(topic: String, consumerName: String, subscription: String) {
        mapConsumer[topic] = pulsarClient
            .newConsumer(Schema.STRING)
            .topic(topic)
            .subscriptionName(subscription)
            .consumerName(consumerName)
            .negativeAckRedeliveryDelay(20, TimeUnit.SECONDS)
            .subscriptionType(SubscriptionType.Key_Shared)
            .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
            .subscribe()
    }

    fun create(topic: String, consumerName: String, subscription: String): EventRegistry {
        if (!mapConsumer.containsKey(topic)) {
            addToMap(topic, consumerName, subscription)
        }

        return object : EventRegistry {
            override fun <T : Any> register(
                eventName: String,
                serializer: KSerializer<T>,
                handler: (T) -> Result<Boolean>
            ) {
                handlers[eventName]?.add(EventHandler(serializer, handler)) ?: handlers.put(
                    eventName,
                    mutableListOf(EventHandler(serializer, handler))
                )
            }

            override fun run() {
                mapConsumer.forEach { (topicName, _) ->
                    scope.launch {
                        while (true) {

                            if (!mapConsumer.containsKey(topicName)) {
                                throw Exception("Не найден topic: $topicName")
                            }

                            val consumer = mapConsumer[topicName]!!
                            val msgR = runCatching { consumer.receive(2, TimeUnit.SECONDS) }

                            if (msgR.isFailure) {
                                logger.error(msgR.exceptionOrNull().toString())
                                continue
                            }

                            val msg = msgR.getOrThrow() ?: continue

                            val hash = msg.getProperty("hash")
                            eventPersister.updateStatus(hash, EventStatus.IN_PROCESS, LocalDateTime.now()).getOrThrow()

                            try {
                                transaction {
                                    val result = measureTimedValue { msg.process() }.run {
                                        value
                                    }

                                    result
                                        .onSuccess {
                                            eventPersister.updateStatus(
                                                hash,
                                                EventStatus.PROCESSED,
                                                LocalDateTime.now()
                                            )
                                                .getOrThrow()
                                            consumer.acknowledge(msg)

                                            commit()
                                        }
                                        .onFailure { err ->
                                            logger.error("Error: ${err.message}")
                                            consumer.negativeAcknowledge(msg)
                                            rollback()
                                            eventPersister.updateStatus(hash, EventStatus.ERROR, LocalDateTime.now())
                                                .getOrThrow()
                                            logger.error("Ошибка при обработке $msg: ${err.message}")
                                        }
                                }
                            } catch (err: Exception) {
                                logger.error("Ошибка при обработке $msg: ${err.message}")
                            }
                        }
                    }
                }
            }

            override fun handlersForEvent(eventName: String): List<EventHandler> {
                return handlers[eventName] ?: emptyList()
            }
        }
    }
}
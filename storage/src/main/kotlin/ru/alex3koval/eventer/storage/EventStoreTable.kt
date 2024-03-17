package ru.alex3koval.eventer.storage

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object EventStoreTable : IntIdTable("event_store") {
    val aggregateId = varchar("aggregate_id", 255)
    val status = integer("status")
    val jsonPayload = jsonb<String>("payload")
    val hash = varchar("hash", 255)
    val type = varchar("type", 255)
    val producer = varchar("producer", 255)
    val comment = text("comment").nullable()
    val topic = varchar("topic", 255).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
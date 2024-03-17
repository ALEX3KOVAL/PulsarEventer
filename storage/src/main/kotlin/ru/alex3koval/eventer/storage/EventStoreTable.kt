package ru.alex3koval.eventer.storage

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object EventStoreTable : IntIdTable("event_store") {
    val status = integer("status")
    val payload = jsonb<String>("payload")
    val hash = varchar("hash", 255)
    val updatedAt = datetime("updated_at")
}
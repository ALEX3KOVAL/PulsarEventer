package ru.alex3koval.eventer.storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import ru.alex3koval.eventer.app.extensions.DateTimeAsStringSerializer

interface JsonConverter<T : Any> {
    fun stringify(value: T): String
    fun parse(value: String): T
}

/**
 * Базовый JSON конвертер
 */
inline fun <reified T : Any> standardJsonConverter(): JsonConverter<T> {
    return object : JsonConverter<T> {
        override fun stringify(value: T): String = when {
            value is String -> value
            else -> standardJsonConverter.encodeToString(value)
        }

        override fun parse(value: String): T = when {
            T::class == String::class -> value as T
            else -> standardJsonConverter.decodeFromString(value)
        }
    }
}

/**
 * Объект JSON для стандартного конвертера
 */
val standardJsonConverter = Json {
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        contextual(DateTimeAsStringSerializer)
    }
}

internal class JsonbColumnType<T : Any>(
    private val stringify: (T) -> String,
    private val parse: (String) -> T
) : ColumnType() {
    override fun sqlType() = JSONB

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        super.setParameter(
            stmt,
            index,
            value.let {
                PGobject().apply {
                    this.type = sqlType()
                    this.value = value as String?
                }
            }
        )
    }

    override fun valueFromDB(value: Any): Any {
        return if (value is PGobject) parse(value.value!!) else value
    }

    override fun valueToString(value: Any?): String = when (value) {
        is Iterable<*> -> nonNullValueToString(value)
        else -> super.valueToString(value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun notNullValueToDB(value: Any) = stringify(value as T)

    companion object {
        const val JSONB = "JSONB"
        const val TEXT = "TEXT"
    }
}

internal inline fun <reified T : Any> Table.jsonb(
    name: String,
    converter: JsonConverter<T> = standardJsonConverter()
): Column<T> =
    jsonb(name = name, stringify = converter::stringify, parse = converter::parse)

fun <T : Any> Table.jsonb(name: String, stringify: (T) -> String, parse: (String) -> T): Column<T> =
    registerColumn(name, JsonbColumnType(stringify, parse))
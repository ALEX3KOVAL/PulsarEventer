package ru.alex3koval.eventer.app.extensions

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeAsStringSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("timestamp", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) =
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val value = decoder.decodeString()
        return try {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.parse(value)
        }
    }
}
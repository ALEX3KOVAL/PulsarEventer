package ru.coderocket.sza.configure.settings

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
class SystemSettings internal constructor(
    val pulsar: Pulsar,
    val db: DB
) {
    companion object {
        fun read(): SystemSettings {
            val configFromFile =
                SystemSettings::class.java.getResourceAsStream("/global.yaml")!!.bufferedReader().use { it.readText() }

            return Yaml.default.decodeFromString<SystemSettings>(configFromFile)
        }
    }

    @Serializable
    data class Pulsar(val listener: String, val url: String, val topics: List<String>)

    @Serializable
    data class DB(val jdbcURL: String, val login: String, val password: String)
}
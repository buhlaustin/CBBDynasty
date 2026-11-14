package dev.novux.dynasty.cbb.core.scrimmage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ScrimmageGameRecordCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(record: ScrimmageGameRecord): String = json.encodeToString(record)

    fun decode(raw: String): ScrimmageGameRecord = json.decodeFromString(raw)
}

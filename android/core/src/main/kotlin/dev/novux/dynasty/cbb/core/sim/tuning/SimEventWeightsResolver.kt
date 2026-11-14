package dev.novux.dynasty.cbb.core.sim.tuning

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object SimEventWeightsResolver {
    const val QA_ID: String = "MEGA-SIM-099"

    val DEFAULT: SimEventWeights = SimEventWeights()

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun resolve(raw: kotlinx.serialization.json.JsonElement): SimEventWeights {
        if (raw !is JsonObject || raw.isEmpty()) return DEFAULT
        return try {
            json.decodeFromJsonElement(SimEventWeights.serializer(), raw)
        } catch (_: SerializationException) {
            DEFAULT
        } catch (_: IllegalArgumentException) {
            DEFAULT
        }
    }

    fun validate(weights: SimEventWeights): String? {
        val trip = weights.tripBuckets
        val sum = trip.quickTurnover + trip.quickShot + trip.foulStoppage
        if (sum <= 0.0) return "tripBuckets weights must sum > 0"
        if (weights.offensiveReboundRate !in 0.0..1.0) {
            return "offensiveReboundRate must be in [0,1]"
        }
        return null
    }
}

package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.FoulEarlyLateGamePath
import dev.novux.dynasty.cbb.core.sim.tuning.SimFoulEarlyWeights
import dev.novux.dynasty.cbb.core.strategy.LateGamePossessionPolicy
import kotlin.random.Random

/**
 * Foul-early / hack-a trip sampling when defense is trailing late (MEGA-SIM-114).
 */
object FoulEarlyLateGamePolicy {
    val QA_ID: String = FoulEarlyLateGamePath.SIM_ID

    enum class TripKind {
        Normal,
        IntentionalFoul,
        HackaQuickFoul,
    }

    fun rollTripKind(random: Random, weights: SimFoulEarlyWeights): TripKind {
        val roll = random.nextDouble()
        return when {
            roll < weights.intentionalFoulTripRate -> TripKind.IntentionalFoul
            roll < weights.intentionalFoulTripRate + weights.hackaQuickFoulTripRate -> TripKind.HackaQuickFoul
            else -> TripKind.Normal
        }
    }

    internal fun isFoulToStopClock(trip: PossessionSimEngine.TripSample): Boolean =
        trip.intentionalFoul || trip.foulEarlyHacka

    fun isActive(decision: LateGamePossessionPolicy.PossessionDecision): Boolean =
        decision == LateGamePossessionPolicy.PossessionDecision.IntentionalFoulTrip

    internal fun logTag(trip: PossessionSimEngine.TripSample): String? = when {
        trip.intentionalFoul -> "foul-early intentional"
        trip.foulEarlyHacka -> "foul-early hack-a"
        else -> null
    }

    internal fun foulLogLabel(
        trip: PossessionSimEngine.TripSample,
        personalFoulLine: String?,
    ): String? =
        when {
            trip.intentionalFoul ->
                IntentionalFoulLateGamePolicy.foulLogLabel(personalFoulLine)
            trip.foulEarlyHacka ->
                personalFoulLine?.let { "hack-a foul | $it" } ?: "hack-a foul"
            else -> null
        }
}

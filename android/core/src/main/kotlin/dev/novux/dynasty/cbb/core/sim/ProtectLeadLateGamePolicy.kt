package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.ProtectLeadLateGamePath
import dev.novux.dynasty.cbb.core.sim.tuning.SimProtectLeadWeights
import dev.novux.dynasty.cbb.core.strategy.LateGamePossessionPolicy
import kotlin.random.Random

/**
 * Protect-lead late game: stretch pace, avoid foul stops and 3PT shooting fouls (MEGA-SIM-113).
 */
object ProtectLeadLateGamePolicy {
    val QA_ID: String = ProtectLeadLateGamePath.SIM_ID

    fun isActive(decision: LateGamePossessionPolicy.PossessionDecision): Boolean =
        decision == LateGamePossessionPolicy.PossessionDecision.ProtectLeadPace

    fun adjustRawTripSeconds(rawSeconds: Int, weights: SimProtectLeadWeights): Int =
        (rawSeconds * weights.paceTripScale)
            .toInt()
            .coerceIn(GameSimClock.MIN_TRIP_SECONDS, GameSimClock.MAX_TRIP_SECONDS)

    fun shouldAvoidFoulStoppage(random: Random, weights: SimProtectLeadWeights): Boolean =
        random.nextDouble() < weights.avoidFoulStoppageRate

    fun shootingFoulRateMultiplier(threePointAttempt: Boolean, weights: SimProtectLeadWeights): Double =
        if (threePointAttempt) {
            weights.threePointShootingFoulMultiplier
        } else {
            weights.twoPointShootingFoulMultiplier
        }

    fun logTag(): String = "protect-lead pace"
}

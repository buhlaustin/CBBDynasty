package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimEventWeightsResolver
import kotlin.random.Random

/**
 * Shooting foul on FGA → 1–3 free throws ([planning/simulation-engine.md#what-can-happen-on-a-possession], MEGA-SIM-094).
 */
object PossessionShootingFoulPolicy {
    const val QA_ID: String = "MEGA-SIM-094"

    fun rollAndOne(
        lateClock: Boolean,
        random: Random,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
        rateMultiplier: Double = 1.0,
    ): Boolean {
        val sf = eventWeights.shootingFoul
        val rate = sf.andOneRate * if (lateClock) sf.lateClockMultiplier else 1.0
        return random.nextDouble() < rate * rateMultiplier
    }

    fun rollMissShootingFoul(
        lateClock: Boolean,
        random: Random,
        eventWeights: SimEventWeights = SimEventWeightsResolver.DEFAULT,
        rateMultiplier: Double = 1.0,
    ): Boolean {
        val sf = eventWeights.shootingFoul
        val rate = sf.missFoulRate * if (lateClock) sf.lateClockMultiplier else 1.0
        return random.nextDouble() < rate * rateMultiplier
    }

    fun attemptsForMissFoul(threePointAttempt: Boolean): Int = if (threePointAttempt) 3 else 2

    fun chainSuffix(andOne: Boolean, attempts: Int): String =
        when {
            andOne -> "and-1 | FT×$attempts"
            attempts > 0 -> "shooting foul | FT×$attempts"
            else -> ""
        }
}

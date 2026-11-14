package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import kotlin.math.min

/** Shot-clock carry and resets ([planning/simulation-engine.md#possession-loop--clock], MEGA-SIM-086). */
object PossessionShotClock {
    const val DEFAULT_OFFENSIVE_REBOUND_KEEP_RATE: Double = 0.28

    fun resetOnChangeOfPossession(clock: SimShotClock): Int = clock.full

    fun resetAfterOffensiveRebound(clock: SimShotClock): Int = clock.offensiveRebound

    fun secondsConsumed(sampledTrip: Int, shotClockRemaining: Int, gameClockRemaining: Int): Int =
        min(sampledTrip, min(shotClockRemaining, gameClockRemaining)).coerceAtLeast(0)

    fun remainingAfterTrip(shotClockBefore: Int, used: Int): Int =
        (shotClockBefore - used).coerceAtLeast(0)

    /**
     * Clock exhausted on this trip before a normal terminal event — shot-clock violation turnover
     * ([planning/simulation-engine.md], MEGA-SIM-087).
     */
    fun isShotClockViolation(shotClockBefore: Int, used: Int, sampledTrip: Int): Boolean =
        shotClockBefore > 0 &&
            used >= shotClockBefore &&
            sampledTrip > shotClockBefore &&
            remainingAfterTrip(shotClockBefore, used) == 0
}

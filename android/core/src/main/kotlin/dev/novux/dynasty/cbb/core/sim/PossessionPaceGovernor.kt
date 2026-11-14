package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning

/** Keeps regulation possession totals near `possessionsPerGameTarget` ([MEGA-SIM-177]). */
object PossessionPaceGovernor {
    fun tripPaceMultiplier(
        totalPossessionsSoFar: Int,
        gameClockRemaining: Int,
        tuning: SimTuning,
    ): Double {
        val targetPerTeam =
            (tuning.possessionsPerGameTarget.min + tuning.possessionsPerGameTarget.max) / 2.0
        val elapsed = GameSimClock.REGULATION_SECONDS - gameClockRemaining
        if (elapsed <= 0) return 1.0
        val progress = elapsed.toDouble() / GameSimClock.REGULATION_SECONDS.toDouble()
        val expectedPossessions = targetPerTeam * 2.0 * progress
        val delta = totalPossessionsSoFar - expectedPossessions
        return when {
            delta > 6.0 -> 1.06
            delta > 3.0 -> 1.03
            delta < -6.0 -> 0.94
            delta < -3.0 -> 0.97
            else -> 1.0
        }
    }
}

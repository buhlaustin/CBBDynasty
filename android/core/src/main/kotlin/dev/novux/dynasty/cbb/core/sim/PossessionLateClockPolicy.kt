package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects

/**
 * Shot-clock stress when a trip consumes **22–30s** of the 30s clock
 * ([planning/simulation-engine.md#why-track-a-shot-clock], MEGA-SIM-088).
 */
object PossessionLateClockPolicy {
    const val QA_ID: String = "MEGA-SIM-088"

    const val MIN_USED_SECONDS: Int = 22
    const val MAX_USED_SECONDS: Int = 30

    const val TURNOVER_BUMP: Double = 0.08
    const val MAKE_RATE_PENALTY: Double = 0.06
    const val EXTRA_FOUL_STOPPAGE_RATE: Double = 0.14

    fun isLateClockTrip(usedSeconds: Int, shotClockFull: Int = 30): Boolean {
        val cap = minOf(MAX_USED_SECONDS, shotClockFull)
        return usedSeconds in MIN_USED_SECONDS..cap
    }

    fun applyToEffects(effects: ResolvedSchemeEffects): ResolvedSchemeEffects =
        effects.copy(
            turnoverChance = (effects.turnoverChance + TURNOVER_BUMP).coerceIn(0.05, 0.45),
            makeRateAdjust = effects.makeRateAdjust - MAKE_RATE_PENALTY,
        )
}

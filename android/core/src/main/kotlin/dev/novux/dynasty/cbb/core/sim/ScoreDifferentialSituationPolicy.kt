package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.ScoreDifferentialSituationPath
import dev.novux.dynasty.cbb.core.sim.tuning.SimSituationWeights
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects

/**
 * Late-game score margin nudges shot mix and foul pressure ([planning/simulation-engine.md], MEGA-SIM-111).
 */
object ScoreDifferentialSituationPolicy {
    val QA_ID: String = ScoreDifferentialSituationPath.SIM_ID

    enum class Situation {
        Neutral,
        CloseLate,
        TrailingLate,
        LeadingLate,
    }

    fun classify(
        gameClockSeconds: Int,
        offenseScore: Int,
        defenseScore: Int,
        weights: SimSituationWeights,
    ): Situation {
        if (gameClockSeconds > weights.lateWindowSeconds) return Situation.Neutral
        val margin = offenseScore - defenseScore
        return when {
            margin <= -weights.closeMarginPoints -> Situation.TrailingLate
            margin >= weights.closeMarginPoints -> Situation.LeadingLate
            else -> Situation.CloseLate
        }
    }

    fun applyToEffects(
        effects: ResolvedSchemeEffects,
        situation: Situation,
        weights: SimSituationWeights,
    ): ResolvedSchemeEffects =
        when (situation) {
            Situation.Neutral -> effects
            Situation.TrailingLate -> effects.copy(
                threePointAttemptRate = (effects.threePointAttemptRate + weights.trailingThreePointBoost)
                    .coerceIn(0.0, 0.58),
                turnoverChance = (effects.turnoverChance + weights.trailingTurnoverBoost)
                    .coerceIn(0.10, 0.40),
                makeRateAdjust = effects.makeRateAdjust - weights.trailingMakeRatePenalty,
            )
            Situation.LeadingLate -> effects.copy(
                threePointAttemptRate = (effects.threePointAttemptRate - weights.leadingThreePointPenalty)
                    .coerceIn(0.0, 0.58),
                turnoverChance = (effects.turnoverChance - weights.leadingTurnoverReduction)
                    .coerceIn(0.10, 0.40),
                tripPaceScale = (effects.tripPaceScale * weights.leadingPaceScale).coerceIn(0.82, 1.18),
            )
            Situation.CloseLate -> effects.copy(
                makeRateAdjust = effects.makeRateAdjust + weights.closeMakeRateBoost,
                threePointAttemptRate = (effects.threePointAttemptRate + weights.closeThreePointNudge)
                    .coerceIn(0.0, 0.58),
            )
        }

    fun trailingDefenseFoulNudge(
        gameClockSeconds: Int,
        defenseScore: Int,
        offenseScore: Int,
        weights: SimSituationWeights,
    ): Double {
        val situation = classify(
            gameClockSeconds,
            defenseScore,
            offenseScore,
            weights,
        )
        return if (situation == Situation.TrailingLate) weights.trailingDefenseFoulNudge else 0.0
    }

    fun logTag(situation: Situation): String? = when (situation) {
        Situation.TrailingLate -> "situation trailing"
        Situation.LeadingLate -> "situation leading"
        Situation.CloseLate -> "situation close"
        Situation.Neutral -> null
    }
}

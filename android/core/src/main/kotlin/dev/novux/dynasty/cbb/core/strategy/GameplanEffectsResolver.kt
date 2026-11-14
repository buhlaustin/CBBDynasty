package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.sim.GameSimClock

data class GameplanPossessionTweaks(
    val makeRateAdjust: Double = 0.0,
    val turnoverAdjust: Double = 0.0,
    val tripPaceScaleAdjust: Double = 1.0,
)

/**
 * Merges usage, deny-primary, and late foul knobs onto scheme effects.
 */
object GameplanEffectsResolver {
    fun offenseTweaks(
        gameplan: GameplanInput?,
        activeUsageShare: Double,
    ): GameplanPossessionTweaks {
        if (gameplan == null) return GameplanPossessionTweaks()
        val concentration = activeUsageShare.coerceIn(0.0, 1.0)
        val usageMakeBoost = (concentration - 0.20).coerceAtLeast(0.0) * 0.08
        return GameplanPossessionTweaks(makeRateAdjust = usageMakeBoost)
    }

    fun defenseTweaks(
        gameplan: GameplanInput?,
        gameClockSeconds: Int,
        defenseScore: Int,
        offenseScore: Int,
    ): GameplanPossessionTweaks {
        if (gameplan == null) return GameplanPossessionTweaks()
        var turnover = 0.0
        var makePenalty = 0.0
        var pace = 1.0
        if (gameplan.denyPrimaryScorer) {
            turnover += 0.025
            makePenalty += 0.02
            if (gameplan.defensiveAssignments.containsKey(gameplan.primaryScorerSlot)) {
                turnover += 0.01
            }
        }
        val lateWindow = gameClockSeconds <= 2 * 60
        if (lateWindow) {
            when (gameplan.lateGameFoulStrategy) {
                LateGameFoulStrategy.FoulEarly -> if (defenseScore < offenseScore) {
                    turnover += 0.02
                    makePenalty += 0.03
                    pace = 0.94
                }
                LateGameFoulStrategy.ProtectLead -> if (defenseScore > offenseScore) {
                    makePenalty += 0.025
                    turnover -= 0.01
                }
                LateGameFoulStrategy.Standard -> Unit
            }
        }
        return GameplanPossessionTweaks(
            makeRateAdjust = -makePenalty,
            turnoverAdjust = turnover,
            tripPaceScaleAdjust = pace,
        )
    }

    fun pickUsageShare(gameplan: GameplanInput?, playerId: String?): Double {
        if (gameplan == null || playerId == null) return 0.2
        val normalized = GameplanUsageNormalizer.normalize(gameplan.usageWeights)
        return normalized[playerId] ?: (1.0 / normalized.size.coerceAtLeast(1))
    }

    fun isSecondHalf(gameClockSeconds: Int): Boolean = gameClockSeconds <= GameSimClock.HALF_SECONDS

    fun mergeInto(
        base: ResolvedSchemeEffects,
        offense: GameplanPossessionTweaks,
        defense: GameplanPossessionTweaks,
    ): ResolvedSchemeEffects = base.copy(
        makeRateAdjust = base.makeRateAdjust + offense.makeRateAdjust + defense.makeRateAdjust,
        turnoverChance = (base.turnoverChance + offense.turnoverAdjust + defense.turnoverAdjust)
            .coerceIn(0.10, 0.40),
        tripPaceScale = (base.tripPaceScale * defense.tripPaceScaleAdjust).coerceIn(0.82, 1.18),
    )
}

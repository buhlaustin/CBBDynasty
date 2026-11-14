package dev.novux.dynasty.cbb.core.sim.tuning

import kotlinx.serialization.Serializable

/** Tuning tables consumed each possession ([ADR-003](../docs/ADR-003-sim-tuning-bundle.md), MEGA-SIM-099). */
@Serializable
data class SimTripBucketWeights(
    val quickTurnover: Double = 1.0 / 3.0,
    val quickShot: Double = 1.0 / 3.0,
    val foulStoppage: Double = 1.0 / 3.0,
)

@Serializable
data class SimRatingGapMakeRate(
    val base: Double = 0.42,
    val perPointEdge: Double = 0.004,
    val min: Double = 0.25,
    val max: Double = 0.65,
    val kickLookBonus: Double = 0.025,
)

@Serializable
data class SimRatingGapBlockOnMiss(
    val base: Double = 0.08,
    val perDefEdge: Double = 0.002,
    val min: Double = 0.03,
    val max: Double = 0.18,
    val foulOnBlock: Double = 0.15,
)

@Serializable
data class SimShootingFoulWeights(
    val andOneRate: Double = 0.045,
    val missFoulRate: Double = 0.062,
    val lateClockMultiplier: Double = 1.35,
)

@Serializable
data class SimTurnoverSubPhaseWeights(
    val driveShare: Double = 0.28,
    val kickShare: Double = 0.22,
)

/** Made basket / FT at horn ([planning/simulation-engine.md#possession-loop--clock], MEGA-SIM-100). */
@Serializable
data class SimDenyPrimaryWeights(
    val assignedSlotTurnoverBoost: Double = 0.018,
    val denyFoulOnPrimaryTouchRate: Double = 0.085,
    val denyFoulOnTurnoverRate: Double = 0.065,
)

@Serializable
data class SimClutchUsageWeights(
    val lateWindowSeconds: Int = 4 * 60,
    val closeMarginPoints: Int = 5,
    val peakPlayerUsageMultiplier: Double = 1.35,
)

@Serializable
data class SimFoulEarlyWeights(
    val intentionalFoulTripRate: Double = 0.40,
    val hackaQuickFoulTripRate: Double = 0.30,
    val hackaTripMaxSeconds: Int = 6,
)

@Serializable
data class SimProtectLeadWeights(
    val paceTripScale: Double = 1.12,
    val avoidFoulStoppageRate: Double = 0.65,
    val threePointShootingFoulMultiplier: Double = 0.42,
    val twoPointShootingFoulMultiplier: Double = 0.78,
    val andOneFoulMultiplier: Double = 0.70,
)

@Serializable
data class SimSituationWeights(
    val lateWindowSeconds: Int = 4 * 60,
    val closeMarginPoints: Int = 3,
    val trailingThreePointBoost: Double = 0.10,
    val trailingTurnoverBoost: Double = 0.02,
    val trailingMakeRatePenalty: Double = 0.02,
    val leadingThreePointPenalty: Double = 0.08,
    val leadingTurnoverReduction: Double = 0.015,
    val leadingPaceScale: Double = 0.97,
    val closeMakeRateBoost: Double = 0.015,
    val closeThreePointNudge: Double = 0.03,
    val trailingDefenseFoulNudge: Double = 0.10,
)

@Serializable
data class SimPeriodEndRules(
    val countMadeBasketAtPeriodEnd: Boolean = true,
    val countFreeThrowsAtPeriodEnd: Boolean = true,
)

@Serializable
data class SimEventWeights(
    val tripBuckets: SimTripBucketWeights = SimTripBucketWeights(),
    val ratingGapMakeRate: SimRatingGapMakeRate = SimRatingGapMakeRate(),
    val ratingGapBlockOnMiss: SimRatingGapBlockOnMiss = SimRatingGapBlockOnMiss(),
    val offensiveReboundRate: Double = 0.28,
    val shootingFoul: SimShootingFoulWeights = SimShootingFoulWeights(),
    val turnoverSubPhase: SimTurnoverSubPhaseWeights = SimTurnoverSubPhaseWeights(),
    val periodEnd: SimPeriodEndRules = SimPeriodEndRules(),
    val situation: SimSituationWeights = SimSituationWeights(),
    val protectLead: SimProtectLeadWeights = SimProtectLeadWeights(),
    val foulEarly: SimFoulEarlyWeights = SimFoulEarlyWeights(),
    val clutchUsage: SimClutchUsageWeights = SimClutchUsageWeights(),
    val denyPrimary: SimDenyPrimaryWeights = SimDenyPrimaryWeights(),
) {
    fun makeRateForEdge(edge: Int): Double =
        (
            ratingGapMakeRate.base +
                edge * ratingGapMakeRate.perPointEdge
            ).coerceIn(ratingGapMakeRate.min, ratingGapMakeRate.max)

    fun blockRateForEdge(defEdge: Int): Double =
        (
            ratingGapBlockOnMiss.base +
                defEdge * ratingGapBlockOnMiss.perDefEdge
            ).coerceIn(ratingGapBlockOnMiss.min, ratingGapBlockOnMiss.max)
}

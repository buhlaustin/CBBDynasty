package dev.novux.dynasty.cbb.core.sim.tuning

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** NCAA men’s team-foul table for automatic bonus trips ([MEGA-SIM-095]). */
@Serializable
data class SimTeamFoulRules(
    val bonusThreshold: Int = 7,
    val doubleBonusThreshold: Int = 10,
    /** Fouls 7–9: one-and-one when true; at **doubleBonus** always two shots. */
    val oneAndOneUntilDouble: Boolean = true,
)

@Serializable
data class SimTuning(
    val simTuningId: String,
    val schemaVersion: Int,
    val possessionsPerGameTarget: IntMinMax,
    val shotClock: SimShotClock,
    val timeUsedSeconds: SimTimeUsedSeconds,
    val eventWeights: JsonElement,
    val homeNeutralModifiers: SimVenueModifiers,
    val teamFoulRules: SimTeamFoulRules = SimTeamFoulRules(),
    val positionComfort: SimPositionComfortModifiers = SimPositionComfortModifiers(),
)

@Serializable
data class SimPositionComfortModifiers(
    val eligibleInterchangePenalty: Int = 1,
    val ineligibleSlotPenalty: Int = 3,
)

@Serializable
data class IntMinMax(
    val min: Int,
    val max: Int,
)

@Serializable
data class SimShotClock(
    val full: Int,
    val offensiveRebound: Int,
)

@Serializable
data class SimTimeUsedSeconds(
    val quickTurnover: List<Int>,
    val quickShot: List<Int>,
    val foulStoppage: List<Int>,
)

@Serializable
data class SimVenueModifiers(
    val home: Double,
    val neutral: Double,
    val road: Double,
)

package dev.novux.dynasty.cbb.core.strategy

/** One O×D cell in the 7×7 scheme interaction matrix ([MEGA-SIM-146]). */
data class SchemeInteractionNudge(
    val threeAdjust: Double = 0.0,
    val turnoverAdjust: Double = 0.0,
    val makeAdjust: Double = 0.0,
    val tripPaceScale: Double = 1.0,
    val offRatingDelta: Int = 0,
    val defRatingDelta: Int = 0,
)

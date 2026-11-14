package dev.novux.dynasty.cbb.core.recruiting

/** UI helpers for the live interest meter 0–100 ([planning/recruiting-engine.md#two-surfaces]). */
object RecruitInterestMeterDisplay {
    const val METER_MAX: Int = 100

    fun clampedLiveInterest(liveInterest: Int): Int = liveInterest.coerceIn(0, METER_MAX)

    fun progressFraction(liveInterest: Int): Float = clampedLiveInterest(liveInterest) / METER_MAX.toFloat()

    fun spendGainPoints(baselineInterest: Int, liveInterest: Int): Int =
        (clampedLiveInterest(liveInterest) - clampedLiveInterest(baselineInterest)).coerceAtLeast(0)
}

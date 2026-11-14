package dev.novux.dynasty.cbb.core.sim

/**
 * User-selectable watch pacing per [planning/strategy-schemes.md] watch timing.
 */
object GameWatchSpeedPresets {
    val SPEED_MULTIPLIERS: List<Double> = listOf(0.5, 1.0, 2.0, 4.0)

    fun isUserMultiplier(value: Double): Boolean =
        SPEED_MULTIPLIERS.any { kotlin.math.abs(it - value) < 0.001 }
}

package dev.novux.dynasty.cbb.core.fatigue

import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset

/** Press scheme + back-to-back fatigue extras ([MEGA-SHELL-083]). */
object FatiguePressBackToBackPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-083"

    const val PRESS_EXTRA_DRAIN_PER_MINUTE: Double = 0.12
    const val BACK_TO_BACK_EXTRA_DRAIN: Int = 8

    fun pressDrainPerMinute(@Suppress("UNUSED_PARAMETER") offensiveScheme: OffensiveSchemePreset): Double = 0.0

    fun defensivePressDrainPerMinute(defensiveScheme: DefensiveSchemePreset): Double = when (defensiveScheme) {
        DefensiveSchemePreset.FullCourtPress -> PRESS_EXTRA_DRAIN_PER_MINUTE
        else -> 0.0
    }

    fun backToBackExtraDrain(userGamesThisWeek: Int): Int =
        if (userGamesThisWeek >= 2) BACK_TO_BACK_EXTRA_DRAIN else 0

    fun adjustedEstimatedDrain(
        baseDrain: Int,
        offensiveScheme: OffensiveSchemePreset = OffensiveSchemePreset.Balanced,
        defensiveScheme: DefensiveSchemePreset = DefensiveSchemePreset.ManToMan,
        userGamesThisWeek: Int = 1,
        estimatedMinutes: Double,
    ): Int {
        val pressExtra = (
            (pressDrainPerMinute(offensiveScheme) + defensivePressDrainPerMinute(defensiveScheme)) *
                estimatedMinutes
            ).toInt()
        return baseDrain + pressExtra + backToBackExtraDrain(userGamesThisWeek)
    }
}
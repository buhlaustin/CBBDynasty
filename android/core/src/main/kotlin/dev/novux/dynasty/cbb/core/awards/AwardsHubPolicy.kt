package dev.novux.dynasty.cbb.core.awards

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** Awards hub visibility rules ([planning/awards.md#awards-hub-ui-design-lock]). */
object AwardsHubPolicy {
    const val SHELL_MENU_MEGA_ID = "MEGA-FRAME-145"
    private val yearlyLockedPhases = setOf(
        CalendarPhase.Offseason,
        CalendarPhase.Preseason,
        CalendarPhase.Exhibition,
        CalendarPhase.RegularSeason,
        CalendarPhase.ConferenceTournament,
        CalendarPhase.Postseason,
    )

    fun yearlyCurrentSeasonLocked(phase: CalendarPhase): Boolean = phase in yearlyLockedPhases

    fun weeklyAwardsLive(phase: CalendarPhase): Boolean = phase == CalendarPhase.RegularSeason
}

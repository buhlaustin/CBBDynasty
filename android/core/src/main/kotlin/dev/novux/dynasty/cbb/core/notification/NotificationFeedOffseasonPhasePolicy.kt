package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** Notification feed is composed on offseason dashboard, not in-season shell only ([MEGA-SHELL-056]). */
object NotificationFeedOffseasonPhasePolicy {
    const val MEGA_ID: String = "MEGA-SHELL-056"

    private val IN_SEASON = setOf(
        CalendarPhase.Exhibition,
        CalendarPhase.RegularSeason,
        CalendarPhase.ConferenceTournament,
        CalendarPhase.Postseason,
    )

    fun showsOnDashboard(phase: CalendarPhase): Boolean =
        phase == CalendarPhase.Offseason || phase == CalendarPhase.Preseason || phase in IN_SEASON
}
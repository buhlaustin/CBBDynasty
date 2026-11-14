package dev.novux.dynasty.cbb.core.shell

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/** Staff + Automation hamburger extras appear only on the live in-season shell ([MEGA-SHELL-093]). */
object InSeasonShellMenuExtrasPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-093"

    fun includeStaffAndAutomation(phase: CalendarPhase): Boolean =
        when (phase) {
            CalendarPhase.Exhibition,
            CalendarPhase.RegularSeason,
            CalendarPhase.ConferenceTournament,
            CalendarPhase.Postseason,
            -> true
            CalendarPhase.Offseason,
            CalendarPhase.Preseason,
            CalendarPhase.SeasonReview,
            -> false
        }
}
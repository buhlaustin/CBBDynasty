package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear

/**
 * Player-facing week headers — week index is authoritative ([planning/time-system.md]).
 */
object CalendarWeekLabel {
    fun display(
        position: CalendarPosition,
        leagueMaxConfTourneyWeeks: Int? = null,
    ): String =
        when (position.phase) {
            CalendarPhase.Offseason -> "Offseason Week ${position.weekIndex}"
            CalendarPhase.Preseason -> "Preseason — setup week"
            CalendarPhase.Exhibition -> "Exhibition Week ${position.weekIndex}"
            CalendarPhase.RegularSeason -> "Week ${position.weekIndex}"
            CalendarPhase.ConferenceTournament -> conferenceTournamentLabel(position.weekIndex, leagueMaxConfTourneyWeeks)
            CalendarPhase.Postseason -> PostseasonCalendar.displayWeekHeader(position.weekIndex)
            CalendarPhase.SeasonReview -> "End of Season — ${SeasonMembershipYear.label(position.seasonIndex)}"
        }

    private fun conferenceTournamentLabel(weekIndex: Int, leagueMax: Int?): String {
        val max = leagueMax?.takeIf { it > 1 }
        return if (max != null) {
            "Conference Tournament Week $weekIndex of $max"
        } else {
            "Conference Tournament Week $weekIndex"
        }
    }
}

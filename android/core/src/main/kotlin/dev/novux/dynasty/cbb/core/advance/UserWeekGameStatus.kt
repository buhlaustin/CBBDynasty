package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState

object UserWeekGameStatus {
    fun unplayedUserGames(
        state: WeekCheckpointState,
        teamHasScheduledGame: Boolean,
    ): Int {
        if (!teamHasScheduledGame) return 0
        if (!phaseMayHaveUserGames(state.activePosition.phase)) return 0
        return if (state.provisionalGames.isEmpty()) 1 else 0
    }

    private fun phaseMayHaveUserGames(phase: CalendarPhase): Boolean =
        phase in setOf(
            CalendarPhase.Exhibition,
            CalendarPhase.RegularSeason,
            CalendarPhase.ConferenceTournament,
            CalendarPhase.Postseason,
        )
}

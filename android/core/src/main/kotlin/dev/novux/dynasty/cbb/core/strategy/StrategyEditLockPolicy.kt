package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.calendar.isAtOrBefore

/**
 * Strategy prep applies to the active week's open games — locked when the week has no schedulable
 * game or the calendar has not rolled past the last committed week ([planning/strategy-schemes.md]).
 */
object StrategyEditLockPolicy {
    fun prepEditable(
        checkpoint: WeekCheckpointState,
        hasScheduledUserGame: Boolean,
        phase: CalendarPhase,
    ): Boolean {
        if (!hasScheduledUserGame) return false
        if (!phaseAllowsPrep(phase)) return false
        val committed = checkpoint.committedThrough ?: return true
        return !checkpoint.activePosition.isAtOrBefore(committed)
    }

    private fun phaseAllowsPrep(phase: CalendarPhase): Boolean =
        phase in setOf(
            CalendarPhase.Exhibition,
            CalendarPhase.RegularSeason,
            CalendarPhase.ConferenceTournament,
            CalendarPhase.Postseason,
        )
}

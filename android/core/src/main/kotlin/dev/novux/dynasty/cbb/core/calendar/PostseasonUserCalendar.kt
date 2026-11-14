package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.selection.NcbaBidType
import dev.novux.dynasty.cbb.core.selection.SelectionSundaySnapshot

/**
 * User-facing postseason calendar semantics ([planning/time-system.md#national-postseason-weeks]).
 */
object PostseasonUserCalendar {
    /** Top 52 (+ champs / direct at-large) skip NCBA Opening Round — week 1 is a bye on the user calendar. */
    fun skipsNcbaOpeningRound(
        selection: SelectionSundaySnapshot?,
        userTeamId: String,
    ): Boolean {
        val entry = selection?.ncbaField?.firstOrNull { it.teamId == userTeamId } ?: return false
        return entry.bidType != NcbaBidType.OpeningRound
    }
}
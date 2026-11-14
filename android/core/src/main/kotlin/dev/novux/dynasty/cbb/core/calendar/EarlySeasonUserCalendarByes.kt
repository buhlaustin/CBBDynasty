package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonBracketEngine
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonBracketSeason
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonWeekSlot

/** Maps RS weeks → event display name when the user is on an elimination bye ([planning/time-system.md#byes-after-elimination]). */
object EarlySeasonUserCalendarByes {
    fun eliminationByeWeekLabels(
        season: EarlySeasonBracketSeason?,
        userTeamId: String,
    ): Map<Int, String> {
        if (season == null) return emptyMap()
        val result = linkedMapOf<Int, String>()
        for (snapshot in season.brackets.values) {
            if (!snapshot.isParticipant(userTeamId)) continue
            val event = snapshot.event ?: continue
            val display = event.displayName
            for (week in event.rsWeekIndices) {
                if (EarlySeasonBracketEngine.teamWeekSlot(snapshot, userTeamId, week) == EarlySeasonWeekSlot.EliminationBye) {
                    result[week] = display
                }
            }
        }
        return result
    }
}
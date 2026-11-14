package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserCalendarSchedulePolicyTest {
    private fun entry(kind: ScheduleGameKind) = UserCalendarGameEntry(
        opponentTeamId = "opp-1",
        opponentDisplayName = "Opp",
        homeGame = true,
        gameKind = kind,
        isPastWeek = false,
        gameDateLabel = "Jan 1",
        gamePosition = CalendarPosition(1, CalendarPhase.RegularSeason, 2),
    )

    @Test
    fun preservesPlanOrder() {
        val games = listOf(
            entry(ScheduleGameKind.NonConference),
            entry(ScheduleGameKind.Conference),
        )
        assertEquals(games, UserCalendarSchedulePolicy.gamesInChronologicalPlanOrder(games))
    }

    @Test
    fun sectionBreakOnKindChange() {
        val games = listOf(
            entry(ScheduleGameKind.NonConference),
            entry(ScheduleGameKind.Conference),
        )
        assertNull(UserCalendarSchedulePolicy.sectionBreakBefore(0, games))
        assertEquals(ScheduleGameKind.Conference, UserCalendarSchedulePolicy.sectionBreakBefore(1, games))
    }
}

package dev.novux.dynasty.cbb.core.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserCalendarScheduleDensityPolicyTest {
    @Test
    fun countsGamesOnCurrentWeek() {
        val rows = listOf(
            UserCalendarWeekRow(
                weekLabel = "W1",
                phase = CalendarPhase.RegularSeason,
                weekIndex = 1,
                rowKind = UserCalendarRowKind.Game,
                games = listOf(
                    gameStub(),
                    gameStub(),
                ),
                isCurrentWeek = false,
            ),
            UserCalendarWeekRow(
                weekLabel = "W2",
                phase = CalendarPhase.RegularSeason,
                weekIndex = 2,
                rowKind = UserCalendarRowKind.Game,
                games = listOf(gameStub()),
                isCurrentWeek = true,
            ),
        )
        val current = UserCalendarScheduleDensityPolicy.currentWeekRow(rows)
        requireNotNull(current)
        assertEquals(1, UserCalendarScheduleDensityPolicy.gamesScheduledInWeek(current))
    }

    @Test
    fun noCurrentWeekReturnsNull() {
        val rows = listOf(
            UserCalendarWeekRow(
                weekLabel = "W1",
                phase = CalendarPhase.RegularSeason,
                weekIndex = 1,
                rowKind = UserCalendarRowKind.OffWeek,
                games = emptyList(),
                isCurrentWeek = false,
            ),
        )
        assertNull(UserCalendarScheduleDensityPolicy.currentWeekRow(rows))
    }

    private fun gameStub(): UserCalendarGameEntry {
        val position = CalendarPosition(1, CalendarPhase.RegularSeason, 1)
        return UserCalendarGameEntry(
            opponentTeamId = "t-opp",
            opponentDisplayName = "Opp",
            homeGame = true,
            gameKind = dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind.NonConference,
            isPastWeek = false,
            gameDateLabel = "Jan 1",
            gamePosition = position,
        )
    }
}
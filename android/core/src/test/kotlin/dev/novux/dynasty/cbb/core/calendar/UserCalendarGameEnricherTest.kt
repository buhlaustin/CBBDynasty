package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.advance.UserLeagueGameIds
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserCalendarGameEnricherTest {
  @Test
  fun provisionalMarkScoresOnCurrentWeek() {
        val position = CalendarPosition(1, CalendarPhase.RegularSeason, 5)
        val game = UserCalendarGameEntry(
            opponentTeamId = "unc",
            opponentDisplayName = "UNC",
            homeGame = true,
            gameKind = ScheduleGameKind.NonConference,
            isPastWeek = false,
            gameDateLabel = "Jan 2",
            gamePosition = position,
        )
        val row = UserCalendarWeekRow(
            weekLabel = "Week 5",
            phase = CalendarPhase.RegularSeason,
            weekIndex = 5,
            rowKind = UserCalendarRowKind.Game,
            games = listOf(game),
            isCurrentWeek = true,
        )
        val gameId = UserLeagueGameIds.forPosition(position, 0)
        val enriched = UserCalendarGameEnricher.enrichRows(
            rows = listOf(row),
            userTeamId = "duke",
            activePosition = position,
            standings = null,
            provisionalGames = mapOf(gameId to ProvisionalGameMark(gameId, 80, 72)),
            lastGame = null,
        ).single()
        assertEquals(80, enriched.games.single().playedResult?.userScore)
    }

    @Test
    fun standingsFillPastWeek() {
        val position = CalendarPosition(1, CalendarPhase.RegularSeason, 3)
        val game = UserCalendarGameEntry(
            opponentTeamId = "unc",
            opponentDisplayName = "UNC",
            homeGame = true,
            gameKind = ScheduleGameKind.Conference,
            isPastWeek = true,
            gameDateLabel = "Dec 3",
            gamePosition = position,
        )
        val row = UserCalendarWeekRow(
            weekLabel = "Week 3",
            phase = CalendarPhase.RegularSeason,
            weekIndex = 3,
            rowKind = UserCalendarRowKind.Game,
            games = listOf(game),
            isCurrentWeek = false,
        )
        val standings = LeagueStandingsState(
            seasonIndex = 1,
            seasonResults = listOf(
                LeagueGameResult("duke", "unc", 70, 65, rsWeekIndex = 3),
            ),
        )
        val enriched = UserCalendarGameEnricher.enrichRows(
            rows = listOf(row),
            userTeamId = "duke",
            activePosition = CalendarPosition(1, CalendarPhase.RegularSeason, 8),
            standings = standings,
            provisionalGames = emptyMap(),
            lastGame = null,
        ).single()
        assertNotNull(enriched.games.single().playedResult)
        assertEquals(70, enriched.games.single().playedResult?.userScore)
    }
}

package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class DashboardLastUserGameResolverTest {
    @Test
    fun fromStandings_picksLatestUserGame() {
        val user = "team-user"
        val standings = LeagueStandingsState(
            seasonIndex = 2,
            seasonResults = listOf(
                LeagueGameResult(user, "opp-a", 70, 65, rsWeekIndex = 3),
                LeagueGameResult("opp-b", user, 80, 75, rsWeekIndex = 5),
            ),
        )
        val summary = DashboardLastUserGameResolver.fromStandings(
            userTeamId = user,
            userTeamDisplayName = "State",
            standings = standings,
            teamDisplayName = { "Opp $it" },
        )
        assertNotNull(summary)
        assertEquals(75, summary!!.snapshot.userScore)
        assertEquals(80, summary.snapshot.opponentScore)
        assertFalse(summary.hasBoxScore)
    }

    @Test
    fun fromSnapshot_marksBoxWhenPresent() {
        val snap = DashboardLastGameSnapshot(
            opponentDisplayName = "Kentucky",
            userScore = 71,
            opponentScore = 68,
            userWon = true,
            weekIndex = 4,
            phase = CalendarPhase.RegularSeason,
            userTeamIsHome = true,
            userTeamDisplayName = "State",
            boxArchive = dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive(
                homeBox = dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedTeamBoxScore(
                    teamId = "h",
                    points = 71,
                    fieldGoalsMade = 0,
                    fieldGoalsAttempted = 0,
                    threePointsMade = 0,
                    threePointsAttempted = 0,
                    freeThrowsMade = 0,
                    freeThrowsAttempted = 0,
                    turnovers = 0,
                    possessions = 0,
                    personalFouls = 0,
                ),
                awayBox = dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedTeamBoxScore(
                    teamId = "a",
                    points = 68,
                    fieldGoalsMade = 0,
                    fieldGoalsAttempted = 0,
                    threePointsMade = 0,
                    threePointsAttempted = 0,
                    freeThrowsMade = 0,
                    freeThrowsAttempted = 0,
                    turnovers = 0,
                    possessions = 0,
                    personalFouls = 0,
                ),
            ),
        )
        val summary = DashboardLastUserGameResolver.fromSnapshot(snap)
        assertNotNull(summary)
        assertEquals(true, summary!!.hasBoxScore)
    }
}

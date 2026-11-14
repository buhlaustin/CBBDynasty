package dev.novux.dynasty.cbb.core.rankings

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConferenceStandingsTiebreakerEngineTest {
    private val teamA = TeamDisplayOverlay(
        teamId = "a",
        conferenceId = "conf",
        displayName = "Alpha",
        shortName = "A",
        homeCity = "City",
        homeState = "ST",
        primaryColorHex = "#000",
        secondaryColorHex = "#fff",
        logoUri = null,
        postseasonEligible = true,
        programPrestige = 50,
    )
    private val teamB = teamA.copy(teamId = "b", displayName = "Beta")
    private val teamC = teamA.copy(teamId = "c", displayName = "Charlie")

    @Test
    fun headToHeadBreaksConferenceRecordTie() {
        val games = listOf(
            LeagueGameResult("a", "b", 70, 60, rsWeekIndex = 5),
            LeagueGameResult("b", "c", 72, 65, rsWeekIndex = 6),
            LeagueGameResult("c", "a", 75, 68, rsWeekIndex = 7),
        )
        val rows = ConferenceStandingsTiebreakerEngine.sortConferenceTeams(
            conferenceId = "conf",
            teams = listOf(teamA, teamB, teamC),
            records = emptyMap(),
            seasonResults = games,
            tiebreakers = listOf("h2h"),
        )
        val aRank = rows.indexOfFirst { it.teamId == "a" }
        val bRank = rows.indexOfFirst { it.teamId == "b" }
        assertEquals(1, rows[aRank].wins)
        assertEquals(1, rows[bRank].wins)
        assertTrue("a won head-to-head vs b", aRank < bRank)
    }

    @Test
    fun pointDifferentialTiebreakerWhenH2HSplits() {
        val games = listOf(
            LeagueGameResult("a", "b", 80, 60, rsWeekIndex = 5),
            LeagueGameResult("b", "a", 85, 75, rsWeekIndex = 6),
        )
        val rows = ConferenceStandingsTiebreakerEngine.sortConferenceTeams(
            conferenceId = "conf",
            teams = listOf(teamA, teamB),
            records = emptyMap(),
            seasonResults = games,
            tiebreakers = listOf("h2h", "h2h-point-diff"),
        )
        assertEquals("a", rows.first().teamId)
    }

    @Test
    fun miniLeagueRanksThreeTeamTieGroup() {
        val games = listOf(
            LeagueGameResult("a", "b", 70, 60, rsWeekIndex = 1),
            LeagueGameResult("a", "c", 72, 65, rsWeekIndex = 2),
            LeagueGameResult("b", "c", 68, 60, rsWeekIndex = 3),
        )
        val rows = ConferenceStandingsTiebreakerEngine.sortConferenceTeams(
            conferenceId = "conf",
            teams = listOf(teamA, teamB, teamC),
            records = emptyMap(),
            seasonResults = games,
            tiebreakers = listOf("h2h"),
        )
        assertEquals(listOf("a", "b", "c"), rows.map { it.teamId })
    }

    @Test
    fun sortsByConferenceWinsBeforeTiebreakers() {
        val rows = ConferenceStandingsTiebreakerEngine.sortConferenceTeams(
            conferenceId = "conf",
            teams = listOf(teamA, teamB, teamC),
            records = emptyMap(),
            seasonResults = listOf(
                LeagueGameResult("a", "b", 70, 60, rsWeekIndex = 1),
                LeagueGameResult("a", "c", 72, 65, rsWeekIndex = 2),
                LeagueGameResult("b", "c", 68, 60, rsWeekIndex = 3),
            ),
            tiebreakers = listOf("h2h"),
        )
        assertEquals("a", rows.first().teamId)
        assertEquals(2, rows.first().wins)
    }
}

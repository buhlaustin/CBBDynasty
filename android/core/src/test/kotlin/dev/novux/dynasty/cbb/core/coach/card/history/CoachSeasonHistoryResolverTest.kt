package dev.novux.dynasty.cbb.core.coach.card.history

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class CoachSeasonHistoryResolverTest {
    private val user = "team_a"
  private val confTeams = setOf("team_a", "team_b")

    @Test
    fun conferenceRecordCountsOnlyConferenceGames() {
        val standings = LeagueStandingsState(
            seasonIndex = 1,
            seasonResults = listOf(
                LeagueGameResult("team_a", "team_b", 70, 60, 1),
                LeagueGameResult("team_a", "team_x", 80, 70, 2),
            ),
            records = mapOf(
                user to LeagueTeamRecord(user, 2, 0),
            ),
        )
        assertEquals("1-0", CoachSeasonHistoryResolver.conferenceRecordLabel(standings, user, confTeams))
    }
}

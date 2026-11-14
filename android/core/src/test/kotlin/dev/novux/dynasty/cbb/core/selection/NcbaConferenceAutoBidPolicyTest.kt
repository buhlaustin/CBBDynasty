package dev.novux.dynasty.cbb.core.selection

import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import org.junit.Assert.assertEquals
import org.junit.Test

class NcbaConferenceAutoBidPolicyTest {
    @Test
    fun picksStandingsLeaderPerConference() {
        val teams = listOf(
            team("a", "conf1", eligible = true),
            team("b", "conf1", eligible = true),
            team("c", "conf2", eligible = true),
        )
        val records = mapOf(
            "a" to LeagueTeamRecord(teamId = "a", wins = 8, losses = 2),
            "b" to LeagueTeamRecord(teamId = "b", wins = 6, losses = 4),
            "c" to LeagueTeamRecord(teamId = "c", wins = 10, losses = 0),
        )
        val champs = NcbaConferenceAutoBidPolicy.fromStandings(
            teams = teams,
            records = records,
            seasonResults = emptyList(),
            tiebreakersByConference = { emptyList() },
            netRankByTeam = mapOf("a" to 5, "b" to 1, "c" to 10),
        )
        assertEquals(setOf("a", "c"), champs)
    }

    private fun team(id: String, conf: String, eligible: Boolean) = TeamDisplayOverlay(
        teamId = id,
        conferenceId = conf,
        displayName = id,
        shortName = id,
        homeCity = "City",
        homeState = "ST",
        primaryColorHex = "#000000",
        secondaryColorHex = "#ffffff",
        logoUri = null,
        postseasonEligible = eligible,
        programPrestige = 50,
    )
}
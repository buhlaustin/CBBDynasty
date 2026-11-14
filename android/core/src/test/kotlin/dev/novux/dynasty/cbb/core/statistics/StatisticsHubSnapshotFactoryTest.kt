package dev.novux.dynasty.cbb.core.statistics

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.core.pack.ConferencePack
import dev.novux.dynasty.cbb.core.pack.IdentityPack
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.pack.TeamColorsPack
import dev.novux.dynasty.cbb.core.pack.TeamPack
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsHubSnapshotFactoryTest {
    @Test
    fun buildsNationalLeadersFromTeamRecords() {
        val pack = IdentityPack(
            packVersion = 1,
            membershipYear = "2026-27",
            rulesetYear = 2026,
            conferences = listOf(
                ConferencePack("conf-a", "Atlantic", 18, tournamentTemplateId = null),
            ),
            teams = listOf(
                team("t1", "conf-a", 80),
                team("t2", "conf-a", 70),
            ),
        )
        val index = IdentityPackIndex.from(pack)
        val standings = LeagueStandingsState(
            seasonIndex = 1,
            records = mapOf(
                "t1" to LeagueTeamRecord("t1", 10, 2),
                "t2" to LeagueTeamRecord("t2", 8, 4),
            ),
        )
        val snapshot = StatisticsHubSnapshotFactory.build(
            packIndex = index,
            standings = standings,
            dynastyId = "dyn-1",
            userTeamId = "t1",
            userConferenceId = "conf-a",
            userRoster = emptyList(),
            seasonRecords = emptyMap(),
        )
        assertTrue(snapshot.hasStats)
        assertTrue(snapshot.national.points.isNotEmpty())
        assertTrue(snapshot.conferenceLeadersById["conf-a"]!!.points.isNotEmpty())
    }

    private fun team(id: String, conf: String, prestige: Int) = TeamPack(
        id = id,
        conferenceId = conf,
        displayName = id.uppercase(),
        shortName = id,
        homeCity = "City",
        homeState = "ST",
        postseasonEligible = true,
        colors = TeamColorsPack("#000", "#fff"),
        arenaBaseName = "Arena",
        programPrestige = prestige,
    )
}

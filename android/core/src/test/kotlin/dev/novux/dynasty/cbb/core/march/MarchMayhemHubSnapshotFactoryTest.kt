package dev.novux.dynasty.cbb.core.march

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.pack.ConferencePack
import dev.novux.dynasty.cbb.core.pack.IdentityPack
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.pack.TeamColorsPack
import dev.novux.dynasty.cbb.core.pack.TeamPack
import dev.novux.dynasty.cbb.core.selection.SelectionSundayEngine
import dev.novux.dynasty.cbb.core.selection.SelectionSundayInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarchMayhemHubSnapshotFactoryTest {
    @Test
    fun buildsPanelsFromSelectionSunday() {
        val teams = (1..140).map { "t$it" }
        val index = IdentityPackIndex.from(syntheticPack(teams))
        val net = teams.withIndex().associate { (i, id) -> id to i + 1 }
        val selection = SelectionSundayEngine.run(
            SelectionSundayInput(
                seasonIndex = 1,
                netRankByTeam = net,
                postseasonEligible = teams.associateWith { true },
                heritageEligible = teams.associateWith { true },
                conferenceChampions = teams.take(32).toSet(),
                programPrestige = teams.associateWith { 60 },
                records = emptyMap(),
                results = emptyList(),
            ),
        )
        val standings = LeagueStandingsState(seasonIndex = 1, selectionSunday = selection)
        val snapshot = MarchMayhemHubSnapshotFactory.build(index, standings, userTeamId = "t1")
        assertTrue(snapshot.selectionPublished)
        assertEquals(32, snapshot.nci.field.size)
        assertEquals(16, snapshot.heritage.field.size)
        assertTrue(snapshot.nci.field.first().seedLabel.startsWith("NCI #"))
        assertEquals(3, snapshot.overview.rails.size)
        assertTrue(snapshot.overview.selectionFieldCount > 0)
    }

    private fun syntheticPack(teamIds: List<String>): IdentityPack {
        return IdentityPack(
            packVersion = 1,
            membershipYear = "2026-27",
            disclaimerAcceptedRequired = true,
            rulesetYear = 2026,
            conferences = listOf(
                ConferencePack(
                    id = "conf-a",
                    displayName = "Test",
                    leagueGameCount = 18,
                    tournamentTemplateId = null,
                ),
            ),
            teams = teamIds.map { id ->
                TeamPack(
                    id = id,
                    conferenceId = "conf-a",
                    displayName = id.uppercase(),
                    shortName = id,
                    homeCity = "X",
                    homeState = "CA",
                    postseasonEligible = true,
                    colors = TeamColorsPack("#000", "#fff"),
                    arenaBaseName = "Arena",
                )
            },
            tournamentTemplates = emptyList(),
        )
    }
}

package dev.novux.dynasty.cbb.core.preview

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview
import dev.novux.dynasty.cbb.core.school.SchoolRankingsSnapshot
import dev.novux.dynasty.cbb.core.school.SchoolSeasonRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePreviewScoutingBuilderTest {
    private val overview = SchoolHubOverview(
        teamId = "duke",
        displayName = "Duke",
        conferenceName = "ACC",
        isUserSchool = true,
        programPrestige = 92,
        sosRating = 58,
        powerIndex = 88,
        teamOffRating = 84,
        teamDefRating = 82,
        record = SchoolSeasonRecord(5, 2, 3, 1),
        rankings = SchoolRankingsSnapshot(
            netRank = 7,
            mediaPollRank = 5,
            coachesPollRank = 6,
            conferenceStandingLabel = "2nd ACC",
        ),
    )

    @Test
    fun zeroGames_usesRatingFallback() {
        val scouting = GamePreviewScoutingBuilder.fromOverview(overview, standings = null)
        assertTrue(scouting.line.pointsPerGame > 70.0)
        assertTrue(scouting.line.offensiveEfficiency in 90..118)
        assertEquals(7, scouting.line.netRank)
    }

    @Test
    fun withResults_computesPpg() {
        val standings = LeagueStandingsState(
            seasonIndex = 1,
            seasonResults = listOf(
                LeagueGameResult("duke", "unc", 80, 70, 1),
                LeagueGameResult("vt", "duke", 65, 75, 2),
            ),
            netRankByTeam = mapOf("duke" to 4),
            mediaPollRankByTeam = mapOf("duke" to 3),
        )
        val scouting = GamePreviewScoutingBuilder.fromOverview(overview, standings)
        assertEquals(77.5, scouting.line.pointsPerGame, 0.1)
        assertEquals(4, scouting.line.netRank)
    }
}

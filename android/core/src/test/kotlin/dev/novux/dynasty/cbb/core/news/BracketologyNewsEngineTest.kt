package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BracketologyNewsEngineTest {
    @Test
    fun skipsOutsideFebruaryWindow() {
        val standings = standingsWithNet()
        assertNull(
            BracketologyNewsEngine.headlineForWeek(standings, rsWeekIndex = 10, teamDisplayName = { it }),
        )
    }

    @Test
    fun publishesWeeklyBracketologyInFebruaryWindow() {
        val standings = standingsWithNet()
        val headline = BracketologyNewsEngine.headlineForWeek(
            standings,
            rsWeekIndex = 22,
            teamDisplayName = { "Team-$it" },
            userTeamId = "t2",
        )
        assertNotNull(headline)
        assertEquals(NewsHeadlineKind.Bracketology, headline!!.kind)
        assertTrue(headline.title.startsWith("Bracketology (Week 22):"))
        assertTrue(headline.title.contains("Team-t1"))
    }

    private fun standingsWithNet(): LeagueStandingsState =
        LeagueStandingsState(
            seasonIndex = 1,
            lastCommittedRsWeek = 22,
            netRankByTeam = mapOf(
                "t1" to 1,
                "t2" to 50,
                "t3" to 55,
                "t4" to 60,
                "t5" to 8,
            ),
        )
}

package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchPerformanceBudget
import org.junit.Assert.assertEquals
import org.junit.Test

class LeagueWeekBatchCoreCiPathTest {
    @Test
    fun qaPathMatchesFullLeagueBudget() {
        assertEquals("MEGA-QA-015", LeagueWeekBatchCoreCiPath.QA_ID)
        assertEquals(
            LeagueWeekBatchPerformanceBudget.FULL_LEAGUE_TEAM_COUNT,
            LeagueWeekBatchCoreCiPath.TEAM_COUNT,
        )
        assertEquals(
            LeagueWeekBatchPerformanceBudget.MAX_CORE_REGULAR_SEASON_WEEK_MS,
            LeagueWeekBatchCoreCiPath.MAX_MEDIAN_WEEK_MS,
        )
        assertEquals(12_000L, LeagueWeekBatchCoreCiPath.MAX_MEDIAN_WEEK_MS)
    }
}

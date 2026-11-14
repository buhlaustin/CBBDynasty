package dev.novux.dynasty.cbb.core.rankings

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConferenceStandingsH2hMiniLeagueTest {
    @Test
    fun miniLeagueRecordAmongTiedPool() {
        val pool = setOf("a", "b", "c")
        val games = listOf(
            LeagueGameResult("a", "b", 70, 60, rsWeekIndex = 1),
            LeagueGameResult("a", "c", 72, 65, rsWeekIndex = 2),
            LeagueGameResult("b", "c", 68, 60, rsWeekIndex = 3),
        )
        val leader = ConferenceStandingsH2hMiniLeague.recordAmong("a", pool, games)
        val mid = ConferenceStandingsH2hMiniLeague.recordAmong("b", pool, games)
        val last = ConferenceStandingsH2hMiniLeague.recordAmong("c", pool, games)
        assertEquals(2, leader.wins)
        assertTrue(ConferenceStandingsH2hMiniLeague.compareRecords(leader, mid) < 0)
        assertTrue(ConferenceStandingsH2hMiniLeague.compareRecords(mid, last) < 0)
    }
}

package dev.novux.dynasty.cbb.core.preview

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import org.junit.Assert.assertEquals
import org.junit.Test

class HeadToHeadHistoryResolverTest {
    @Test
    fun meetingsFromResults_userPerspective() {
        val meetings = HeadToHeadHistoryResolver.meetingsForPair(
            userTeamId = "duke",
            opponentTeamId = "unc",
            seasonIndex = 2,
            results = listOf(
                LeagueGameResult("duke", "unc", 80, 74, rsWeekIndex = 5),
                LeagueGameResult("unc", "duke", 71, 68, rsWeekIndex = 12),
            ),
        )
        assertEquals(2, meetings.size)
        assertEquals("2027-28", meetings[0].seasonLabel)
        assertEquals(80, meetings[0].userScore)
        assertEquals(74, meetings[0].opponentScore)
        assertEquals(68, meetings[1].userScore)
        assertEquals(71, meetings[1].opponentScore)
    }

    @Test
    fun collectDynasty_flattensSeasons() {
        val all = HeadToHeadHistoryResolver.collectDynastyMeetings(
            "duke",
            "unc",
            mapOf(
                1 to listOf(LeagueGameResult("duke", "unc", 70, 69, 3)),
                2 to listOf(LeagueGameResult("unc", "duke", 75, 72, 8)),
            ),
        )
        assertEquals(2, all.size)
        val record = HeadToHeadHistory.summarize(all)
        assertEquals(1, record.userWins)
        assertEquals(1, record.userLosses)
    }
}

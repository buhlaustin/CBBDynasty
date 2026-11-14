package dev.novux.dynasty.cbb.core.preseason

import dev.novux.dynasty.cbb.core.rankings.RankingsHubTeamRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PreseasonRankingsReviewUxPolicyTest {
    @Test
    fun userProgramRanks_resolvesFromPollLists() {
        val net = listOf(row("duke", 12))
        val media = listOf(row("duke", 8))
        val coaches = listOf(row("duke", 15))
        val ranks = PreseasonRankingsReviewUxPolicy.userProgramRanks("duke", net, media, coaches)
        assertNotNull(ranks)
        assertEquals(12, ranks!!.netRank)
        assertEquals(8, ranks.mediaRank)
        assertEquals(15, ranks.coachesRank)
    }

    @Test
    fun megaFrameId() {
        assertEquals("MEGA-FRAME-072", PreseasonRankingsReviewUxPolicy.MEGA_FRAME_ID)
    }

    private fun row(teamId: String, rank: Int) = RankingsHubTeamRow(
        teamId = teamId,
        displayName = teamId,
        rank = rank,
        top25Highlight = rank <= 25,
    )
}

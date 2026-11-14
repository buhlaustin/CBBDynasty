package dev.novux.dynasty.cbb.core.rankings

import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RankingsHubPublishPolicyTest {
    @Test
    fun publishedWhenPreseasonReviewComplete() {
        assertTrue(
            RankingsHubPublishPolicy.rankingsPublished(
                preseasonRankingsReviewComplete = true,
                standings = null,
            ),
        )
    }

    @Test
    fun publishedWhenStandingsNetPresentEvenIfPreseasonIncomplete() {
        val standings = LeagueStandingsState(seasonIndex = 1, netRankByTeam = mapOf("t1" to 1))
        assertTrue(
            RankingsHubPublishPolicy.rankingsPublished(
                preseasonRankingsReviewComplete = false,
                standings = standings,
            ),
        )
    }

    @Test
    fun unpublishedBeforeReviewAndWithoutStandingsNet() {
        assertFalse(
            RankingsHubPublishPolicy.rankingsPublished(
                preseasonRankingsReviewComplete = false,
                standings = null,
            ),
        )
    }
}

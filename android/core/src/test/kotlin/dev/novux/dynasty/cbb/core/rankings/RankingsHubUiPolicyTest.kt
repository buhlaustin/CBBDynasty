package dev.novux.dynasty.cbb.core.rankings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RankingsHubUiPolicyTest {
    @Test
    fun defaultTab_isTeamNetPolls() {
        assertTrue(RankingsHubUiPolicy.DEFAULT_TAB_IS_TEAM_NET)
        assertEquals(0, RankingsHubUiPolicy.DEFAULT_TAB_INDEX)
    }
}

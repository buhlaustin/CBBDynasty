package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingHubTabPolicyTest {
    @Test
    fun specTabInventory() {
        assertEquals(8, RecruitingHubDefaults.tabs.size)
        assertTrue(RecruitingHubDefaults.tabs.contains(RecruitingHubTab.BoardTargets))
        assertTrue(RecruitingHubDefaults.tabs.contains(RecruitingHubTab.Committed))
        assertTrue(RecruitingHubDefaults.tabs.contains(RecruitingHubTab.Lost))
        assertTrue(RecruitingHubDefaults.tabs.contains(RecruitingHubTab.HighSchool))
        assertTrue(RecruitingHubDefaults.tabs.contains(RecruitingHubTab.JuniorCollege))
    }

    @Test
    fun highSchoolFilter() {
        val pool = listOf(
            sample("hs-1", RecruitPoolKind.HighSchool),
            sample("j-1", RecruitPoolKind.Juco),
        )
        val filtered = RecruitingHubTabPolicy.filterPool(pool, RecruitingHubTab.HighSchool)
        assertEquals(1, filtered.size)
        assertEquals(RecruitPoolKind.HighSchool, filtered.single().poolKind)
    }

    @Test
    fun boardTargets_watchlistOnly() {
        val pool = listOf(sample("a", RecruitPoolKind.HighSchool), sample("b", RecruitPoolKind.Juco))
        val board = RecruitingHubTabPolicy.boardTargets(pool, setOf("b"))
        assertEquals(1, board.size)
        assertEquals("b", board.single().prospectId)
    }

    private fun sample(id: String, kind: RecruitPoolKind) = RecruitProspect(
        prospectId = id,
        displayName = id,
        stars = 3,
        position = PlayerPosition.Center,
        homeState = "OH",
        nilAskUsd = 10_000,
        scoutLetterOverall = "B",
        poolKind = kind,
    )
}

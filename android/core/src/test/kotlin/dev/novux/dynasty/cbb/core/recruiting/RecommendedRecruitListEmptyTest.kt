package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendedRecruitListEmptyTest {
    private val school = SchoolRecruitingContext(
        programPrestige = 70,
        schoolHomeState = "OH",
        nilPoolAnnualUsd = 500_000,
    )
    private val config = BalanceRecruitingRecommended(listCap = 30)

    @Test
    fun build_emptyPool_returnsEmptyWithoutCrash() {
        val rows = RecommendedRecruitList.build(
            pool = emptyList(),
            school = school,
            recommended = config,
        )
        assertTrue(rows.isEmpty())
    }

    @Test
    fun forceEmptyRecommendedPool_hookCanBeToggled() {
        RecruitingHubTestHooks.forceEmptyRecommendedPool = true
        try {
            assertTrue(RecruitingHubTestHooks.forceEmptyRecommendedPool)
        } finally {
            RecruitingHubTestHooks.forceEmptyRecommendedPool = false
        }
    }
}

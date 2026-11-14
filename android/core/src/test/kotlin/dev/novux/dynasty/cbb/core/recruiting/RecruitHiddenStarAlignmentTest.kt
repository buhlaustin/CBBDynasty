package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.qa.RecruitHiddenStarAlignmentPath
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitHiddenStarAlignmentTest {
    @Test
    fun megaRecr137_hiddenMonotoneWithStarsInSeededPool() {
        assertEquals(RecruitHiddenStarAlignmentPath.RECR_ID, "MEGA-RECR-137")
        val pool = IncomingClassSeeder.seed(
            enrollingSeasonIndex = 9,
            hsCount = 120,
            jucoCount = 60,
            seedKey = 13_371,
        )
        val hs = pool.filter { it.poolKind == RecruitPoolKind.HighSchool }
        assertHiddenStarMonotone(hs)
        val juco = pool.filter { it.poolKind == RecruitPoolKind.Juco }
        assertHiddenStarMonotone(juco)
    }

    @Test
    fun sampler_usesPercentileRankNotStarBand() {
        val random = Random(42)
        val first = RecruitHiddenRatingSampler.forPercentileRank(0, 100, random)
        val last = RecruitHiddenRatingSampler.forPercentileRank(99, 100, random)
        assertTrue(first > last)
    }

    private fun assertHiddenStarMonotone(prospects: List<RecruitProspect>) {
        val sorted = prospects.sortedByDescending { it.hiddenRating }
        for (i in 0 until sorted.size - 1) {
            assertTrue(
                "hidden ${sorted[i].hiddenRating} stars ${sorted[i].stars} vs " +
                    "${sorted[i + 1].hiddenRating} stars ${sorted[i + 1].stars}",
                sorted[i].stars >= sorted[i + 1].stars,
            )
        }
    }
}

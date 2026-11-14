package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.recruiting.IncomingClassSeeder
import dev.novux.dynasty.cbb.core.recruiting.RecruitingClassSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NationalRecruitingRankingsTest {
    @Test
    fun top60_cappedAtSixty() {
        val rows = NationalRecruitingRankings.top60(1)
        assertTrue(rows.size <= 60)
        assertEquals(1, rows.first().rank)
    }

    @Test
    fun top60FromDemoPool_followsHiddenRatingOrder() {
        val pool = RecruitingClassSeed.demoPool(3)
        val ranked = pool.filter { it.nationalHsRank != null }.sortedBy { it.nationalHsRank!! }
        val byHidden = pool.sortedByDescending { it.hiddenRating }.take(IncomingClassSeeder.TOP_NATIONAL_RANKS)
        assertEquals(60, ranked.size)
        ranked.zip(byHidden).forEach { (row, expected) ->
            assertEquals(expected.prospectId, row.prospectId)
        }
    }
}

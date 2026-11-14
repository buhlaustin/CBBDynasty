package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.signingday.NationalRecruitingRankings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingClassSeederTest {
    @Test
    fun hsPoolSizeDrawsUniformBandPerSeason() {
        val count = IncomingClassSeeder.resolveHsPoolSize(enrollingSeasonIndex = 3, seedKey = 99)
        assertTrue(count in IncomingClassSeeder.HS_POOL_MIN..IncomingClassSeeder.HS_POOL_MAX)
        assertEquals(count, IncomingClassSeeder.resolveHsPoolSize(3, 99))
        val otherSeason = IncomingClassSeeder.resolveHsPoolSize(enrollingSeasonIndex = 4, seedKey = 99)
        assertTrue(otherSeason in IncomingClassSeeder.HS_POOL_MIN..IncomingClassSeeder.HS_POOL_MAX)
    }

    @Test
    fun defaultSeedUsesHsBandPlusJuco() {
        val pool = IncomingClassSeeder.seed(enrollingSeasonIndex = 5, seedKey = 7_777)
        val hsCount = pool.count { it.poolKind == RecruitPoolKind.HighSchool }
        assertTrue(hsCount in IncomingClassSeeder.HS_POOL_MIN..IncomingClassSeeder.HS_POOL_MAX)
        assertEquals(IncomingClassSeeder.JUCO_POOL_SIZE, pool.count { it.poolKind == RecruitPoolKind.Juco })
        assertTrue(pool.size > 120)
    }

    @Test
    fun poolsIncludeHsAndJucoWithTop60() {
        val pool = IncomingClassSeeder.seed(
            enrollingSeasonIndex = 2,
            hsCount = 200,
            jucoCount = 80,
            seedKey = 42,
        )
        assertEquals(280, pool.size)
        assertTrue(pool.any { it.poolKind == RecruitPoolKind.HighSchool })
        assertTrue(pool.any { it.poolKind == RecruitPoolKind.Juco })
        val top = NationalRecruitingRankings.top60FromPool(pool)
        assertEquals(60, top.size)
        assertEquals(1, top.first().rank)
        pool.forEach { prospect ->
            assertTrue(prospect.topPriorities.isNotEmpty())
            assertTrue(prospect.topPriorities.size <= 2)
        }
    }

    @Test
    fun starsFollowHiddenRatingPercentilesWithinEachPool() {
        val hsCount = 200
        val jucoCount = 100
        val pool = IncomingClassSeeder.seed(
            enrollingSeasonIndex = 1,
            hsCount = hsCount,
            jucoCount = jucoCount,
            seedKey = 99,
        )
        assertStarBandsMatchHiddenOrder(pool.filter { it.poolKind == RecruitPoolKind.HighSchool })
        assertStarBandsMatchHiddenOrder(pool.filter { it.poolKind == RecruitPoolKind.Juco })
    }

    private fun assertStarBandsMatchHiddenOrder(prospects: List<RecruitProspect>) {
        val size = prospects.size
        val sorted = prospects.sortedByDescending { it.hiddenRating }
        val expectedForHidden = sorted
            .mapIndexed { rank, prospect -> prospect.hiddenRating to rank }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, ranks) ->
                IncomingClassSeeder.starBandForPercentile(ranks.min().toDouble() / size)
            }
        prospects.forEach { prospect ->
            assertEquals(
                "hidden=${prospect.hiddenRating}",
                expectedForHidden.getValue(prospect.hiddenRating),
                prospect.stars,
            )
        }
    }
}

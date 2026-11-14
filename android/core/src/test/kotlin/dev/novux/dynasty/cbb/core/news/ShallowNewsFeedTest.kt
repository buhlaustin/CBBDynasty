package dev.novux.dynasty.cbb.core.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShallowNewsFeedTest {
    @Test
    fun capsAndPrioritizesUserInjury() {
        val low = NewsHeadline("a", 1, kind = NewsHeadlineKind.LeagueResult, title = "game", priority = 10, createdAtMs = 1)
        val high = NewsHeadline("b", 1, kind = NewsHeadlineKind.UserInjury, title = "injury", priority = 100, createdAtMs = 2)
        val feed = ShallowNewsFeed.select(listOf(low, high), maxItems = 1)
        assertEquals(1, feed.size)
        assertEquals(NewsHeadlineKind.UserInjury, feed.first().kind)
    }

    @Test
    fun snapshotReportsTruncation() {
        val items = (1..12).map { i ->
            NewsHeadline("id-$i", 1, kind = NewsHeadlineKind.General, title = "t$i", priority = i, createdAtMs = i.toLong())
        }
        val snap = ShallowNewsFeed.snapshot(items, maxItems = 10)
        assertEquals(10, snap.headlines.size)
        assertEquals(12, snap.mergedCount)
        assertTrue(snap.truncated)
        assertEquals(10, snap.cap)
    }

    @Test
    fun leagueInjuryTierFromCount() {
        val headline = NewsHeadlinePublisher.leagueInjuryWire(1, rsWeekIndex = 3, programsAffected = 6)
        assertTrue(headline != null)
        assertEquals(NewsHeadlineKind.LeagueInjury, headline!!.kind)
    }
}

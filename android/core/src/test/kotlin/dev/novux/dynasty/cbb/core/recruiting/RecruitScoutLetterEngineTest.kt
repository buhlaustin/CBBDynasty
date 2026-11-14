package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitScoutLetterEngineTest {
    @Test
    fun categoryRows_coverAFBands() {
        val prospect = RecruitProspect(
            prospectId = "hs-1-42",
            displayName = "Test Player",
            stars = 4,
            position = PlayerPosition.PointGuard,
            homeState = "OH",
            nilAskUsd = 50_000,
            scoutLetterOverall = "B",
            hiddenRating = 85,
        )
        val letters = RecruitScoutLetterEngine.fromProspect(prospect)
        assertEquals(7, letters.asRows().size)
        letters.asRows().forEach { (_, grade) ->
            assertTrue(grade.matches(Regex("[A-F][+-]?")))
        }
    }

    @Test
    fun sameProspect_isDeterministic() {
        val a = RecruitScoutLetterEngine.fromHiddenRating(72, "prospect-a")
        val b = RecruitScoutLetterEngine.fromHiddenRating(72, "prospect-a")
        assertEquals(a, b)
    }

    @Test
    fun differentProspects_differOnAtLeastOneCategory() {
        val a = RecruitScoutLetterEngine.fromHiddenRating(72, "prospect-a")
        val b = RecruitScoutLetterEngine.fromHiddenRating(72, "prospect-b")
        assertNotEquals(a, b)
    }
}

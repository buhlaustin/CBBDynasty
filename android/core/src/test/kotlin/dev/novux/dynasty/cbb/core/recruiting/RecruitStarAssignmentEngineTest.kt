package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.qa.RecruitStarAssignmentOrderPath
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitStarAssignmentEngineTest {
    @Test
    fun megaRecr136_starsTrackHiddenNotCreationIndex() {
        assertEquals(RecruitStarAssignmentOrderPath.RECR_ID, "MEGA-RECR-136")
        val pool = listOf(
            prospect("a", hidden = 95, creationIndex = 0),
            prospect("b", hidden = 40, creationIndex = 1),
            prospect("c", hidden = 88, creationIndex = 2),
        )
        val assigned = RecruitStarAssignmentEngine.assignStarsWithinPool(pool, jucoBias = false)
        val byId = assigned.associateBy { it.prospectId }
        assertTrue(byId.getValue("a").stars >= byId.getValue("c").stars)
        assertTrue(byId.getValue("c").stars >= byId.getValue("b").stars)
    }

    @Test
    fun tiesOnHiddenShareStarBand() {
        val pool = List(10) { i ->
            prospect("p$i", hidden = if (i < 5) 90 else 50, creationIndex = i)
        }
        val assigned = RecruitStarAssignmentEngine.assignStarsWithinPool(pool, jucoBias = false)
        val highStars = assigned.filter { it.hiddenRating == 90 }.map { it.stars }.toSet()
        assertEquals(1, highStars.size)
    }

    private fun prospect(id: String, hidden: Int, creationIndex: Int) = RecruitProspect(
        prospectId = id,
        displayName = "P $creationIndex",
        stars = 2,
        position = PlayerPosition.SmallForward,
        homeState = "OH",
        nilAskUsd = 0,
        scoutLetterOverall = "C",
        hiddenRating = hidden,
    )
}

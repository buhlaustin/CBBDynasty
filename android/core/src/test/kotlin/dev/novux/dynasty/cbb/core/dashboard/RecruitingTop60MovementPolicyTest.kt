package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.qa.RecruitingTop60MovementPath
import dev.novux.dynasty.cbb.core.recruiting.RecruitPoolKind
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingTop60MovementPolicyTest {
    @Test
    fun megaRecr147_firesWhenRankChangesForWatchlistTarget() {
        assertEquals(RecruitingTop60MovementPath.RECR_ID, "MEGA-RECR-147")
        val prospect = prospect("w1", "Casey Brooks")
        val movements = RecruitingTop60MovementPolicy.detectMovements(
            watchlistIds = setOf("w1"),
            prospectsById = mapOf(prospect.prospectId to prospect),
            previousRankByProspect = mapOf("w1" to 18),
            currentRankByProspect = mapOf("w1" to 9),
        )
        assertEquals(1, movements.size)
        assertEquals(9, movements.single().rank)
        assertEquals(18, movements.single().previousRank)
    }

    @Test
    fun suppressesWhenRankUnchanged() {
        val prospect = prospect("w1", "Casey Brooks")
        val movements = RecruitingTop60MovementPolicy.detectMovements(
            watchlistIds = setOf("w1"),
            prospectsById = mapOf(prospect.prospectId to prospect),
            previousRankByProspect = mapOf("w1" to 12),
            currentRankByProspect = mapOf("w1" to 12),
        )
        assertTrue(movements.isEmpty())
    }

    @Test
    fun firstObservationOnBoard_firesOnce() {
        val prospect = prospect("w1", "Casey Brooks")
        val movements = RecruitingTop60MovementPolicy.detectMovements(
            watchlistIds = setOf("w1"),
            prospectsById = mapOf(prospect.prospectId to prospect),
            previousRankByProspect = emptyMap(),
            currentRankByProspect = mapOf("w1" to 4),
        )
        assertEquals(4, movements.single().rank)
        assertEquals(null, movements.single().previousRank)
    }

    private fun prospect(id: String, name: String): RecruitProspect =
        RecruitProspect(
            prospectId = id,
            displayName = name,
            stars = 5,
            hiddenRating = 90,
            homeState = "OH",
            position = PlayerPosition.ShootingGuard,
            poolKind = RecruitPoolKind.HighSchool,
            topPriorities = emptyList(),
            scoutLetterOverall = "A",
            nilAskUsd = 25_000,
        )
}

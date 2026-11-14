package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class HsJucoRecruitBoardTest {
    @Test
    fun partition_sortsHsByNationalRank() {
        val pool = listOf(
            prospect("b", RecruitPoolKind.HighSchool, rank = 12),
            prospect("a", RecruitPoolKind.HighSchool, rank = 3),
            prospect("j", RecruitPoolKind.Juco),
        )
        val board = HsJucoRecruitBoard.partition(pool)
        assertEquals(listOf("a", "b"), board.highSchool.map { it.prospectId })
        assertEquals(1, board.juco.size)
    }

    private fun prospect(id: String, kind: RecruitPoolKind, rank: Int? = null) = RecruitProspect(
        prospectId = id,
        displayName = "Player $id",
        stars = 3,
        position = PlayerPosition.ShootingGuard,
        homeState = "TX",
        nilAskUsd = 10_000,
        scoutLetterOverall = "B",
        poolKind = kind,
        nationalHsRank = rank,
        hiddenRating = 70,
    )
}

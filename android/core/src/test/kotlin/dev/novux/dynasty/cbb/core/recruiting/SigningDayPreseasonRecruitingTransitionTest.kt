package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningDayPreseasonRecruitingTransitionTest {
    private val ledger = RecruitingCycleLedger(
        cycleSeasonIndex = 1,
        programPrestige = 55,
        weeklyPoolBase = 40,
        pointsAvailable = 40,
    )

    @Test
    fun seasonOneWeekOneKeepsLedgerClosed() {
        val gated = SigningDayPreseasonRecruitingTransition.seasonOneOffseasonWeekOneLedger(ledger)
        assertTrue(gated.cycleClosed)
        assertEquals(0, gated.pointsAvailable)
    }

    @Test
    fun weekTwoReopensChasingForSeasonOne() {
        val position = CalendarPosition(seasonIndex = 1, phase = CalendarPhase.Offseason, weekIndex = 2)
        val reopen = SigningDayPreseasonRecruitingTransition.reopenSeasonOneChasingAtWeekTwo(
            position,
            membershipSeasonIndex = 1,
            ledger.copy(cycleClosed = true, pointsAvailable = 0),
        )
        assertNotNull(reopen)
        assertEquals(false, reopen!!.cycleClosed)
    }

    @Test
    fun preseasonSeedsNextClassIndex() {
        assertEquals(2, SigningDayPreseasonRecruitingTransition.nextEnrollingClassSeasonIndex(1))
        assertTrue(SigningDayPreseasonRecruitingTransition.enteringPreseason(
            CalendarPosition(seasonIndex = 1, phase = CalendarPhase.Preseason, weekIndex = 1),
        ))
    }
}

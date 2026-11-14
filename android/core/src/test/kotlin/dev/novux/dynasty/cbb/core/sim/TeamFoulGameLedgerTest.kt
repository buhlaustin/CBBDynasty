package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.TeamFoulGamePath
import org.junit.Assert.assertEquals
import org.junit.Test

class TeamFoulGameLedgerTest {
    @Test
    fun megaSim105_qaAnchor() {
        assertEquals("MEGA-SIM-105", TeamFoulGamePath.SIM_ID)
    }

    @Test
    fun firstHalfFouls_sealAtHalftime_resetCurrentHalf() {
        val ledger = TeamFoulGameLedger()
        repeat(7) { ledger.recordDefensiveFoul(defenseIsHome = false) }
        repeat(3) { ledger.recordDefensiveFoul(defenseIsHome = true) }
        ledger.onRegulationHalfBoundaryCrossed()
        val sealed = ledger.summary()
        assertEquals(3, sealed.homeFirstHalf)
        assertEquals(7, sealed.awayFirstHalf)
        assertEquals(0, sealed.homeSecondHalf)
        assertEquals(0, sealed.awaySecondHalf)
        ledger.recordDefensiveFoul(defenseIsHome = false)
        val afterOne = ledger.summary()
        assertEquals(3, afterOne.homeFirstHalf)
        assertEquals(7, afterOne.awayFirstHalf)
        assertEquals(0, afterOne.homeSecondHalf)
        assertEquals(1, afterOne.awaySecondHalf)
    }

    @Test
    fun resume_secondHalf_restoresSealedFirstHalf() {
        val resume = MidGameState(
            random = kotlin.random.Random(1),
            gameClockSeconds = GameSimClock.HALF_SECONDS,
            offenseHome = true,
            homeBox = TeamBoxScore(teamId = "home"),
            awayBox = TeamBoxScore(teamId = "away"),
            possessionLog = emptyList(),
            totalPossessions = 0,
            homeTeamFoulsInHalf = 2,
            awayTeamFoulsInHalf = 5,
            homeTeamFoulsFirstHalf = 4,
            awayTeamFoulsFirstHalf = 6,
        )
        val ledger = TeamFoulGameLedger(resume)
        val summary = ledger.summary()
        assertEquals(4, summary.homeFirstHalf)
        assertEquals(6, summary.awayFirstHalf)
        assertEquals(2, summary.homeSecondHalf)
        assertEquals(5, summary.awaySecondHalf)
    }

    @Test
    fun restoreMidGameFields_roundTrip() {
        val ledger = TeamFoulGameLedger()
        repeat(5) { ledger.recordDefensiveFoul(defenseIsHome = true) }
        val fields = ledger.restoreMidGameFields()
        assertEquals(5, fields.homeTeamFoulsInHalf)
        assertEquals(0, fields.awayTeamFoulsInHalf)
        assertEquals(0, fields.homeTeamFoulsFirstHalf)
    }
}

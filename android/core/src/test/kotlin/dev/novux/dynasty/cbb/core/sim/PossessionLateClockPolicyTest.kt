package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionLateClockPath
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionLateClockPolicyTest {
    @Test
    fun megaSim088_lateClockWindowIs22Through30SecondsUsed() {
        assertEquals(PossessionLateClockPath.SIM_ID, "MEGA-SIM-088")
        assertFalse(PossessionLateClockPolicy.isLateClockTrip(21))
        assertTrue(PossessionLateClockPolicy.isLateClockTrip(22))
        assertTrue(PossessionLateClockPolicy.isLateClockTrip(30))
        assertFalse(PossessionLateClockPolicy.isLateClockTrip(31))
    }

    @Test
    fun applyToEffects_increasesTurnoverAndLowersMakeTendency() {
        val base = ResolvedSchemeEffects(
            tripPaceScale = 1.0,
            threePointAttemptRate = 0.38,
            turnoverChance = 0.22,
            makeRateAdjust = 0.0,
            offensiveRatingDelta = 0,
            defensiveRatingDelta = 0,
        )
        val late = PossessionLateClockPolicy.applyToEffects(base)
        assertTrue(late.turnoverChance > base.turnoverChance)
        assertTrue(late.makeRateAdjust < base.makeRateAdjust)
    }
}

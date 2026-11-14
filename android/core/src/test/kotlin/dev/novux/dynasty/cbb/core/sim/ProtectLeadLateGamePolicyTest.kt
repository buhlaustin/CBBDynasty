package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.ProtectLeadLateGamePath
import dev.novux.dynasty.cbb.core.sim.tuning.SimProtectLeadWeights
import dev.novux.dynasty.cbb.core.strategy.LateGamePossessionPolicy
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectLeadLateGamePolicyTest {
    private val weights = SimProtectLeadWeights()

    @Test
    fun megaSim113_qaAnchor() {
        assertEquals(ProtectLeadLateGamePath.SIM_ID, ProtectLeadLateGamePolicy.QA_ID)
    }

    @Test
    fun paceTripScale_increasesRawSeconds() {
        val adjusted = ProtectLeadLateGamePolicy.adjustRawTripSeconds(10, weights)
        assertTrue(adjusted > 10)
        assertTrue(adjusted <= GameSimClock.MAX_TRIP_SECONDS)
    }

    @Test
    fun threePointShootingFoul_multiplierLowerThanTwoPoint() {
        val three = ProtectLeadLateGamePolicy.shootingFoulRateMultiplier(threePointAttempt = true, weights)
        val two = ProtectLeadLateGamePolicy.shootingFoulRateMultiplier(threePointAttempt = false, weights)
        assertTrue(three < two)
        assertTrue(three < 1.0)
    }

    @Test
    fun protectLead_reducesThreePointMissFoulRolls() {
        var baseline = 0
        var protected = 0
        repeat(400) { seed ->
            val random = Random(seed.toLong())
            if (PossessionShootingFoulPolicy.rollMissShootingFoul(false, random, rateMultiplier = 1.0)) {
                baseline++
            }
        }
        repeat(400) { seed ->
            val random = Random(seed.toLong())
            if (
                PossessionShootingFoulPolicy.rollMissShootingFoul(
                    false,
                    random,
                    rateMultiplier = weights.threePointShootingFoulMultiplier,
                )
            ) {
                protected++
            }
        }
        assertTrue(protected < baseline)
    }

    @Test
    fun isActive_onlyOnProtectLeadDecision() {
        assertTrue(
            ProtectLeadLateGamePolicy.isActive(LateGamePossessionPolicy.PossessionDecision.ProtectLeadPace),
        )
    }
}

package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.FoulEarlyLateGamePath
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimFoulEarlyWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.strategy.LateGamePossessionPolicy
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoulEarlyLateGamePolicyTest {
  private val tuning = SimTuning(
        simTuningId = "test",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = SimShotClock(full = 30, offensiveRebound = 20),
        timeUsedSeconds = SimTimeUsedSeconds(
            quickTurnover = listOf(3, 6),
            quickShot = listOf(8, 14),
            foulStoppage = listOf(12, 18),
        ),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    @Test
    fun megaSim114_qaAnchor() {
        assertEquals(FoulEarlyLateGamePath.SIM_ID, FoulEarlyLateGamePolicy.QA_ID)
    }

    @Test
    fun sampleTrip_canProduceHackaAndIntentionalFouls() {
        val weights = SimFoulEarlyWeights(
            intentionalFoulTripRate = 0.25,
            hackaQuickFoulTripRate = 0.35,
            hackaTripMaxSeconds = 6,
        )
        var intentional = 0
        var hacka = 0
        repeat(200) { seed ->
            val trip = PossessionSimEngine.sampleTripForLateGame(
                tuning,
                kotlin.random.Random(seed.toLong()),
                LateGamePossessionPolicy.PossessionDecision.IntentionalFoulTrip,
            )
            if (trip.intentionalFoul) intentional++
            if (trip.foulEarlyHacka) {
                hacka++
                assertTrue(trip.rawSeconds <= 6)
                assertTrue(trip.foulStoppage)
            }
        }
        assertTrue(intentional > 0)
        assertTrue(hacka > 0)
    }

    @Test
    fun isFoulToStopClock_coversHackaAndIntentional() {
        val hacka = PossessionSimEngine.TripSample(4, foulStoppage = true, foulEarlyHacka = true)
        val intentional = PossessionSimEngine.TripSample(12, foulStoppage = true, intentionalFoul = true)
        assertTrue(FoulEarlyLateGamePolicy.isFoulToStopClock(hacka))
        assertTrue(FoulEarlyLateGamePolicy.isFoulToStopClock(intentional))
    }
}

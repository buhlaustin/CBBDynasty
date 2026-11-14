package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.sim.PossessionSimEngine
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LateGamePossessionPolicyTest {
    @Test
    fun foulEarlyWhenTrailingLate() {
        val decision = LateGamePossessionPolicy.defenseDecision(
            LateGameFoulStrategy.FoulEarly,
            gameClockSeconds = 120,
            defenseScore = 60,
            offenseScore = 65,
        )
        assertEquals(LateGamePossessionPolicy.PossessionDecision.IntentionalFoulTrip, decision)
    }

    @Test
    fun protectLeadWhenAheadLate() {
        val decision = LateGamePossessionPolicy.defenseDecision(
            LateGameFoulStrategy.ProtectLead,
            gameClockSeconds = 200,
            defenseScore = 70,
            offenseScore = 65,
        )
        assertEquals(LateGamePossessionPolicy.PossessionDecision.ProtectLeadPace, decision)
    }

    @Test
    fun intentionalFoulTripCanForceFoulStoppageSample() {
        val tuning = SimTuning(
            simTuningId = "t",
            schemaVersion = 1,
            possessionsPerGameTarget = IntMinMax(60, 70),
            shotClock = SimShotClock(30, 20),
            timeUsedSeconds = SimTimeUsedSeconds(listOf(3, 6), listOf(8, 14), listOf(12, 18)),
            eventWeights = JsonObject(emptyMap()),
            homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
        )
        var sawFoulStoppage = false
        repeat(50) { seed ->
            val trip = PossessionSimEngine.sampleTripForLateGame(
                tuning,
                kotlin.random.Random(seed.toLong()),
                LateGamePossessionPolicy.PossessionDecision.IntentionalFoulTrip,
            )
            if (trip.foulStoppage) sawFoulStoppage = true
            if (trip.intentionalFoul || trip.foulEarlyHacka) {
                assertTrue(trip.foulStoppage)
                sawFoulStoppage = true
            }
        }
        assertTrue(sawFoulStoppage)
    }
}

package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionPaceGovernorTest {
    private val tuning = SimTuning(
        simTuningId = "t",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = SimShotClock(30, 20),
        timeUsedSeconds = SimTimeUsedSeconds(listOf(3, 6), listOf(8, 14), listOf(12, 18)),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    @Test
    fun slowsTripsWhenAheadOfPace() {
        val mult = PossessionPaceGovernor.tripPaceMultiplier(
            totalPossessionsSoFar = 80,
            gameClockRemaining = GameSimClock.HALF_SECONDS,
            tuning = tuning,
        )
        assertTrue(mult > 1.0)
    }

    @Test
    fun speedsTripsWhenBehindPace() {
        val mult = PossessionPaceGovernor.tripPaceMultiplier(
            totalPossessionsSoFar = 20,
            gameClockRemaining = GameSimClock.HALF_SECONDS,
            tuning = tuning,
        )
        assertTrue(mult < 1.0)
    }
}

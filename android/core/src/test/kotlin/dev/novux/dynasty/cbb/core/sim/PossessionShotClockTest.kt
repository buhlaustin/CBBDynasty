package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionShotClockTest {
    private val clock = SimShotClock(full = 30, offensiveRebound = 20)
    private val tuning = SimTuning(
        simTuningId = "t",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = clock,
        timeUsedSeconds = SimTimeUsedSeconds(
            quickTurnover = listOf(3, 6),
            quickShot = listOf(8, 14),
            foulStoppage = listOf(12, 18),
        ),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    @Test
    fun resetValuesFromTuning() {
        assertEquals(30, PossessionShotClock.resetOnChangeOfPossession(clock))
        assertEquals(20, PossessionShotClock.resetAfterOffensiveRebound(clock))
    }

    @Test
    fun secondsConsumed_cappedByShotAndGameClock() {
        assertEquals(8, PossessionShotClock.secondsConsumed(sampledTrip = 14, shotClockRemaining = 8, gameClockRemaining = 600))
        assertEquals(5, PossessionShotClock.secondsConsumed(sampledTrip = 20, shotClockRemaining = 30, gameClockRemaining = 5))
    }

    @Test
    fun shotClockViolation_whenSampledTripExceedsClock() {
        assertFalse(PossessionShotClock.isShotClockViolation(shotClockBefore = 30, used = 30, sampledTrip = 30))
        assertTrue(PossessionShotClock.isShotClockViolation(shotClockBefore = 30, used = 30, sampledTrip = 31))
        assertTrue(PossessionShotClock.isShotClockViolation(shotClockBefore = 20, used = 20, sampledTrip = 28))
        assertFalse(PossessionShotClock.isShotClockViolation(shotClockBefore = 30, used = 12, sampledTrip = 12))
    }

    @Test
    fun shotClockViolation_resolvesTurnoverAndLog() {
        val before = TeamBoxScore(teamId = "h")
        val result = PossessionSimEngine.resolveShotClockViolation(before)
        assertEquals(1, result.box.turnovers)
        assertEquals(TurnoverTerminal.ShotClockViolation, result.turnoverTerminal)
        val line = PossessionSimEngine.formatPossessionLog(
            used = 30,
            clockRemaining = 1200,
            offense = "HOME",
            before = before,
            after = result.box,
            shotClockBefore = 30,
            shotClockAfter = 0,
            change = result.change,
            turnoverTerminal = result.turnoverTerminal,
        )
        assertTrue(line.contains("TO shot-clock"))
    }

    @Test
    fun simulationLogCarriesShotClockMarker() {
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 78, 74),
                away = GameSimTeamInput("a", 76, 75),
                tuning = tuning,
                seed = 4242L,
            ),
        )
        assertTrue(result.possessionLog.any { "| SC " in it })
    }

    @Test
    fun midGamePersistsShotClock() {
        val firstHalf = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 78, 74),
                away = GameSimTeamInput("a", 76, 75),
                tuning = tuning,
                seed = 77L,
            ),
            SimulateOptions(stopAtHalftime = true),
        )
        val mid = firstHalf.midGame!!
        assertTrue(mid.shotClockSecondsRemaining in 1..30)
    }
}

package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import kotlinx.serialization.json.JsonObject
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSimOvertimeTest {
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

    private val input = GameSimInput(
        home = GameSimTeamInput("h", 78, 74),
        away = GameSimTeamInput("a", 76, 75),
        tuning = tuning,
        seed = 1L,
    )

    @Test
    fun tiedRegulationStartsOvertimePeriod() {
        val mid = MidGameState(
            random = Random(42),
            gameClockSeconds = 0,
            offenseHome = true,
            homeBox = TeamBoxScore("h", points = 65),
            awayBox = TeamBoxScore("a", points = 65),
            possessionLog = emptyList(),
            totalPossessions = 50,
        )
        val session = PossessionSimSession(input, resume = mid)
        assertTrue(session.isTiedAtBuzz())
        session.beginOvertimePeriod()
        assertTrue(session.hasNext())
        assertEquals(GameSimClock.OVERTIME_SECONDS, session.toMidGame().gameClockSeconds)
        assertEquals(1, session.periodsPlayedTotal() - 1)
    }

    @Test
    fun simulateEventuallyProducesOvertimeOrWinner() {
        var sawOt = false
        var sawWinner = false
        repeat(400) { i ->
            val result = PossessionSimEngine.simulate(input.copy(seed = i.toLong()))
            assertNotEquals(result.homeScore, result.awayScore)
            sawWinner = true
            if (result.overtimePeriods > 0) {
                sawOt = true
                assertTrue(result.periodsPlayed > 1)
                assertTrue(result.possessionLog.any { it.contains("OT") })
            }
        }
        assertTrue(sawWinner)
        assertTrue("Expected at least one OT in seed sweep", sawOt)
    }
}

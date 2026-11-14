package dev.novux.dynasty.cbb.core.league

import dev.novux.dynasty.cbb.core.balance.BalanceInjuryOccurrence
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTeamFoulRules
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueFastSimBatchTest {
    private val tuning = SimTuning(
        simTuningId = "test",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(65, 75),
        shotClock = SimShotClock(30, 20),
        timeUsedSeconds = SimTimeUsedSeconds(listOf(3, 8), listOf(8, 18), listOf(10, 14)),
        eventWeights = JsonPrimitive("default"),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
        teamFoulRules = SimTeamFoulRules(),
    )

    @Test
    fun batchSimUsesPossessionEngineNotRandomStub() {
        val batch = LeagueBatchSimParams(
            tuning = tuning,
            programPrestigeByTeamId = mapOf("a" to 85, "b" to 55),
            phase = CalendarPhase.RegularSeason,
            injuryBank = emptyList(),
            injuryOccurrence = BalanceInjuryOccurrence(),
        )
        val matchup = LeagueMatchup("a", "b", rsWeekIndex = 1)
        val first = LeagueFastSim.simulateWithBatch(matchup, seed = 42L, batch = batch)
        val second = LeagueFastSim.simulateWithBatch(matchup, seed = 99L, batch = batch)
        assertTrue(first.league.homeScore in 40..120)
        assertNotEquals(first.league.homeScore, second.league.homeScore)
        assertTrue(first.sim.homeBox.possessions > 0)
    }
}

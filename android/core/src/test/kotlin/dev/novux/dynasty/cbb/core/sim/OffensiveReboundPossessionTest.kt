package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlin.random.Random
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-SIM-090] offensive rebound retains possession and resets shot clock to 20s. */
class OffensiveReboundPossessionTest {
    private val clock = SimShotClock(full = 30, offensiveRebound = 20)

    @Test
    fun missRebound_offensivePath_retainsPossessionChange() {
        val afterMiss = TeamBoxScore(teamId = "home", possessions = 1, fieldGoalsAttempted = 1)
        var oreb: PossessionResolveResult? = null
        for (seed in 0 until 500) {
            val result = PossessionSimEngine.resolveMissRebound(afterMiss, Random(seed.toLong()))
            if (result.change == PossessionChange.OffenseRetainsOffensiveRebound) {
                oreb = result
                break
            }
        }
        assertTrue(oreb != null)
        assertEquals(afterMiss.fieldGoalsAttempted, oreb!!.box.fieldGoalsAttempted)
        assertEquals(0, oreb.box.fieldGoalsMade)
    }

    @Test
    fun possessionLog_offensiveRebound_includesOreBStatLine() {
        val before = TeamBoxScore(teamId = "KU")
        val after = before.copy(fieldGoalsAttempted = 1)
        val line = PossessionSimEngine.formatPossessionLog(
            used = 11,
            clockRemaining = 720,
            offense = "KU",
            before = before,
            after = after,
            shotClockBefore = 7,
            shotClockAfter = PossessionShotClock.resetAfterOffensiveRebound(clock),
            change = PossessionChange.OffenseRetainsOffensiveRebound,
        )
        assertTrue(line.contains("OREB +1"))
        assertTrue(line.contains("SC 7→20"))
        assertTrue(line.contains("retain possession"))
    }

    @Test
    fun playerRecorder_offensiveRebound_incrementsOreBStat() {
        val ledger = PlayerBoxScoreLedger(listOf("p1", "p2"))
        val before = TeamBoxScore(teamId = "home")
        val after = before.copy(fieldGoalsAttempted = 1)
        PlayerPossessionStatRecorder.record(
            offenseLedger = ledger,
            defenseLedger = PlayerBoxScoreLedger(listOf("d1")),
            offenseFloor = listOf("p1", "p2"),
            defenseFloor = listOf("d1"),
            usageWeights = mapOf("p1" to 50, "p2" to 50),
            before = before,
            after = after,
            change = PossessionChange.OffenseRetainsOffensiveRebound,
            turnoverTerminal = null,
            foulStoppage = false,
            random = Random(3),
        )
        val totalOreB = ledger.snapshot().sumOf { it.offensiveRebounds }
        assertEquals(1, totalOreB)
    }

    @Test
    fun fullSim_producesOffensiveReboundPossessionLogs() {
        val tuning = SimTuning(
            simTuningId = "test",
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
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 70, 70),
                away = GameSimTeamInput("a", 70, 70),
                tuning = tuning,
                seed = 4242L,
            ),
        )
        val orebLogs = result.possessionLog.filter { it.contains("OREB +1") }
        assertTrue(
            "expected at least one offensive rebound trip in sim log",
            orebLogs.isNotEmpty(),
        )
        assertTrue(orebLogs.any { it.contains("SC") && it.contains("→20") })
    }
}

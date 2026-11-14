package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.ClutchUsagePath
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimClutchUsageWeights
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClutchUsagePolicyTest {
    private val tuning = SimClutchUsageWeights()

    @Test
    fun megaSim115_qaAnchor() {
        assertEquals(ClutchUsagePath.SIM_ID, ClutchUsagePolicy.QA_ID)
    }

    @Test
    fun effectiveUsageWeights_boostsPeakPlayerInClutch() {
        val base = mapOf("a" to 25, "b" to 35, "c" to 40)
        val bumped = ClutchUsagePolicy.effectiveUsageWeights(
            baseWeights = base,
            offenseFloor = listOf("a", "b", "c"),
            gameClockSeconds = 120,
            offenseScore = 70,
            defenseScore = 68,
            tuning = tuning,
        )
        assertTrue(bumped["c"]!! > base["c"]!!)
        assertEquals(base["a"], bumped["a"])
        assertTrue(ClutchUsagePolicy.peakShareAfterBump(bumped) > ClutchUsagePolicy.peakShareAfterBump(base))
    }

    @Test
    fun outsideClutchWindow_weightsUnchanged() {
        val base = mapOf("a" to 30, "b" to 70)
        val same = ClutchUsagePolicy.effectiveUsageWeights(
            baseWeights = base,
            offenseFloor = listOf("a", "b"),
            gameClockSeconds = 600,
            offenseScore = 70,
            defenseScore = 68,
            tuning = tuning,
        )
        assertEquals(base, same)
    }

    @Test
    fun clutchGameplan_increasesPeakPlayerFieldGoalAttempts() {
        val simTuning = SimTuning(
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
        val roster = (1..6).map { i ->
            RosterPlayer(
                playerId = "p$i",
                dynastyId = "d",
                position = PlayerPosition.entries[i % 5],
                classYear = PlayerClassYear.Junior,
                isStarter = i <= 5,
                depthOrder = i,
                inTransferPortal = false,
                off = 72,
                def = 70,
            )
        }
        val homeRoster = GameLineupPrep.build("home", roster)
        val resume = MidGameState(
            random = kotlin.random.Random(2),
            gameClockSeconds = 150,
            offenseHome = true,
            homeBox = TeamBoxScore(teamId = "home", points = 71),
            awayBox = TeamBoxScore(teamId = "away", points = 70),
            possessionLog = emptyList(),
            totalPossessions = 70,
        )
        val usage = mapOf(
            "p1" to 10,
            "p2" to 15,
            "p3" to 55,
            "p4" to 10,
            "p5" to 10,
        )
        fun peakFga(seed: Long, withClutchBump: Boolean): Int {
            val result = PossessionSimEngine.simulate(
                GameSimInput(
                    home = GameSimTeamInput("home", 75, 75),
                    away = GameSimTeamInput("away", 75, 75),
                    tuning = simTuning,
                    seed = seed,
                    homeRoster = homeRoster,
                    homeGameplan = GameplanInput(usageWeights = if (withClutchBump) usage else emptyMap()),
                ),
                SimulateOptions(resume = resume.copy(random = kotlin.random.Random(seed))),
            )
            return result.homePlayerBox.first { it.playerId == "p3" }.fieldGoalsAttempted
        }
        var withPlan = 0
        var without = 0
        repeat(20) { i ->
            val seed = 5000L + i
            withPlan += peakFga(seed, withClutchBump = true)
            without += peakFga(seed, withClutchBump = false)
        }
        assertTrue(withPlan >= without)
    }
}

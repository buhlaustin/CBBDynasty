package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.IntentionalFoulLateGamePath
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.LateGameFoulStrategy
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentionalFoulLateGamePolicyTest {
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
    fun megaSim106_qaAnchor() {
        assertEquals(IntentionalFoulLateGamePath.SIM_ID, IntentionalFoulLateGamePolicy.QA_ID)
    }

    @Test
    fun resolve_doesNotAddFieldGoalAttempt() {
        val box = TeamBoxScore(teamId = "h", fieldGoalsAttempted = 3)
        val result = IntentionalFoulLateGamePolicy.resolvePossession(box)
        assertEquals(3, result.box.fieldGoalsAttempted)
        assertEquals(1, result.box.possessions)
    }

    @Test
    fun foulEarlyLateGame_canLogIntentionalFoul() {
        fun roster() = listOf(
            RosterPlayer(
                playerId = "pg",
                dynastyId = "d",
                position = PlayerPosition.PointGuard,
                classYear = PlayerClassYear.Senior,
                isStarter = true,
                depthOrder = 1,
                inTransferPortal = false,
                ovr = 75,
                off = 75,
                def = 70,
            ),
            RosterPlayer(
                playerId = "sg",
                dynastyId = "d",
                position = PlayerPosition.ShootingGuard,
                classYear = PlayerClassYear.Senior,
                isStarter = true,
                depthOrder = 2,
                inTransferPortal = false,
                ovr = 74,
                off = 74,
                def = 70,
            ),
            RosterPlayer(
                playerId = "sf",
                dynastyId = "d",
                position = PlayerPosition.SmallForward,
                classYear = PlayerClassYear.Junior,
                isStarter = true,
                depthOrder = 3,
                inTransferPortal = false,
                ovr = 73,
                off = 73,
                def = 70,
            ),
            RosterPlayer(
                playerId = "pf",
                dynastyId = "d",
                position = PlayerPosition.PowerForward,
                classYear = PlayerClassYear.Junior,
                isStarter = true,
                depthOrder = 4,
                inTransferPortal = false,
                ovr = 72,
                off = 72,
                def = 70,
            ),
            RosterPlayer(
                playerId = "c",
                dynastyId = "d",
                position = PlayerPosition.Center,
                classYear = PlayerClassYear.Sophomore,
                isStarter = true,
                depthOrder = 5,
                inTransferPortal = false,
                ovr = 71,
                off = 71,
                def = 70,
            ),
        )
        val homePrep = GameLineupPrep.build("h", roster())
        val awayPrep = GameLineupPrep.build("a", roster().map { it.copy(playerId = "a-${it.playerId}") })
        val resume = MidGameState(
            random = kotlin.random.Random(77),
            gameClockSeconds = 90,
            offenseHome = true,
            homeBox = TeamBoxScore(teamId = "h", points = 72),
            awayBox = TeamBoxScore(teamId = "a", points = 65),
            possessionLog = emptyList(),
            totalPossessions = 50,
        )
        val input = GameSimInput(
            home = GameSimTeamInput("h", 70, 70),
            away = GameSimTeamInput("a", 70, 70),
            tuning = tuning,
            seed = 77L,
            homeRoster = homePrep,
            awayRoster = awayPrep,
            awayGameplan = GameplanInput(lateGameFoulStrategy = LateGameFoulStrategy.FoulEarly),
        )
        var sawIntentional = false
        for (offset in 0 until 40) {
            if (sawIntentional) break
            val result = PossessionSimEngine.simulate(
                input,
                SimulateOptions(resume = resume.copy(random = kotlin.random.Random(800L + offset))),
            )
            sawIntentional = result.possessionLog.any { line ->
                line.contains("intentional foul stop", ignoreCase = true) ||
                    line.contains("intentional foul |", ignoreCase = true)
            }
        }
        assertTrue(sawIntentional)
    }
}

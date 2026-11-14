package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionTerminalDiversityTest {
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
    fun fullGamesAverageAtLeastEightDistinctTerminalLabels() {
        val positions = PlayerPosition.STARTER_ORDER + listOf(PlayerPosition.ShootingGuard, PlayerPosition.SmallForward, PlayerPosition.PowerForward)
        val roster = (1..8).map { i ->
            RosterPlayer(
                playerId = "h$i",
                dynastyId = "d",
                position = positions[i - 1],
                classYear = PlayerClassYear.Junior,
                isStarter = i <= 5,
                depthOrder = i,
                inTransferPortal = false,
                off = 70 + (i % 5),
                def = 68 + (i % 4),
            )
        }
        val homeRoster = GameLineupPrep.build("home", roster)
        val awayRoster = GameLineupPrep.build("away", roster.map { it.copy(playerId = "a${it.playerId}") })
        var totalDistinct = 0
        repeat(6) { gameIndex ->
            val result = PossessionSimEngine.simulate(
                GameSimInput(
                    home = GameSimTeamInput("home", 82, 78),
                    away = GameSimTeamInput("away", 77, 76),
                    tuning = tuning,
                    seed = 18_000L + gameIndex,
                    homeRoster = homeRoster,
                    awayRoster = awayRoster,
                ),
            )
            totalDistinct += PossessionTerminalSignature.distinctTerminals(result.possessionLog).size
        }
        val average = totalDistinct / 6.0
        assertTrue("average distinct terminals=$average", average >= 8.0)
    }
}

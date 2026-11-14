package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerBoxScoreCoherenceTest {
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
    fun fullGamePopulatesAllPlayerBoxColumns() {
        val roster = listOf(
            p("h1", PlayerPosition.PointGuard),
            p("h2", PlayerPosition.ShootingGuard),
            p("h3", PlayerPosition.SmallForward),
            p("h4", PlayerPosition.PowerForward),
            p("h5", PlayerPosition.Center),
            p("h6", PlayerPosition.ShootingGuard, 6, false),
        )
        val homeRoster = GameLineupPrep.build("home", roster)
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("home", 82, 78),
                away = GameSimTeamInput("away", 77, 76),
                tuning = tuning,
                seed = 90210L,
                homeRoster = homeRoster,
            ),
        )
        assertTrue(result.homePlayerBox.isNotEmpty())
        val totalMin = result.homePlayerBox.sumOf { it.minutesPlayed() }
        assertTrue(totalMin >= 150)
        assertTrue(result.homePlayerBox.any { it.minutesPlayed() > 0 })
        result.homePlayerBox.forEach { line ->
            assertTrue(line.fieldGoalsMade <= line.fieldGoalsAttempted)
            assertTrue(line.freeThrowsMade <= line.freeThrowsAttempted)
        }
        val scored = result.homePlayerBox.filter { it.points > 0 || it.turnovers > 0 || it.rebounds > 0 }
        assertTrue(scored.isNotEmpty())
    }

    private fun p(
        id: String,
        pos: PlayerPosition,
        depth: Int = 1,
        starter: Boolean = depth <= 5,
    ) = RosterPlayer(
        playerId = id,
        dynastyId = "d",
        position = pos,
        classYear = PlayerClassYear.Junior,
        isStarter = starter,
        depthOrder = depth,
        inTransferPortal = false,
        off = 72,
        def = 70,
    )
}

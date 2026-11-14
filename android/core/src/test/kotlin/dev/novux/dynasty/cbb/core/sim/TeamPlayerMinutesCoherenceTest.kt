package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.qa.TeamPlayerMinutesPath
import dev.novux.dynasty.cbb.core.roster.PlayerClassYear
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

class TeamPlayerMinutesCoherenceTest {
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
    fun megaSim109_qaAnchor() {
        assertEquals(TeamPlayerMinutesPath.SIM_ID, TeamPlayerMinutesCoherence.QA_ID)
    }

    @Test
    fun regulationRosterGame_sumsTwoHundredPlayerMinutesPerTeam() {
        val roster = (1..8).map { i ->
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
        val awayRoster = GameLineupPrep.build("away", roster.map { it.copy(playerId = "a-${it.playerId}") })
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("home", 78, 76),
                away = GameSimTeamInput("away", 77, 75),
                tuning = tuning,
                seed = 4242L,
                homeRoster = homeRoster,
                awayRoster = awayRoster,
            ),
        )
        BoxScoreCoherence.assertValid(result)
        assertEquals(
            RotationMinutesAllocator.TEAM_MINUTES_REGULATION * 60,
            TeamPlayerMinutesCoherence.expectedTeamFloorSeconds(result),
        )
        assertEquals(12_000, TeamPlayerMinutesCoherence.teamFloorSeconds(result.homePlayerBox))
        assertEquals(12_000, TeamPlayerMinutesCoherence.teamFloorSeconds(result.awayPlayerBox))
    }
}

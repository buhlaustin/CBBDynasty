package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.sim.GameSimTeamInput
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserLeagueGameSimulatorTest {
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
    fun fastSim_stagesPossessionEngineMarkWithBoxArchive() {
        val state = WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason)
        val gameId = UserLeagueGameIds.forPosition(state.activePosition)
        val bundle = UserLeagueFastSimBundle(
            listOf(
                UserLeagueGameSimContext(
                    gameId = gameId,
                    userTeamIsHome = true,
                    tuning = tuning,
                    seed = 77L,
                    home = GameSimTeamInput("home", 80, 78),
                    away = GameSimTeamInput("away", 76, 75),
                ),
            ),
        )
        val (after, outcomes) = UserLeagueGameSimulator.fastSimUnplayed(state, bundle, unplayedCount = 1)
        assertEquals(1, outcomes.size)
        val mark = after.provisionalGames[gameId]
        assertNotNull(mark)
        assertTrue(mark!!.possessionEngine)
        assertNotNull(mark.boxArchive)
        assertEquals(mark.homeScore, mark.boxArchive!!.homeBox.points)
        assertEquals(mark.awayScore, mark.boxArchive.awayBox.points)
    }
}

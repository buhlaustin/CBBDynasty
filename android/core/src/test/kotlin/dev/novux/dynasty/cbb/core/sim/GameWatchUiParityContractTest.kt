package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * UI contract: Sim to end must match a full watched run at 0× delay for the same seed
 * ([MEGA-SIM-127], [planning/simulation-engine.md#playback-watch-speed-sim-to-end]).
 */
class GameWatchUiParityContractTest {
    private val tuning = SimTuning(
        simTuningId = "parity",
        schemaVersion = 1,
        possessionsPerGameTarget = IntMinMax(60, 70),
        shotClock = SimShotClock(30, 20),
        timeUsedSeconds = SimTimeUsedSeconds(
            quickTurnover = listOf(3, 6),
            quickShot = listOf(8, 14),
            foulStoppage = listOf(12, 18),
        ),
        eventWeights = JsonObject(emptyMap()),
        homeNeutralModifiers = SimVenueModifiers(1.0, 1.0, 1.0),
    )

    @Test
    fun watchAtZeroSpeedMatchesSimToEndAcrossSeeds() = runBlocking {
        listOf(11L, 33L, 88L, 202L).forEach { seed ->
            val input = GameSimInput(
                home = GameSimTeamInput("Home", 79, 74),
                away = GameSimTeamInput("Away", 76, 75),
                tuning = tuning,
                seed = seed,
            )
            val watched = GameWatchPlayback.watch(
                input = input,
                speedMultiplier = 0.0,
                onLine = { },
                onHalftime = { _, _ -> input },
            )
            val instant = GameWatchPlayback.simToEnd(input)
            assertEquals("homeScore seed=$seed", instant.homeScore, watched.homeScore)
            assertEquals("awayScore seed=$seed", instant.awayScore, watched.awayScore)
            assertEquals("possessions seed=$seed", instant.totalPossessions, watched.totalPossessions)
            assertEquals("possessionLog seed=$seed", instant.possessionLog, watched.possessionLog)
        }
    }
}

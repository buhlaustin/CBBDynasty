package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameWatchPlaybackTest {
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
    fun defaultWatchSpeedYieldsTwoSecondLineDelay() {
        assertEquals(2_000L, GameWatchPlayback.lineDelayMs(GameWatchPlayback.DEFAULT_WATCH_SPEED_MULTIPLIER))
    }

    @Test
    fun userSpeedPresetDelays() {
        GameWatchSpeedPresets.SPEED_MULTIPLIERS.forEach { mult ->
            assertTrue(GameWatchSpeedPresets.isUserMultiplier(mult))
        }
        assertEquals(4_000L, GameWatchPlayback.lineDelayMs(0.5))
        assertEquals(2_000L, GameWatchPlayback.lineDelayMs(1.0))
        assertEquals(1_000L, GameWatchPlayback.lineDelayMs(2.0))
        assertEquals(500L, GameWatchPlayback.lineDelayMs(4.0))
        assertEquals(0L, GameWatchPlayback.lineDelayMs(0.0))
    }

    @Test
    fun watchStreamsLinesAndMatchesSimToEnd() = runBlocking {
        val input = GameSimInput(
            home = GameSimTeamInput("h", 80, 78),
            away = GameSimTeamInput("a", 77, 76),
            tuning = tuning,
            seed = 33L,
        )
        val lines = mutableListOf<String>()
        var halftimeCalls = 0
        val watched = GameWatchPlayback.watch(
            input = input,
            speedMultiplier = 0.0,
            onLine = { lines += it.logLine },
            onHalftime = { _, _ ->
                halftimeCalls++
                input
            },
        )
        val instant = GameWatchPlayback.simToEnd(input)
        assertEquals(instant.homeScore, watched.homeScore)
        assertEquals(instant.awayScore, watched.awayScore)
        assertEquals(instant.possessionLog.size, watched.possessionLog.size)
        assertEquals(1, halftimeCalls)
        assertTrue(lines.isNotEmpty())
    }

    @Test
    fun watchStepsExposeChromeWithShotClock() = runBlocking {
        val input = GameSimInput(
            home = GameSimTeamInput("h", 80, 78),
            away = GameSimTeamInput("a", 77, 76),
            tuning = tuning,
            seed = 9L,
        )
        var chrome: GameWatchChrome? = null
        GameWatchPlayback.watch(
            input = input,
            speedMultiplier = 0.0,
            onLine = {
                chrome = it.chrome
            },
            onHalftime = { _, _ -> input },
        )
        assertEquals(true, chrome != null)
        assertTrue(chrome!!.shotClockSeconds in 0..30)
        assertTrue(linesContainScMarker(input))
    }

    private fun linesContainScMarker(input: GameSimInput): Boolean =
        GameWatchPlayback.simToEnd(input).possessionLog.any { "| SC " in it }

    @Test
    fun watchPauseGatePreservesSessionUntilResume() = runBlocking {
        val input = GameSimInput(
            home = GameSimTeamInput("h", 80, 78),
            away = GameSimTeamInput("a", 77, 76),
            tuning = tuning,
            seed = 44L,
        )
        val gate = GameWatchPauseGate()
        gate.pause()
        val lines = mutableListOf<String>()
        val watchJob = async {
            GameWatchPlayback.watch(
                input = input,
                speedMultiplier = 0.0,
                pauseGate = gate,
                onLine = { lines += it.logLine },
                onHalftime = { _, _ -> input },
            )
        }
        delay(50)
        assertEquals(1, lines.size)
        gate.resume()
        val result = watchJob.await()
        val instant = GameWatchPlayback.simToEnd(input)
        assertEquals(instant.homeScore, result.homeScore)
        assertEquals(instant.awayScore, result.awayScore)
        assertTrue(lines.size > 1)
    }
}

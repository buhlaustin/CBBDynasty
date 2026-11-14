package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionShootingFoulPath
import dev.novux.dynasty.cbb.core.sim.tuning.IntMinMax
import dev.novux.dynasty.cbb.core.sim.tuning.SimShotClock
import dev.novux.dynasty.cbb.core.sim.tuning.SimTimeUsedSeconds
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.sim.tuning.SimVenueModifiers
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import kotlin.random.Random
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionShootingFoulPolicyTest {
    @Test
    fun megaSim094_shootingFoulAccumulatesFtaFtm() {
        assertEquals(PossessionShootingFoulPath.SIM_ID, "MEGA-SIM-094")
        val tuning = SimTuning(
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
        val input = GameSimInput(
            home = GameSimTeamInput("home", 78, 74),
            away = GameSimTeamInput("away", 76, 75),
            tuning = tuning,
            seed = 94L,
        )
        var foundFt: GameSimResult? = null
        for (i in 0 until 800) {
            val result = PossessionSimEngine.simulate(input.copy(seed = 94000L + i))
            val homeFt = result.homeBox.freeThrowsAttempted + result.awayBox.freeThrowsAttempted
            if (homeFt > 0) {
                foundFt = result
                break
            }
        }
        assertNotNull(foundFt)
        val r = foundFt!!
        assertTrue(r.homeBox.freeThrowsAttempted + r.awayBox.freeThrowsAttempted > 0)
        assertTrue(
            r.possessionLog.any {
                it.contains("shooting foul", ignoreCase = true) ||
                    it.contains("and-1", ignoreCase = true) ||
                    it.contains("FT ")
            },
        )
    }

    @Test
    fun resolvePath_andOneSchedulesOneFreeThrow() {
        val effects = ResolvedSchemeEffects(
            tripPaceScale = 1.0,
            threePointAttemptRate = 0.0,
            turnoverChance = 0.001,
            makeRateAdjust = 0.5,
            offensiveRatingDelta = 0,
            defensiveRatingDelta = 0,
        )
        var andOne: PossessionResolveResult? = null
        for (seed in 0 until 2_000) {
            val result = PossessionSubPhaseEngine.resolve(
                offense = TeamBoxScore(teamId = "home"),
                offRating = 88,
                defRating = 60,
                effects = effects,
                random = Random(seed.toLong()),
            )
            if (result.andOne) {
                andOne = result
                break
            }
        }
        assertNotNull(andOne)
        assertEquals(1, andOne!!.freeThrowAttempts)
        assertTrue(andOne.subPhaseChain.orEmpty().contains("and-1"))
    }

    @Test
    fun missShootingFoulUsesTwoOrThreeAttempts() {
        val effects = ResolvedSchemeEffects(
            tripPaceScale = 1.0,
            threePointAttemptRate = 1.0,
            turnoverChance = 0.001,
            makeRateAdjust = -0.5,
            offensiveRatingDelta = 0,
            defensiveRatingDelta = 0,
        )
        var foul: PossessionResolveResult? = null
        for (seed in 0 until 3_000) {
            val result = PossessionSubPhaseEngine.resolve(
                offense = TeamBoxScore(teamId = "home"),
                offRating = 55,
                defRating = 60,
                effects = effects,
                random = Random(seed.toLong()),
            )
            if (result.shootingFoul && !result.andOne) {
                foul = result
                break
            }
        }
        assertNotNull(foul)
        assertTrue(foul!!.freeThrowAttempts in 2..3)
    }
}

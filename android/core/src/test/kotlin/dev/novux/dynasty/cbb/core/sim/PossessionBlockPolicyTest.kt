package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionBlockPath
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionBlockPolicyTest {
    @Test
    fun megaSim093_blockedShotInResolvePath() {
        assertEquals(PossessionBlockPath.SIM_ID, "MEGA-SIM-093")
        val effects = ResolvedSchemeEffects(
            tripPaceScale = 1.0,
            threePointAttemptRate = 0.1,
            turnoverChance = 0.01,
            makeRateAdjust = -0.5,
            offensiveRatingDelta = 0,
            defensiveRatingDelta = 0,
        )
        var blocked: PossessionResolveResult? = null
        for (seed in 0 until 400) {
            val result = PossessionSubPhaseEngine.resolve(
                offense = TeamBoxScore(teamId = "home"),
                offRating = 60,
                defRating = 88,
                effects = effects,
                random = Random(seed.toLong()),
            )
            if (result.blockedShot) {
                blocked = result
                break
            }
        }
        assertNotNull(blocked)
        assertEquals(PossessionChange.DefenseTakesBall, blocked!!.change)
    }

    @Test
    fun formatLog_blockedShotVocabulary() {
        val before = TeamBoxScore(teamId = "KU")
        val after = before.copy(fieldGoalsAttempted = 1)
        val line = PossessionSimEngine.formatPossessionLog(
            used = 12,
            clockRemaining = 500,
            offense = "KU",
            before = before,
            after = after,
            shotClockBefore = 18,
            shotClockAfter = 30,
            change = PossessionChange.DefenseTakesBall,
            blockedShot = true,
            foulOnBlock = true,
        )
        assertTrue(line.contains("BLK"))
        assertTrue(line.contains("foul on block"))
    }

    @Test
    fun playerRecorder_creditsBlockOnBlockedShot() {
        val offenseLedger = PlayerBoxScoreLedger(listOf("o1"))
        val defenseLedger = PlayerBoxScoreLedger(listOf("d1"))
        val before = TeamBoxScore(teamId = "home")
        val after = before.copy(fieldGoalsAttempted = 1)
        val _credits = PlayerPossessionStatRecorder.record(
            offenseLedger = offenseLedger,
            defenseLedger = defenseLedger,
            offenseFloor = listOf("o1"),
            defenseFloor = listOf("d1"),
            usageWeights = mapOf("o1" to 100),
            before = before,
            after = after,
            change = PossessionChange.DefenseTakesBall,
            turnoverTerminal = null,
            foulStoppage = false,
            blockedShot = true,
            random = Random(2),
        )
        assertEquals(1, defenseLedger.snapshot().single().blocks)
    }
}

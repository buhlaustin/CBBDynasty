package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionDefensiveReboundLogPath
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-SIM-091] defensive rebound logs change of possession. */
class DefensiveReboundPossessionTest {
    @Test
    fun megaSim091_formatLog_includesDrebAndChangePossession() {
        assertEquals(PossessionDefensiveReboundLogPath.SIM_ID, "MEGA-SIM-091")
        val before = TeamBoxScore(teamId = "KU")
        val after = before.copy(fieldGoalsAttempted = 1)
        val line = PossessionSimEngine.formatPossessionLog(
            used = 14,
            clockRemaining = 612,
            offense = "KU",
            before = before,
            after = after,
            shotClockBefore = 30,
            shotClockAfter = 30,
            change = PossessionChange.DefenseTakesBall,
        )
        assertTrue(line.contains("DREB"))
        assertTrue(line.contains("change possession"))
    }

    @Test
    fun missRebound_defensivePath_changesPossession() {
        val afterMiss = TeamBoxScore(teamId = "away", possessions = 1, fieldGoalsAttempted = 1)
        var dreb: PossessionResolveResult? = null
        for (seed in 0 until 500) {
            val result = PossessionSimEngine.resolveMissRebound(afterMiss, Random(seed.toLong()))
            if (result.change == PossessionChange.DefenseTakesBall) {
                dreb = result
                break
            }
        }
        assertNotNull(dreb)
    }
}

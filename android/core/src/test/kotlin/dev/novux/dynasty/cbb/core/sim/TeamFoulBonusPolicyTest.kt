package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.TeamFoulBonusPath
import dev.novux.dynasty.cbb.core.sim.tuning.SimTeamFoulRules
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamFoulBonusPolicyTest {
    private val ncaaRules = SimTeamFoulRules(
        bonusThreshold = 7,
        doubleBonusThreshold = 10,
        oneAndOneUntilDouble = true,
    )

    @Test
    fun megaSim095_qaAnchor() {
        assertEquals(TeamFoulBonusPath.SIM_ID, TeamFoulBonusPolicy.QA_ID)
    }

    @Test
    fun belowBonus_noAutomaticTrip() {
        assertNull(
            TeamFoulBonusPolicy.sampleNonShootingFreeThrows(6, ncaaRules, Random(1)),
        )
    }

    @Test
    fun atBonus_oneAndOneOrTwoAttempts() {
        val samples = (0 until 50).map { seed ->
            TeamFoulBonusPolicy.sampleNonShootingFreeThrows(7, ncaaRules, Random(seed.toLong()))
        }.filterNotNull()
        assertTrue(samples.isNotEmpty())
        assertTrue(samples.all { (_, attempts) -> attempts in 1..2 })
    }

    @Test
    fun doubleBonus_alwaysTwoAttempts() {
        repeat(20) { seed ->
            val ft = TeamFoulBonusPolicy.sampleNonShootingFreeThrows(10, ncaaRules, Random(seed.toLong()))!!
            assertEquals(2, ft.second)
        }
    }

    @Test
    fun halfLedger_resets() {
        val ledger = TeamFoulHalfLedger()
        assertEquals(1, ledger.recordDefensiveFoul(defenseIsHome = true))
        assertEquals(1, ledger.recordDefensiveFoul(defenseIsHome = false))
        ledger.reset()
        assertEquals(0, ledger.homeFoulsInHalf)
        assertEquals(0, ledger.awayFoulsInHalf)
    }
}

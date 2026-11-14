package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionTurnoverFlavorPath
import dev.novux.dynasty.cbb.core.strategy.ResolvedSchemeEffects
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PossessionTurnoverFlavorPolicyTest {
    @Test
    fun megaSim092_logLabelsForEachFlavor() {
        assertEquals(PossessionTurnoverFlavorPath.SIM_ID, "MEGA-SIM-092")
        assertEquals("TO steal", PossessionTurnoverFlavorPolicy.logLabel(TurnoverTerminal.Steal))
        assertEquals("TO bad pass", PossessionTurnoverFlavorPolicy.logLabel(TurnoverTerminal.BadPass))
        assertEquals("TO travel", PossessionTurnoverFlavorPolicy.logLabel(TurnoverTerminal.Travel))
        assertEquals("TO offensive foul", PossessionTurnoverFlavorPolicy.logLabel(TurnoverTerminal.OffensiveFoul))
    }

    @Test
    fun subPhaseTurnovers_emitDistinctTerminals() {
        val effects = ResolvedSchemeEffects(
            tripPaceScale = 1.0,
            threePointAttemptRate = 0.38,
            turnoverChance = 0.95,
            makeRateAdjust = 0.0,
            offensiveRatingDelta = 0,
            defensiveRatingDelta = 0,
        )
        val flavors = mutableSetOf<TurnoverTerminal>()
        for (seed in 0 until 200) {
            val result = PossessionSubPhaseEngine.resolve(
                offense = TeamBoxScore(teamId = "home"),
                offRating = 70,
                defRating = 72,
                effects = effects,
                random = Random(seed.toLong()),
            )
            if (result.box.turnovers > 0) {
                result.turnoverTerminal?.let { flavors.add(it) }
            }
        }
        assertTrue(flavors.contains(TurnoverTerminal.BadPass))
        assertNotEquals(1, flavors.size)
    }

    @Test
    fun formatPossessionLog_usesFlavorLabel() {
        val before = TeamBoxScore(teamId = "KU")
        val after = before.copy(turnovers = 1)
        val line = PossessionSimEngine.formatPossessionLog(
            used = 5,
            clockRemaining = 800,
            offense = "KU",
            before = before,
            after = after,
            shotClockBefore = 30,
            shotClockAfter = 30,
            change = PossessionChange.DefenseTakesBall,
            turnoverTerminal = TurnoverTerminal.Steal,
        )
        assertTrue(line.contains("TO steal"))
    }
}

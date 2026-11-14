package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.qa.PossessionPeriodEndPath
import dev.novux.dynasty.cbb.core.sim.tuning.SimPeriodEndRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import kotlin.io.path.readText
import org.junit.Test

class PossessionPeriodEndPolicyTest {
    @Test
    fun megaSim100_qaAnchor() {
        assertEquals(PossessionPeriodEndPath.SIM_ID, PossessionPeriodEndPolicy.QA_ID)
    }

    @Test
    fun madeBasketAtGameEnd_countsByDefault() {
        val before = TeamBoxScore(teamId = "h")
        val after = before.copy(points = 2, fieldGoalsMade = 1, fieldGoalsAttempted = 1)
        val adjusted = PossessionPeriodEndPolicy.applyScoringRules(
            before = before,
            after = after,
            rules = SimPeriodEndRules(),
            clockBeforeTrip = 3,
            clockAfterTrip = 0,
            inOvertime = false,
        )
        assertEquals(2, adjusted.points)
        val tag = PossessionPeriodEndPolicy.logTag(
            PossessionPeriodEndPolicy.context(before, adjusted, 3, 0, false),
        )
        assertEquals("0:00 release: +2 counts", tag)
    }

    @Test
    fun madeBasketAtGameEnd_canBeVoidedByTuning() {
        val before = TeamBoxScore(teamId = "h")
        val after = before.copy(points = 3, fieldGoalsMade = 1, fieldGoalsAttempted = 1, threePointsMade = 1, threePointsAttempted = 1)
        val adjusted = PossessionPeriodEndPolicy.applyScoringRules(
            before = before,
            after = after,
            rules = SimPeriodEndRules(countMadeBasketAtPeriodEnd = false),
            clockBeforeTrip = 2,
            clockAfterTrip = 0,
            inOvertime = false,
        )
        assertEquals(0, adjusted.points)
        assertEquals(0, adjusted.fieldGoalsMade)
    }

    @Test
    fun fullSim_emitsPeriodEndTagEventually() {
        val root = generateSequence(java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath()) { it.parent }
            .first { it.resolve("assets/sim/sim-tuning.json").toFile().exists() }
        val raw = root.resolve("assets/sim/sim-tuning.json").readText()
        val tuning = (dev.novux.dynasty.cbb.core.sim.tuning.SimTuningCodec.parseAndValidate(raw)
            as dev.novux.dynasty.cbb.core.sim.tuning.SimTuningLoadResult.Success).tuning
        val result = PossessionSimEngine.simulate(
            GameSimInput(
                home = GameSimTeamInput("h", 82, 78),
                away = GameSimTeamInput("a", 78, 76),
                tuning = tuning,
                seed = 100L,
            ),
        )
        assertNotNull(
            result.possessionLog.firstOrNull {
                it.contains("0:00 release") || it.contains("half buzzer")
            },
        )
    }
}

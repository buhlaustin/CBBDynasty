package dev.novux.dynasty.cbb.core.coach.contract

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadCoachPerformanceGoalsEngineTest {
    @Test
    fun buildsWinConfAndPostseasonLines() {
        val goals = HeadCoachPerformanceGoalsEngine.build(55, "State U")
        assertEquals(3, goals.lines.size)
        assertEquals(CoachPerformanceGoalKind.WinTotal, goals.lines[0].kind)
        assertEquals(CoachPerformanceGoalKind.Conference, goals.lines[1].kind)
        assertEquals(CoachPerformanceGoalKind.Postseason, goals.lines[2].kind)
        assertTrue(goals.displayLines()[0].contains("Win total"))
    }

    @Test
    fun higherPrestigeRaisesWinFloor() {
        val low = HeadCoachPerformanceGoalsEngine.winTotalFloor(20)
        val high = HeadCoachPerformanceGoalsEngine.winTotalFloor(85)
        assertTrue(high > low)
    }
}

package dev.novux.dynasty.cbb.core.sim

import org.junit.Assert.assertEquals
import org.junit.Test

class TeamBoxScoreDisplayTest {
    @Test
    fun aggregatesShootingAndReboundsFromPlayers() {
        val box = TeamBoxScore(
            teamId = "h",
            points = 42,
            fieldGoalsMade = 16,
            fieldGoalsAttempted = 32,
            threePointsMade = 4,
            threePointsAttempted = 12,
            freeThrowsMade = 6,
            freeThrowsAttempted = 8,
            turnovers = 9,
            personalFouls = 11,
            possessions = 40,
        )
        val players = listOf(
            PlayerBoxScore(
                playerId = "a",
                minutesSeconds = 600,
                offensiveRebounds = 2,
                defensiveRebounds = 5,
            ),
            PlayerBoxScore(
                playerId = "b",
                minutesSeconds = 600,
                offensiveRebounds = 1,
                defensiveRebounds = 3,
            ),
        )
        val display = TeamBoxScoreDisplay.from(box, players)
        assertEquals(50, display.fieldGoalPct)
        assertEquals(33, display.threePointPct)
        assertEquals(75, display.freeThrowPct)
        assertEquals(3, display.offensiveRebounds)
        assertEquals(8, display.defensiveRebounds)
        assertEquals(11, display.box.personalFouls)
        assertEquals(9, display.box.turnovers)
    }
}

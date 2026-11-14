package dev.novux.dynasty.cbb.core.sim

import org.junit.Assert.assertEquals
import org.junit.Test

/** Smoke check that postgame UI can bind full player columns (MEGA-SIM-130). */
class PostgameBoxScoreModelTest {
    @Test
    fun playerBoxExposesStandardColumns() {
        val line = PlayerBoxScore(
            playerId = "p1",
            minutesSeconds = 1234,
            points = 18,
            fieldGoalsMade = 7,
            fieldGoalsAttempted = 12,
            threePointsMade = 2,
            threePointsAttempted = 5,
            freeThrowsMade = 2,
            freeThrowsAttempted = 2,
            offensiveRebounds = 1,
            defensiveRebounds = 4,
            assists = 3,
            steals = 1,
            blocks = 0,
            turnovers = 2,
            personalFouls = 3,
        )
        assertEquals(21, line.minutesPlayed())
        assertEquals(5, line.rebounds)
        assertEquals(18, line.points)
    }
}

package dev.novux.dynasty.cbb.core.sim

import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.InjurySeverity
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerBoxScoreMinutesLabelTest {
    @Test
    fun midGameInjuryZeroMinutesIsDnpInj() {
        val line = PlayerBoxScore(playerId = "p1", minutesSeconds = 0)
        val ids = PlayerBoxScoreMinutesLabel.midGameInjuredIds(
            listOf(
                GameSimInjuryEvent(
                    playerId = "p1",
                    injuryId = "ankle",
                    label = "Ankle",
                    bodyPart = "ankle",
                    severity = InjurySeverity.Minor,
                    weeksRemaining = 1,
                    occurredInGameMinute = 12,
                ),
            ),
        )
        assertEquals("DNP-INJ", PlayerBoxScoreMinutesLabel.format(line, ids))
    }

    @Test
    fun partialMinutesAfterMidGameInjury() {
        val line = PlayerBoxScore(playerId = "p1", minutesSeconds = 420)
        val ids = setOf("p1")
        assertEquals("7", PlayerBoxScoreMinutesLabel.format(line, ids))
    }
}

package dev.novux.dynasty.cbb.core.strategy

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrategyEditLockPolicyTest {
    @Test
    fun lockedWithoutScheduledGame() {
        val checkpoint = WeekCheckpointState(
            activePosition = CalendarPosition(seasonIndex = 1, phase = CalendarPhase.RegularSeason, weekIndex = 3),
        )
        assertFalse(
            StrategyEditLockPolicy.prepEditable(
                checkpoint = checkpoint,
                hasScheduledUserGame = false,
                phase = CalendarPhase.RegularSeason,
            ),
        )
    }

    @Test
    fun editableWhenActiveWeekAfterCommitted() {
        val committed = CalendarPosition(seasonIndex = 1, phase = CalendarPhase.RegularSeason, weekIndex = 2)
        val checkpoint = WeekCheckpointState(
            activePosition = CalendarPosition(seasonIndex = 1, phase = CalendarPhase.RegularSeason, weekIndex = 3),
            committedThrough = committed,
        )
        assertTrue(
            StrategyEditLockPolicy.prepEditable(
                checkpoint = checkpoint,
                hasScheduledUserGame = true,
                phase = CalendarPhase.RegularSeason,
            ),
        )
    }
}

package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.GameConstants
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegularSeasonWeekBatchPolicyTest {
    @Test
    fun weeks1Through32_areRegularSeasonBatchWeeks() {
        (1..GameConstants.MAX_REGULAR_SEASON_GAMES).forEach { week ->
            val pos = CalendarPosition(1, CalendarPhase.RegularSeason, week)
            assertTrue(RegularSeasonWeekBatchPolicy.isRegularSeasonWeek(pos))
        }
    }

    @Test
    fun exhibition_notBatchWeek() {
        val pos = CalendarPosition(1, CalendarPhase.Exhibition, 1)
        assertFalse(RegularSeasonWeekBatchPolicy.isRegularSeasonWeek(pos))
    }
}
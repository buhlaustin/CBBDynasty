package dev.novux.dynasty.cbb.core.offseason.weekten

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class OffseasonWeekAdvancePolicyTest {
    @Test
    fun seasonOneSkipsWeekTen() {
        val pos = CalendarPosition(1, CalendarPhase.Offseason, 9)
        val next = OffseasonWeekAdvancePolicy.nextPosition(pos)
        assertEquals(CalendarPhase.Preseason, next.phase)
        assertEquals(1, next.weekIndex)
    }

    @Test
    fun seasonTwoEntersWeekTen() {
        val pos = CalendarPosition(2, CalendarPhase.Offseason, 9)
        val next = OffseasonWeekAdvancePolicy.nextPosition(pos)
        assertEquals(CalendarPhase.Offseason, next.phase)
        assertEquals(10, next.weekIndex)
    }

    @Test
    fun preseasonCta_seasonOneWeekNine() {
        val pos = CalendarPosition(1, CalendarPhase.Offseason, 9)
        assertEquals(true, OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta(pos))
    }

    @Test
    fun preseasonCta_seasonOneWeekEight_isAdvanceWeek() {
        val pos = CalendarPosition(1, CalendarPhase.Offseason, 8)
        assertEquals(false, OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta(pos))
    }

    @Test
    fun preseasonCta_seasonTwoWeekTen() {
        val pos = CalendarPosition(2, CalendarPhase.Offseason, 10)
        assertEquals(true, OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta(pos))
    }

    @Test
    fun preseasonCta_seasonTwoWeekNineFalseUntilWeekTen() {
        val pos = CalendarPosition(2, CalendarPhase.Offseason, 9)
        assertEquals(false, OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta(pos))
    }
}

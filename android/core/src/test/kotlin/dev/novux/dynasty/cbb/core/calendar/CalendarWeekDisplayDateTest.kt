package dev.novux.dynasty.cbb.core.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarWeekDisplayDateTest {
    @Test
    fun regularSeasonWeek_hasMonthDayLabel() {
        val label = CalendarWeekDisplayDate.gameDateLabel(
            CalendarPosition(1, CalendarPhase.RegularSeason, 1),
        )
        assertNotNull(label)
        assertEquals("Nov 22", label)
    }

    @Test
    fun offseasonHasNoGameDate() {
        assertNull(
            CalendarWeekDisplayDate.gameDateLabel(
                CalendarPosition(1, CalendarPhase.Offseason, 3),
            ),
        )
    }

    @Test
    fun postseasonWeek_advancesIntoMarch() {
        val label = CalendarWeekDisplayDate.gameDateLabel(
            CalendarPosition(1, CalendarPhase.Postseason, 4),
        )
        assertEquals("Mar 23", label)
    }
}

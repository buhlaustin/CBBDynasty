package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecruitingHubOffseasonWeekOnePathTest {
    @Test
    fun megaRecr038_offseasonWeekOne_hubAccessible() {
        assertEquals(RecruitingHubOffseasonWeekOnePath.RECR_ID, "MEGA-RECR-038")
        assertTrue(
            RecruitingCyclePolicy.hubAccessible(
                CalendarPhase.Offseason,
                offseasonWeekIndex = 1,
            ),
        )
        assertTrue(
            RecruitingCyclePolicy.hubAccessible(CalendarPhase.Offseason, offseasonWeekIndex = null),
        )
    }
}

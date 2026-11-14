package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayPreLaunchSmokePathTest {
    @Test
    fun milestones_endAtRegularSeasonWeekOne() {
        val last = PlayPreLaunchSmokePath.MILESTONES.last()
        assertEquals("regular_season_week_1", last.id)
        assertEquals(CalendarPhase.RegularSeason, last.phase)
    }

    @Test
    fun api34_pixel6_matrixConstants() {
        assertEquals(34, PlayPreLaunchSmokePath.TARGET_API_LEVEL)
        assertEquals("pixel_6", PlayPreLaunchSmokePath.TARGET_AVD_PROFILE)
        assertTrue(PlayPreLaunchSmokePath.MILESTONES.size >= 4)
    }
}

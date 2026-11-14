package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekeight.OffseasonRosterLockConstants
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonWeekTenConstants
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants
import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class OffseasonRequiredDashboardTasksTest {
    @Test
    fun weekSix_delegatesToSigningDayTasks() {
        val tasks = OffseasonRequiredDashboardTasks.build(
            OffseasonRequiredDashboardTasks.WeekContext(
                weekIndex = SigningDayConstants.OFFSEASON_WEEK_INDEX,
                seasonIndex = 2,
                signingProgress = SigningDayProgress(
                    cycleSeasonIndex = 2,
                    rankingsViewed = false,
                    positionsConfirmed = true,
                    hardSignComplete = false,
                ),
            ),
        )
        assertEquals(DashboardTaskId.SigningDayShellLauncher, tasks.single().id)
    }

    @Test
    fun weekEight_delegatesToRosterLock() {
        val tasks = OffseasonRequiredDashboardTasks.build(
            OffseasonRequiredDashboardTasks.WeekContext(
                weekIndex = OffseasonRosterLockConstants.OFFSEASON_WEEK_INDEX,
                seasonIndex = 2,
                rosterScholarshipCount = 18,
            ),
        )
        assertEquals(DashboardTaskId.OffseasonRosterLock, tasks.single().id)
    }

    @Test
    fun inSeasonWeek_returnsEmpty() {
        val tasks = OffseasonRequiredDashboardTasks.build(
            OffseasonRequiredDashboardTasks.WeekContext(
                weekIndex = OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX,
                seasonIndex = 2,
                conferenceOffers = emptyList(),
                conferenceDecisions = emptyMap(),
            ),
        )
        assertEquals(0, tasks.size)
    }
}

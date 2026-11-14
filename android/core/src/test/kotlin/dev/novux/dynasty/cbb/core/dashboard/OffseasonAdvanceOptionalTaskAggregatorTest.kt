package dev.novux.dynasty.cbb.core.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonAdvanceOptionalTaskAggregatorTest {
    @Test
    fun collect_includesPortalTasksForWeekThree() {
        val tasks = OffseasonAdvanceOptionalTaskAggregator.collect(
            offseasonWeekIndex = 3,
            ledger = null,
            portalTop40Locked = false,
            openScholarshipSlots = 2,
            pipelineSpotlight = null,
            summerWorkoutReport = null,
            weekNineEconomy = null,
        )
        assertTrue(tasks.isNotEmpty())
    }

    @Test
    fun unresolvedCount_excludesDismissed() {
        val tasks = listOf(
            DashboardTask(
                id = DashboardTaskId.SpendRecruitingPoints,
                kind = DashboardTaskKind.Optional,
                deepLink = DashboardDeepLink.RecruitingRecommended,
            ),
            DashboardTask(
                id = DashboardTaskId.OpenScholarships,
                kind = DashboardTaskKind.Optional,
                deepLink = DashboardDeepLink.RecruitingPortal,
            ),
        )
        val count = OffseasonAdvanceOptionalTaskAggregator.unresolvedCount(
            tasks,
            dismissedKeys = setOf(DashboardTaskId.SpendRecruitingPoints.name),
        )
        assertEquals(1, count)
    }
}

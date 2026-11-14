package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonWeekSevenOptionalDashboardTasksTest {
    @Test
    fun week7_emitsOptionalReviewWhenReportReady() {
        val tasks = OffseasonWeekSevenOptionalDashboardTasks.build(
            offseasonWeekIndex = SummerWorkoutConstants.OFFSEASON_WEEK_INDEX,
            reportAvailable = true,
            coachTrainingGrade = 82,
            playerCount = 12,
        )
        assertEquals(1, tasks.size)
        assertEquals(DashboardTaskId.SummerWorkoutReportReview, tasks.single().id)
        assertEquals(DashboardTaskKind.Optional, tasks.single().kind)
        assertEquals(DashboardDeepLink.SummerWorkoutReport, tasks.single().deepLink)
    }

    @Test
    fun otherWeeks_empty() {
        assertTrue(
            OffseasonWeekSevenOptionalDashboardTasks.build(
                offseasonWeekIndex = 6,
                reportAvailable = true,
            ).isEmpty(),
        )
    }
}

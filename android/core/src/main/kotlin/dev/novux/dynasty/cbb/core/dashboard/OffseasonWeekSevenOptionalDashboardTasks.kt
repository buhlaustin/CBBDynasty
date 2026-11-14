package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutConstants

/** Optional “review summer workout report” on Week 7 dashboard ([planning/offseason.md], [MEGA-FRAME-063]). */
object OffseasonWeekSevenOptionalDashboardTasks {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-063"

    fun build(
        offseasonWeekIndex: Int,
        reportAvailable: Boolean,
        coachTrainingGrade: Int? = null,
        playerCount: Int? = null,
    ): List<DashboardTask> {
        if (offseasonWeekIndex != SummerWorkoutConstants.OFFSEASON_WEEK_INDEX || !reportAvailable) {
            return emptyList()
        }
        return listOf(
            DashboardTask(
                id = DashboardTaskId.SummerWorkoutReportReview,
                kind = DashboardTaskKind.Optional,
                deepLink = DashboardDeepLink.SummerWorkoutReport,
                formatArgs = listOfNotNull(coachTrainingGrade, playerCount),
            ),
        )
    }
}

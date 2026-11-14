package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewProgress

/**
 * End of Season required dashboard card ([planning/dashboard-tasks.md], [MEGA-FRAME-033]).
 */
object SeasonReviewRequiredDashboardTasks {
    fun build(progress: SeasonReviewProgress): List<DashboardTask> {
        if (progress.acknowledged) return emptyList()
        return listOf(
            DashboardTask(
                id = DashboardTaskId.SeasonReviewAcknowledge,
                kind = DashboardTaskKind.Required,
                deepLink = DashboardDeepLink.SeasonReviewRecap,
            ),
        )
    }

    fun canEnterOffseason(progress: SeasonReviewProgress): Boolean = progress.acknowledged
}

package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId

/** Per-task dashboard deep links ([MEGA-FRAME-026], [planning/dashboard-tasks.md#preseason]). */
object PreseasonTaskDeepLinks {
    fun deepLink(taskId: PreseasonTaskId): DashboardDeepLink =
        when (taskId) {
            PreseasonTaskId.IncomingClassSeed -> DashboardDeepLink.PreseasonTaskIncomingClassSeed
            PreseasonTaskId.PreseasonWorkouts -> DashboardDeepLink.PreseasonTaskWorkouts
            PreseasonTaskId.CoachUpgrades -> DashboardDeepLink.PreseasonTaskCoachUpgrades
            PreseasonTaskId.RankingsReview -> DashboardDeepLink.PreseasonTaskRankingsReview
            PreseasonTaskId.PreseasonReport -> DashboardDeepLink.PreseasonTaskReport
            PreseasonTaskId.TournamentInvites -> DashboardDeepLink.PreseasonTaskTournamentInvites
            PreseasonTaskId.ScheduleExhibitions -> DashboardDeepLink.PreseasonTaskScheduleExhibitions
            PreseasonTaskId.ScheduleRegularSeason -> DashboardDeepLink.PreseasonTaskScheduleRegularSeason
        }

    fun taskId(deepLink: DashboardDeepLink): PreseasonTaskId? =
        when (deepLink) {
            DashboardDeepLink.PreseasonTaskIncomingClassSeed -> PreseasonTaskId.IncomingClassSeed
            DashboardDeepLink.PreseasonTaskWorkouts -> PreseasonTaskId.PreseasonWorkouts
            DashboardDeepLink.PreseasonTaskCoachUpgrades -> PreseasonTaskId.CoachUpgrades
            DashboardDeepLink.PreseasonTaskRankingsReview -> PreseasonTaskId.RankingsReview
            DashboardDeepLink.PreseasonTaskReport -> PreseasonTaskId.PreseasonReport
            DashboardDeepLink.PreseasonTaskTournamentInvites -> PreseasonTaskId.TournamentInvites
            DashboardDeepLink.PreseasonTaskScheduleExhibitions -> PreseasonTaskId.ScheduleExhibitions
            DashboardDeepLink.PreseasonTaskScheduleRegularSeason -> PreseasonTaskId.ScheduleRegularSeason
            DashboardDeepLink.PreseasonChecklist -> null
            else -> null
        }

    enum class HubAction {
        None,
        RecruitingHsJuco,
        Strategy,
        Rankings,
    }

    fun hubAction(taskId: PreseasonTaskId): HubAction =
        when (taskId) {
            PreseasonTaskId.IncomingClassSeed -> HubAction.RecruitingHsJuco
            PreseasonTaskId.CoachUpgrades -> HubAction.Strategy
            PreseasonTaskId.RankingsReview -> HubAction.Rankings
            else -> HubAction.None
        }
}

package dev.novux.dynasty.cbb.ui.preseason

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.dashboard.DashboardTask
import dev.novux.dynasty.cbb.core.dashboard.PreseasonTaskDeepLinks
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.ui.hubs.DynastyHubMenuCallbacks

fun navigatePreseasonDashboardTask(
    task: DashboardTask,
    hubMenuCallbacks: DynastyHubMenuCallbacks,
    scrollToTask: (PreseasonTaskId) -> Unit,
    onOpenRankingsReviewSheet: () -> Unit,
) {
    val taskId = task.preseasonTaskId ?: PreseasonTaskDeepLinks.taskId(task.deepLink) ?: return
    when (PreseasonTaskDeepLinks.hubAction(taskId)) {
        PreseasonTaskDeepLinks.HubAction.RecruitingHsJuco -> hubMenuCallbacks.onOpenRecruiting()
        PreseasonTaskDeepLinks.HubAction.Strategy -> hubMenuCallbacks.onOpenStrategy()
        PreseasonTaskDeepLinks.HubAction.Rankings -> onOpenRankingsReviewSheet()
        PreseasonTaskDeepLinks.HubAction.None -> scrollToTask(taskId)
    }
}

fun navigatePreseasonDeepLink(
    deepLink: DashboardDeepLink,
    hubMenuCallbacks: DynastyHubMenuCallbacks,
    scrollToTask: (PreseasonTaskId) -> Unit,
    onOpenRankingsReviewSheet: () -> Unit,
) {
    val taskId = PreseasonTaskDeepLinks.taskId(deepLink) ?: return
    when (PreseasonTaskDeepLinks.hubAction(taskId)) {
        PreseasonTaskDeepLinks.HubAction.RecruitingHsJuco -> hubMenuCallbacks.onOpenRecruiting()
        PreseasonTaskDeepLinks.HubAction.Strategy -> hubMenuCallbacks.onOpenStrategy()
        PreseasonTaskDeepLinks.HubAction.Rankings -> onOpenRankingsReviewSheet()
        PreseasonTaskDeepLinks.HubAction.None -> scrollToTask(taskId)
    }
}

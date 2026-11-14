package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskProgress

/** Eight required preseason tasks as dashboard gate cards ([planning/dashboard-tasks.md#preseason]). */
object PreseasonRequiredDashboardTasks {
    fun build(progress: PreseasonTaskProgress): List<DashboardTask> =
        PreseasonTaskId.entries
            .filterNot { progress.isComplete(it) }
            .map { taskId ->
                DashboardTask(
                    id = DashboardTaskId.PreseasonRequired,
                    kind = DashboardTaskKind.Required,
                    deepLink = PreseasonTaskDeepLinks.deepLink(taskId),
                    stringFormatArgs = listOf(taskId.name),
                    preseasonTaskId = taskId,
                )
            }
}

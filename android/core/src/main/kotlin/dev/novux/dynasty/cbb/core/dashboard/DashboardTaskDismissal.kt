package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.calendar.CalendarPosition

/** Scoped dismiss keys so the same [DashboardTaskId] can reappear on later weeks ([MEGA-FRAME-100]). */
object DashboardTaskDismissal {
    fun key(position: CalendarPosition, task: DashboardTask): String {
        val extra = task.dismissScopeKey?.let { ":$it" }.orEmpty()
        return "${position.phase.name}:${position.weekIndex}:${task.id}$extra"
    }

    fun key(position: CalendarPosition, id: DashboardTaskId): String =
        key(
            position,
            DashboardTask(
                id = id,
                kind = DashboardTaskKind.Optional,
                deepLink = DashboardDeepLink.RecruitingRecommended,
            ),
        )
}
package dev.novux.dynasty.cbb.core.dashboard

/**
 * In-season recruiting optional cards dismiss per week via [DashboardTaskDismissal]
 * ([MEGA-RECR-047]).
 */
object InSeasonRecruitingOptionalDismissPolicy {
    const val MEGA_ID: String = "MEGA-RECR-047"

    fun dismissKey(position: dev.novux.dynasty.cbb.core.calendar.CalendarPosition, task: DashboardTask): String =
        DashboardTaskDismissal.key(position, task)
}
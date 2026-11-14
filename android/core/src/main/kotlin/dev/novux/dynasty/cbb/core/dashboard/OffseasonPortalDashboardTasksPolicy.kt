package dev.novux.dynasty.cbb.core.dashboard

/**
 * Offseason weeks 2–5 optional dashboard tasks for portal + recruiting spend
 * ([MEGA-RECR-043], [OffseasonOptionalDashboardTasks]).
 */
object OffseasonPortalDashboardTasksPolicy {
    const val MEGA_ID: String = "MEGA-RECR-043"

    val portalTaskWeeks: IntRange get() = OffseasonOptionalDashboardTasks.portalTaskWeeks
}
package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekeight.OffseasonRosterLockConstants

/** Offseason Week 8 required dashboard ([planning/dashboard-tasks.md#offseason], [MEGA-FRAME-028]). */
object OffseasonRosterLockRequiredDashboardTasks {
    fun build(rosterScholarshipCount: Int): List<DashboardTask> {
        if (rosterScholarshipCount <= OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP) {
            return emptyList()
        }
        return listOf(
            DashboardTask(
                id = DashboardTaskId.OffseasonRosterLock,
                kind = DashboardTaskKind.Required,
                deepLink = DashboardDeepLink.OffseasonRosterCuts,
                stringFormatArgs = listOf(
                    rosterScholarshipCount.toString(),
                    OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP.toString(),
                ),
            ),
        )
    }

    fun canAdvanceWeek(rosterScholarshipCount: Int): Boolean = build(rosterScholarshipCount).isEmpty()
}

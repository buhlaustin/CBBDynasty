package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.portal.PortalConstants

/** Offseason weeks 2–5 portal optional dashboard tasks ([MEGA-FRAME-061]). */
object OffseasonPortalOptionalTasksPolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-061"

    fun inPortalTaskWindow(offseasonWeekIndex: Int): Boolean =
        offseasonWeekIndex in OffseasonOptionalDashboardTasks.portalTaskWeeks

    fun signingCrunchActive(offseasonWeekIndex: Int): Boolean =
        offseasonWeekIndex in PortalConstants.PORTAL_RECRUITING_WEEKS
}

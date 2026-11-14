package dev.novux.dynasty.cbb.core.offseason

import dev.novux.dynasty.cbb.core.portal.PortalConstants

/**
 * Offseason weeks 3–5 dashboard recap strings ([MEGA-FRAME-117]).
 * UI maps [stringResSuffix] to `R.string.offseason_dashboard_week{N}_body`.
 */
object OffseasonPortalWeekDashboardCopy {
    fun bodyStringResSuffix(weekIndex: Int): String? = when (weekIndex) {
        in PortalConstants.PORTAL_RECRUITING_WEEKS -> "offseason_dashboard_week${weekIndex}_body"
        else -> null
    }
}
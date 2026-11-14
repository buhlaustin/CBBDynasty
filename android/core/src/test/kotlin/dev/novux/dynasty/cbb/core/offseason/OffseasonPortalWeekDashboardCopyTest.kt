package dev.novux.dynasty.cbb.core.offseason

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** [MEGA-FRAME-117] */
class OffseasonPortalWeekDashboardCopyTest {
    @Test
    fun portalWeeks_mapToStringSuffix() {
        assertEquals("offseason_dashboard_week3_body", OffseasonPortalWeekDashboardCopy.bodyStringResSuffix(3))
        assertEquals("offseason_dashboard_week5_body", OffseasonPortalWeekDashboardCopy.bodyStringResSuffix(5))
        assertNull(OffseasonPortalWeekDashboardCopy.bodyStringResSuffix(6))
    }
}
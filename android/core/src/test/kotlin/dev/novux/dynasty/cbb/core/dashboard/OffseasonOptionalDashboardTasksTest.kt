package dev.novux.dynasty.cbb.core.dashboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonOptionalDashboardTasksTest {
    @Test
    fun week1_hasNoOptionalTasks() {
        val tasks = OffseasonOptionalDashboardTasks.build(
            OffseasonDashboardContext(
                offseasonWeekIndex = 1,
                recruitingPointsAvailable = 500,
                portalTop40Locked = true,
                openScholarshipSlots = 2,
                signingCrunchActive = true,
            ),
        )
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun week3_optionalTasks_neverBlockAdvance() {
        val tasks = OffseasonOptionalDashboardTasks.build(
            OffseasonDashboardContext(
                offseasonWeekIndex = 3,
                recruitingPointsAvailable = 1200,
                portalTop40Locked = true,
                openScholarshipSlots = 1,
                signingCrunchActive = true,
            ),
        )
        assertTrue(tasks.isNotEmpty())
        assertFalse(DashboardAdvanceGate.blocksWeekAdvance(tasks))
        assertTrue(tasks.all { it.kind == DashboardTaskKind.Optional })
    }

    @Test
    fun week3_includesPipelineEntrantWhenSpotlightSet() {
        val tasks = OffseasonOptionalDashboardTasks.build(
            OffseasonDashboardContext(
                offseasonWeekIndex = 3,
                recruitingPointsAvailable = 0,
                portalTop40Locked = false,
                openScholarshipSlots = 0,
                signingCrunchActive = true,
                pipelineSpotlight = PipelineEntrantSpotlight(
                    entrantDisplayName = "Portal — Test U",
                    priorSchoolDisplayName = "Test U",
                ),
            ),
        )
        assertTrue(tasks.any { it.id == DashboardTaskId.PortalPipelineEntrant })
    }
}

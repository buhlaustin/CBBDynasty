package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityGrades
import dev.novux.dynasty.cbb.core.offseason.weeknine.FundraisingEventType
import dev.novux.dynasty.cbb.core.offseason.weeknine.OffseasonWeekNineConstants
import dev.novux.dynasty.cbb.core.offseason.weeknine.ProgramEconomyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonWeekNineOptionalDashboardTasksTest {
    @Test
    fun week9_listsPendingEventsNamingAndUpgrades() {
        val economy = ProgramEconomyState(
            cashUsd = 500_000,
            facilityGrades = FacilityGrades(),
        )
        val tasks = OffseasonWeekNineOptionalDashboardTasks.build(
            offseasonWeekIndex = OffseasonWeekNineConstants.OFFSEASON_WEEK_INDEX,
            economy = economy,
        )
        assertEquals(3, tasks.count { it.id in EVENT_IDS })
        assertTrue(tasks.any { it.id == DashboardTaskId.FundraisingNamingRights })
        assertTrue(tasks.any { it.id == DashboardTaskId.FundraisingFacilityUpgrades })
        assertTrue(tasks.all { it.kind == DashboardTaskKind.Optional })
    }

    @Test
    fun completedEvents_reduceOptionalCards() {
        val economy = ProgramEconomyState(
            completedEvents = setOf(FundraisingEventType.Gala),
            namingRightsSold = true,
        )
        val tasks = OffseasonWeekNineOptionalDashboardTasks.build(9, economy)
        assertEquals(2, tasks.count { it.id in EVENT_IDS })
        assertTrue(tasks.none { it.id == DashboardTaskId.FundraisingNamingRights })
    }

    private companion object {
        val EVENT_IDS = setOf(
            DashboardTaskId.FundraisingGala,
            DashboardTaskId.FundraisingAlumniGame,
            DashboardTaskId.FundraisingDonorDrive,
        )
    }
}

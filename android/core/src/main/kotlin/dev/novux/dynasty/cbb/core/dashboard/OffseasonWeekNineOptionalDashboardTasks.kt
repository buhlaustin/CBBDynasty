package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityCategory
import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityUpgradePolicy
import dev.novux.dynasty.cbb.core.offseason.weeknine.FundraisingEventType
import dev.novux.dynasty.cbb.core.offseason.weeknine.OffseasonWeekNineConstants
import dev.novux.dynasty.cbb.core.offseason.weeknine.ProgramEconomyState

/** Week 9 optional fundraising / upgrade cards ([planning/dashboard-tasks.md#offseason]). */
object OffseasonWeekNineOptionalDashboardTasks {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-065"

    fun build(
        offseasonWeekIndex: Int,
        economy: ProgramEconomyState?,
    ): List<DashboardTask> {
        if (offseasonWeekIndex != OffseasonWeekNineConstants.OFFSEASON_WEEK_INDEX || economy == null) {
            return emptyList()
        }
        val tasks = mutableListOf<DashboardTask>()
        FundraisingEventType.entries.filter { it !in economy.completedEvents }.forEach { event ->
            tasks.add(
                DashboardTask(
                    id = taskIdForEvent(event),
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.FundraisingWeekNine,
                    stringFormatArgs = listOf(event.label),
                ),
            )
        }
        if (!economy.namingRightsSold) {
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.FundraisingNamingRights,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.FundraisingWeekNine,
                ),
            )
        }
        val affordableUpgrades = FacilityCategory.entries.count { category ->
            FacilityUpgradePolicy.canAfford(economy.cashUsd, economy.facilityGrades, category)
        }
        if (affordableUpgrades > 0) {
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.FundraisingFacilityUpgrades,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.FundraisingWeekNine,
                    formatArgs = listOf(affordableUpgrades),
                ),
            )
        }
        return tasks
    }

    private fun taskIdForEvent(event: FundraisingEventType): DashboardTaskId = when (event) {
        FundraisingEventType.Gala -> DashboardTaskId.FundraisingGala
        FundraisingEventType.AlumniGame -> DashboardTaskId.FundraisingAlumniGame
        FundraisingEventType.DonorDrive -> DashboardTaskId.FundraisingDonorDrive
    }
}

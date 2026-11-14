package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.portal.PortalConstants
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy

data class OffseasonDashboardContext(
    val offseasonWeekIndex: Int,
    val recruitingPointsAvailable: Int,
    val portalTop40Locked: Boolean,
    val openScholarshipSlots: Int,
    val signingCrunchActive: Boolean,
    val pipelineSpotlight: PipelineEntrantSpotlight? = null,
)

/**
 * Offseason weeks 2–5 optional dashboard tasks — recruiting + portal
 * ([planning/dashboard-tasks.md#offseason], [planning/transfer-portal.md#ux--dashboard-optional-tasks-portal-only]).
 */
object OffseasonOptionalDashboardTasks {
    val portalTaskWeeks: IntRange = PortalConstants.PORTAL_RECRUITING_WEEKS

    fun build(context: OffseasonDashboardContext): List<DashboardTask> {
        if (context.offseasonWeekIndex !in portalTaskWeeks) return emptyList()
        val tasks = mutableListOf<DashboardTask>()
        if (context.recruitingPointsAvailable > 0) {
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.SpendRecruitingPoints,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.RecruitingRecommended,
                    formatArgs = listOf(context.recruitingPointsAvailable),
                ),
            )
        }
        if (RecruitingCyclePolicy.hubAccessible(
                dev.novux.dynasty.cbb.core.calendar.CalendarPhase.Offseason,
                context.offseasonWeekIndex,
            )
        ) {
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.PortalWindow,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.RecruitingPortal,
                    formatArgs = listOf(context.offseasonWeekIndex),
                ),
            )
        }
        if (context.offseasonWeekIndex == 2 && context.portalTop40Locked) {
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.PortalTop40Published,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.RecruitingPortal,
                ),
            )
        }
        if (context.signingCrunchActive) {
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.SigningCrunch,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.RecruitingRecommended,
                    formatArgs = listOf(context.offseasonWeekIndex),
                ),
            )
        }
        if (context.openScholarshipSlots > 0) {
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.OpenScholarships,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.RecruitingPortal,
                    formatArgs = listOf(context.openScholarshipSlots),
                ),
            )
        }
        context.pipelineSpotlight?.let { spotlight ->
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.PortalPipelineEntrant,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.RecruitingPortal,
                    stringFormatArgs = listOf(
                        spotlight.entrantDisplayName,
                        spotlight.priorSchoolDisplayName,
                    ),
                ),
            )
        }
        return tasks
    }
}

package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.portal.PortalEntrant

/**
 * Optional dashboard row "{Name} in portal" from pipeline/national spotlight
 * ([MEGA-RECR-148], [OffseasonOptionalDashboardTasks]).
 */
object PortalPipelineEntrantDashboardPolicy {
    const val MEGA_ID: String = "MEGA-RECR-148"

    fun spotlight(
        packIndex: IdentityPackIndex,
        entrants: List<PortalEntrant>,
        userPipelineStates: Set<String>,
    ): PipelineEntrantSpotlight? =
        OffseasonPortalOptionalNudgeResolver.pipelineSpotlight(
            packIndex,
            entrants,
            userPipelineStates,
        )

    fun includesPipelineEntrantTask(context: OffseasonDashboardContext): Boolean =
        buildTasks(context).any { it.id == DashboardTaskId.PortalPipelineEntrant }

    fun buildTasks(context: OffseasonDashboardContext): List<DashboardTask> =
        OffseasonOptionalDashboardTasks.build(context)
}
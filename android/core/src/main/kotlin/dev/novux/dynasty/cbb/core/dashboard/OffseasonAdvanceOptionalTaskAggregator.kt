package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weeknine.OffseasonWeekNineConstants
import dev.novux.dynasty.cbb.core.offseason.weeknine.ProgramEconomyState
import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutConstants
import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutReport
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger

/** Offseason dashboard optional tasks for sim-week confirmation ([MEGA-FRAME-068]). */
object OffseasonAdvanceOptionalTaskAggregator {
    fun collect(
        offseasonWeekIndex: Int,
        ledger: RecruitingCycleLedger?,
        portalTop40Locked: Boolean,
        openScholarshipSlots: Int,
        pipelineSpotlight: PipelineEntrantSpotlight?,
        summerWorkoutReport: SummerWorkoutReport?,
        weekNineEconomy: ProgramEconomyState?,
    ): List<DashboardTask> = buildList {
        if (OffseasonPortalOptionalTasksPolicy.inPortalTaskWindow(offseasonWeekIndex)) {
            addAll(
                OffseasonOptionalDashboardTasks.build(
                    OffseasonDashboardContext(
                        offseasonWeekIndex = offseasonWeekIndex,
                        recruitingPointsAvailable = ledger?.pointsAvailable ?: 0,
                        portalTop40Locked = portalTop40Locked,
                        openScholarshipSlots = openScholarshipSlots,
                        signingCrunchActive = OffseasonPortalOptionalTasksPolicy.signingCrunchActive(offseasonWeekIndex),
                        pipelineSpotlight = pipelineSpotlight,
                    ),
                ),
            )
        }
        if (offseasonWeekIndex == SummerWorkoutConstants.OFFSEASON_WEEK_INDEX) {
            addAll(
                OffseasonWeekSevenOptionalDashboardTasks.build(
                    offseasonWeekIndex = offseasonWeekIndex,
                    reportAvailable = summerWorkoutReport != null,
                    coachTrainingGrade = summerWorkoutReport?.coachTrainingGrade,
                    playerCount = summerWorkoutReport?.lines?.size,
                ),
            )
        }
        if (offseasonWeekIndex == OffseasonWeekNineConstants.OFFSEASON_WEEK_INDEX) {
            addAll(
                OffseasonWeekNineOptionalDashboardTasks.build(
                    offseasonWeekIndex = offseasonWeekIndex,
                    economy = weekNineEconomy,
                ),
            )
        }
    }

    fun unresolvedCount(
        tasks: List<DashboardTask>,
        dismissedKeys: Set<String>,
        dismissKey: (DashboardTask) -> String = { it.id.name },
    ): Int = tasks.count { dismissKey(it) !in dismissedKeys }
}

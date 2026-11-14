package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.discipline.PendingDisciplineIncident
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

data class DashboardTaskSnapshot(
    val required: List<DashboardTask>,
    val optional: List<DashboardTask>,
)

/**
 * Required vs optional dashboard tasks ([planning/game-frame.md#dashboard-task-gate]).
 */
object DashboardTaskGate {
    fun preseason(progress: dev.novux.dynasty.cbb.core.preseason.PreseasonTaskProgress): DashboardTaskSnapshot {
        val required = PreseasonRequiredDashboardTasks.build(progress)
        return DashboardTaskSnapshot(required = required, optional = emptyList())
    }

    fun inSeason(
        position: CalendarPosition,
        plan: PreseasonSchedulePlan?,
        exhibitionCount: Int,
        weekSimStaged: Boolean,
        disciplinePending: PendingDisciplineIncident?,
        rosterPlayers: List<RosterPlayer>,
        medicalRules: BalanceMedicalRedshirtRules,
        coachSkillPointBank: Int = 0,
        recruiting: InSeasonRecruitingDashboardContext? = null,
    ): DashboardTaskSnapshot {
        val weekGames = InSeasonWeekGames.forPosition(position, plan, exhibitionCount)
        val required = DisciplineRequiredDashboardTasks.build(disciplinePending)
        val optional = buildList {
            addAll(InSeasonOptionalDashboardTasks.build(weekGames, weekSimStaged))
            addAll(InSeasonRecruitingOptionalDashboardTasks.build(recruiting))
            addAll(
                InjuryOptionalDashboardTasks.build(
                    rosterPlayers,
                    position.weekIndex,
                    medicalRules,
                ),
            )
            addAll(CoachSundayOptionalDashboardTasks.build(coachSkillPointBank))
            addAll(PromiseAtRiskOptionalDashboardTasks.build(rosterPlayers))
        }
        return DashboardTaskSnapshot(required = required, optional = optional)
    }
}

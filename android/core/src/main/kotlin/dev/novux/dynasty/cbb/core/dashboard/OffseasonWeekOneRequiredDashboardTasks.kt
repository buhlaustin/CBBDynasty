package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.offseason.weekone.ProDayEligibility
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

/**
 * Offseason Week 1 required dashboard cards ([planning/dashboard-tasks.md#offseason], [MEGA-FRAME-029]).
 */
object OffseasonWeekOneRequiredDashboardTasks {
    fun build(players: List<RosterPlayer>): List<DashboardTask> {
        val tasks = mutableListOf<DashboardTask>()
        players.filter { it.inTransferPortal }.forEach { player ->
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.WeekOneTransferRetention,
                    kind = DashboardTaskKind.Required,
                    deepLink = DashboardDeepLink.PlayersLeavingTransfer,
                    stringFormatArgs = listOf(player.displayLabel()),
                ),
            )
        }
        players.filter { ProDayEligibility.eligible(it) && !it.proDayCompleted }.forEach { player ->
            tasks.add(
                DashboardTask(
                    id = DashboardTaskId.WeekOneProDay,
                    kind = DashboardTaskKind.Required,
                    deepLink = DashboardDeepLink.PlayersLeavingProDays,
                    stringFormatArgs = listOf(player.displayLabel()),
                ),
            )
        }
        return tasks
    }

    fun canAdvanceWeek(players: List<RosterPlayer>): Boolean = build(players).isEmpty()
}

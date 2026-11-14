package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.schedule.ScheduledGameStub

/** Optional play/sim when a user game is on the calendar this week ([planning/dashboard-tasks.md#in-season-exhibitions--postseason]). */
object InSeasonOptionalDashboardTasks {
    fun build(
        weekGames: List<ScheduledGameStub>,
        weekSimStaged: Boolean,
    ): List<DashboardTask> {
        if (weekGames.isEmpty()) return emptyList()
        if (weekSimStaged) {
            return weekGames.map { game ->
                DashboardTask(
                    id = DashboardTaskId.ReplayStagedWeekGame,
                    kind = DashboardTaskKind.Optional,
                    deepLink = DashboardDeepLink.ReplayProvisionalWeekGame,
                    stringFormatArgs = listOf(game.opponentDisplayName),
                    formatArgs = listOf(if (game.homeGame) 1 else 0),
                    dismissScopeKey = game.opponentTeamId,
                )
            }
        }
        return weekGames.map { game ->
            DashboardTask(
                id = DashboardTaskId.PlayScheduledGame,
                kind = DashboardTaskKind.Optional,
                deepLink = DashboardDeepLink.PlayWeekGame,
                stringFormatArgs = listOf(game.opponentDisplayName),
                formatArgs = listOf(if (game.homeGame) 1 else 0),
                dismissScopeKey = game.opponentTeamId,
            )
        }
    }
}

package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress

/**
 * Offseason shell week 6 — one launcher card into [SigningDayScreen] ([MEGA-FRAME-125]).
 *
 * Step gating lives on the full Signing Day route; the shell does not mirror three separate cards.
 */
object SigningDayShellDashboardTasks {
    fun build(progress: SigningDayProgress): List<DashboardTask> {
        if (progress.hardSignComplete) return emptyList()
        val deepLink = when {
            !progress.rankingsViewed -> DashboardDeepLink.SigningDayRankings
            !progress.positionsConfirmed -> DashboardDeepLink.SigningDayPositions
            else -> DashboardDeepLink.SigningDayPositions
        }
        return listOf(
            DashboardTask(
                id = DashboardTaskId.SigningDayShellLauncher,
                kind = DashboardTaskKind.Required,
                deepLink = deepLink,
            ),
        )
    }
}
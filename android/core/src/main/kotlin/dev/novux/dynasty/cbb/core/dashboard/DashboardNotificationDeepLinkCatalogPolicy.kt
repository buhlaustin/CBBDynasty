package dev.novux.dynasty.cbb.core.dashboard

/** Feed rows may route to rankings, history, March Mayhem, and in-shell actions ([MEGA-SHELL-106]). */
object DashboardNotificationDeepLinkCatalogPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-106"

    val notificationFeedRoutes: Set<DashboardDeepLink> = setOf(
        DashboardDeepLink.RankingsHub,
        DashboardDeepLink.MarchMayhemHub,
        DashboardDeepLink.HistoryHub,
        DashboardDeepLink.PlayWeekGame,
        DashboardDeepLink.DisciplineSheet,
        DashboardDeepLink.CoachCard,
        DashboardDeepLink.Strategy,
        DashboardDeepLink.RecruitingRecommended,
        DashboardDeepLink.RecruitingPortal,
        DashboardDeepLink.SigningDayRankings,
        DashboardDeepLink.SigningDayPositions,
        DashboardDeepLink.FundraisingWeekNine,
    )

    fun isCatalogued(deepLink: DashboardDeepLink): Boolean = deepLink in notificationFeedRoutes
}
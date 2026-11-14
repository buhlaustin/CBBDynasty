package dev.novux.dynasty.cbb.core.navigation

/**
 * Overlay hamburger hubs pop back to the in-career shell and restore the dashboard tab
 * ([MEGA-SHELL-100], [MEGA-FRAME-139]).
 */
object HubRouteShellRetentionPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-100"

    fun restoresShellDashboardTabOnHubBack(): Boolean = true

    val overlayHubRoutes: List<String> = listOf(
        "school_hub",
        "statistics_hub",
        "rankings_hub",
        "news_hub",
        "history_hub",
        "awards_hub",
        "conference_tournament_hub",
        "march_mayhem",
        "strategy",
    )
}
package dev.novux.dynasty.cbb.core.news

/**
 * News hub back uses in-shell dashboard restoration so the prior bottom tab survives ([MEGA-SHELL-118]).
 */
object NewsHubBackNavigationPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-118"

    const val restoresShellDashboardTab: Boolean = true
}
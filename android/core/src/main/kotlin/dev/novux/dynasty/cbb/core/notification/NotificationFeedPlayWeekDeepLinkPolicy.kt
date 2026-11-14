package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/**
 * Game-this-week feed rows route to game preview, not Strategy ([MEGA-SHELL-055]).
 */
object NotificationFeedPlayWeekDeepLinkPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-055"

    val gameThisWeekDeepLink: DashboardDeepLink = DashboardDeepLink.PlayWeekGame

    fun routesToGamePreview(deepLink: DashboardDeepLink): Boolean =
        deepLink == gameThisWeekDeepLink
}
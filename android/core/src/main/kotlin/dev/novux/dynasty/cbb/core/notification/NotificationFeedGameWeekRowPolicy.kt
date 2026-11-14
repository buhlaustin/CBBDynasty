package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/**
 * Game-this-week feed rows route to week game preview, not Strategy prep ([MEGA-SHELL-108]).
 * Delegates to [NotificationFeedPlayWeekDeepLinkPolicy].
 */
object NotificationFeedGameWeekRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-108"

    val deepLink: DashboardDeepLink = NotificationFeedPlayWeekDeepLinkPolicy.gameThisWeekDeepLink

    fun routesToGamePreview(deepLink: DashboardDeepLink): Boolean =
        NotificationFeedPlayWeekDeepLinkPolicy.routesToGamePreview(deepLink)
}
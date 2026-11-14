package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** Signing Day feed taps preserve Rankings vs Positions tab intent ([MEGA-SHELL-117]). */
object NotificationFeedSigningDayFeedPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-117"

    val rankingsDeepLink: DashboardDeepLink = DashboardDeepLink.SigningDayRankings

    val positionsDeepLink: DashboardDeepLink = DashboardDeepLink.SigningDayPositions

    fun isSigningDayDeepLink(link: DashboardDeepLink): Boolean =
        link == rankingsDeepLink || link == positionsDeepLink
}
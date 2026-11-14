package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** Fundraising / facility completion (low tier) ([MEGA-SHELL-053]). */
object NotificationFeedFundraisingRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-053"

    const val TITLE: String = "Program upgrades"

    val deepLink: DashboardDeepLink = DashboardDeepLink.FundraisingWeekNine
}
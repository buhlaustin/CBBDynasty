package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** National record lines open History hub for drill-in context ([MEGA-SHELL-115]). */
object NotificationFeedMajorRecordRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-115"

    val deepLink: DashboardDeepLink = DashboardDeepLink.HistoryHub
}
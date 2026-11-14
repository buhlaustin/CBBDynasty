package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** Medical redshirt eligible optional row ([MEGA-SHELL-052]). */
object NotificationFeedMedicalRedshirtRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-052"

    const val TITLE: String = "Medical redshirt?"

    val deepLink: DashboardDeepLink = DashboardDeepLink.Strategy
}
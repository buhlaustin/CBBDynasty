package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** Team discipline pending → high-priority feed row ([MEGA-SHELL-049], [planning/notifications.md]). */
object NotificationFeedDisciplineRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-049"

    const val TITLE: String = "Team discipline"

    val deepLink: DashboardDeepLink = DashboardDeepLink.DisciplineSheet
}
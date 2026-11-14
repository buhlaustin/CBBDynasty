package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** Carousel offer / extension cards → high-priority feed rows ([MEGA-SHELL-050]). */
object NotificationFeedCarouselRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-050"

    const val TITLE: String = "Coaching carousel"

    val deepLink: DashboardDeepLink = DashboardDeepLink.CoachCard
}
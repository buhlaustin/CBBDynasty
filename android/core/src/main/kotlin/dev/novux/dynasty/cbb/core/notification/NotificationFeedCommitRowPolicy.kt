package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** HS/JUCO commits vs transfer-portal commits route to distinct recruiting tabs ([MEGA-SHELL-114]). */
object NotificationFeedCommitRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-114"

    val hsCommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingRecommended

    val portalCommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingPortal

    val hsDecommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingRecommended

    val portalDecommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingPortal
}
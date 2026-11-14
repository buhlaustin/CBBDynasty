package dev.novux.dynasty.cbb.core.notification

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink

/** HS/JUCO commits vs transfer-portal commits route to distinct recruiting tabs ([MEGA-SHELL-114]). */
object NotificationFeedCommitRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-114"

    val hsCommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingRecommended

    val portalCommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingPortal

    val hsDecommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingRecommended

    val portalDecommitDeepLink: DashboardDeepLink = DashboardDeepLink.RecruitingPortal

    fun prospectNameFromFeedBody(body: String): String? = when {
        body.contains(" signed with") -> body.substringBefore(" signed with").trim()
        body.contains(" joined your roster") -> body.substringBefore(" joined your roster").trim()
        body.contains(" reopened recruitment") -> body.substringBefore(" reopened recruitment").trim()
        body.contains(" entered the transfer portal") ->
            body.substringBefore(" entered the transfer portal").trim()
        else -> null
    }
}
package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab

/** Portal news CTA lands on Recruiting hub Transfer Portal tab ([MEGA-SHELL-035]). */
object NewsHubPortalAddToBoardPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-035"

    val recruitingHubTab: RecruitingHubTab get() = RecruitingHubTab.TransferPortal
}
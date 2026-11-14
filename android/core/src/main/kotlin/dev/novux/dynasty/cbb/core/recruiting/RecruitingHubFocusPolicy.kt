package dev.novux.dynasty.cbb.core.recruiting

/** News / portal drill-in opens recruiting on Transfer Portal with a focused prospect ([MEGA-SHELL-102]). */
object RecruitingHubFocusPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-102"

    val portalDeepLinkTab: RecruitingHubTab = RecruitingHubTab.TransferPortal

    fun isValidFocusProspectId(prospectId: String?): Boolean = !prospectId.isNullOrBlank()

    fun portalEntrantFocusId(entrantId: String): String = entrantId
}
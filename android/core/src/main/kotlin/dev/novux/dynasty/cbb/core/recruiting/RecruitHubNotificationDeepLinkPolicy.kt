package dev.novux.dynasty.cbb.core.recruiting

/** Dashboard / notification feed opens recruiting with scroll + highlight ([MEGA-RECR-092]). */
object RecruitHubNotificationDeepLinkPolicy {
    const val MEGA_ID: String = "MEGA-RECR-092"

    fun resolveFocusTab(
        prospectId: String?,
        hsProspectIds: Set<String>,
        portalEntrantIds: Set<String>,
    ): RecruitingHubTab = when {
        prospectId == null -> RecruitingHubTab.Recommended
        prospectId in portalEntrantIds -> RecruitingHubTab.TransferPortal
        prospectId in hsProspectIds -> RecruitingHubTab.Recommended
        else -> RecruitingHubTab.Recommended
    }

    fun matchProspectIdByCommitName(
        displayName: String,
        pool: List<RecruitProspect>,
    ): String? = pool.firstOrNull {
        it.displayName.equals(displayName, ignoreCase = true)
    }?.prospectId
}
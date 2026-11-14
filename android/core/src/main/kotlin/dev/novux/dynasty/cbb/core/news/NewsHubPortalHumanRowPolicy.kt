package dev.novux.dynasty.cbb.core.news

import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.portal.PortalEntrant
import dev.novux.dynasty.cbb.core.portal.PortalRecruitProspectMapper

/** Portal Top 40 rows use pack display names, not raw ids ([MEGA-SHELL-031]). */
object NewsHubPortalHumanRowPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-031"

    data class RowLabels(
        val playerDisplayName: String,
        val priorSchoolDisplayName: String,
        val stars: Int,
        val positionLabel: String,
    )

    fun rowLabels(entrant: PortalEntrant, packIndex: IdentityPackIndex): RowLabels {
        val priorSchool = packIndex.team(entrant.priorProgramId)?.displayName ?: entrant.priorProgramId
        val prospect = PortalRecruitProspectMapper.toRecruitProspect(entrant, priorSchool)
        return RowLabels(
            playerDisplayName = prospect.displayName,
            priorSchoolDisplayName = priorSchool,
            stars = entrant.stars ?: prospect.stars,
            positionLabel = prospect.position.label,
        )
    }
}
package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.portal.PortalEntrant
import dev.novux.dynasty.cbb.core.portal.PortalRecruitProspectMapper

data class PipelineEntrantSpotlight(
    val entrantDisplayName: String,
    val priorSchoolDisplayName: String,
)

/** Pipeline / national portal optional nudges ([planning/transfer-portal.md#ux--dashboard-optional-tasks-portal-only]). */
object OffseasonPortalOptionalNudgeResolver {
    fun pipelineSpotlight(
        packIndex: IdentityPackIndex,
        entrants: List<PortalEntrant>,
        userPipelineStates: Set<String>,
    ): PipelineEntrantSpotlight? {
        if (entrants.isEmpty()) return null
        val ranked = entrants.sortedBy { it.nationalTop40Rank ?: Int.MAX_VALUE }
        val pipelineMatch = ranked.firstOrNull { entrant ->
            val home = packIndex.team(entrant.priorProgramId)?.homeState
            home != null && home in userPipelineStates
        }
        val pick = pipelineMatch ?: ranked.first()
        val priorLabel = packIndex.team(pick.priorProgramId)?.displayName ?: pick.priorProgramId
        val prospect = PortalRecruitProspectMapper.toRecruitProspect(pick, priorLabel)
        return PipelineEntrantSpotlight(
            entrantDisplayName = prospect.displayName,
            priorSchoolDisplayName = priorLabel,
        )
    }
}

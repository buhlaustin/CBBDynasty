package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitInputs
import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitResolver

/**
 * Hub tab inventory per [planning/recruiting-engine.md#tabs] ([MEGA-RECR-037]).
 */
object RecruitingHubTabPolicy {
    const val RECR_ID: String = "MEGA-RECR-037"

    fun filterPool(pool: List<RecruitProspect>, tab: RecruitingHubTab): List<RecruitProspect> = when (tab) {
        RecruitingHubTab.HighSchool -> pool.filter { it.poolKind == RecruitPoolKind.HighSchool }
        RecruitingHubTab.JuniorCollege -> pool.filter { it.poolKind == RecruitPoolKind.Juco }
        RecruitingHubTab.AllHsJuco,
        RecruitingHubTab.Recommended,
        RecruitingHubTab.TransferPortal,
        RecruitingHubTab.BoardTargets,
        RecruitingHubTab.Committed,
        RecruitingHubTab.Lost,
        -> pool
    }

    fun boardTargets(pool: List<RecruitProspect>, watchlistIds: Set<String>): List<RecruitProspect> =
        pool.filter { it.prospectId in watchlistIds }

    fun committed(
        pool: List<RecruitProspect>,
        offerReservations: List<RecruitingWeekReservation>,
        inputs: SigningDaySoftCommitInputs,
    ): List<RecruitProspect> {
        val committedIds = SigningDaySoftCommitResolver
            .incomingSoftCommits(pool, offerReservations, inputs)
            .map { it.prospectId }
            .toSet()
        return pool.filter { it.prospectId in committedIds }
    }

    fun lost(pool: List<RecruitProspect>, lostIds: Set<String>): List<RecruitProspect> =
        pool.filter { it.prospectId in lostIds }
}

package dev.novux.dynasty.cbb.core.signingday

import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.RecruitingClassSeed

data class NationalRankRow(
    val rank: Int,
    val prospect: RecruitProspect,
)

object NationalRecruitingRankings {
    fun top60FromPool(pool: List<RecruitProspect>): List<NationalRankRow> =
        pool
            .filter { it.nationalHsRank != null }
            .sortedBy { it.nationalHsRank!! }
            .map { NationalRankRow(it.nationalHsRank!!, it) }

    /**
     * Legacy 120-prospect demo list — do not use in dynasty UI; prefer [top60FromPool] after
     * [dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository.ensurePool].
     */
    @Deprecated("Use top60FromPool with IncomingClassSeedRepository.ensurePool")
    fun top60(membershipSeasonIndex: Int): List<NationalRankRow> =
        top60FromPool(RecruitingClassSeed.demoPool(membershipSeasonIndex))
}

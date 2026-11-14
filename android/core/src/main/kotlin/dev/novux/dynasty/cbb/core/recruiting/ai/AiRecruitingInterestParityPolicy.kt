package dev.novux.dynasty.cbb.core.recruiting.ai

import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended
import dev.novux.dynasty.cbb.core.recruiting.RecommendedProspectRow
import dev.novux.dynasty.cbb.core.recruiting.RecommendedRecruitList
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext

/**
 * AI weekly targeting uses the same [RecommendedRecruitList] / interest pipeline as the user hub
 * ([MEGA-RECR-159]).
 */
object AiRecruitingInterestParityPolicy {
    const val MEGA_ID: String = "MEGA-RECR-159"

    fun rankedProspects(
        pool: List<RecruitProspect>,
        school: SchoolRecruitingContext,
        recommended: BalanceRecruitingRecommended,
    ): List<RecommendedProspectRow> =
        RecommendedRecruitList.build(pool, school, recommended)
}
package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended

/** Recommended tab ordering — [planning/recruiting-engine.md#recommended-tab-default]. */
object RecommendedRecruitList {
    fun build(
        pool: List<RecruitProspect>,
        school: SchoolRecruitingContext,
        recommended: BalanceRecruitingRecommended,
        nilOffersByProspect: Map<String, Int> = emptyMap(),
        nilRules: BalanceNilRules? = null,
        spendDeltaByProspect: Map<String, Int> = emptyMap(),
        offerPromisesByProspect: Map<String, RecruitOfferPromise> = emptyMap(),
    ): List<RecommendedProspectRow> {
        return pool
            .map { prospect ->
                val offer = offerPromisesByProspect[prospect.prospectId]
                SignLikelihoodEngine.evaluate(
                    prospect,
                    school,
                    recommended,
                    nilOfferUsd = nilOffersByProspect[prospect.prospectId],
                    nilRules = nilRules,
                    spendDelta = spendDeltaByProspect[prospect.prospectId] ?: 0,
                    promiseDelta = RecruitScholarshipPromiseDeltaPolicy.promiseDelta(offer),
                )
            }
            .filterNot { SignLikelihoodEngine.hiddenOnRecommendedTab(it, school, recommended) }
            .sortedWith(
                compareByDescending<RecommendedProspectRow> { it.adjustedScore }
                    .thenByDescending { it.positionNeed }
                    .thenByDescending { it.inState }
                    .thenByDescending { it.prospect.stars },
            )
            .take(recommended.listCap)
    }
}

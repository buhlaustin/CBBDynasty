package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended

/**
 * Recommended **signLikelihood** uses zero-spend baseline; recruit sheet meter uses live interest
 * ([planning/recruiting-engine.md#two-surfaces], [MEGA-RECR-083]).
 */
object RecruitInterestTwoSurfaces {
    fun baselineSnapshotForSignLikelihood(
        prospect: RecruitProspect,
        school: SchoolRecruitingContext,
        recommended: BalanceRecruitingRecommended,
    ): RecruitInterestSnapshot =
        RecruitInterestEngine.baselineSnapshot(
            prospect = prospect,
            school = school,
            recommended = recommended,
            spendDelta = 0,
            promiseDelta = 0,
            nilOfferUsd = null,
            nilRules = null,
        )

    fun liveInterestMeter(
        prospect: RecruitProspect,
        school: SchoolRecruitingContext,
        recommended: BalanceRecruitingRecommended,
        spendDelta: Int,
        promiseDelta: Int = 0,
        nilOfferUsd: Int? = null,
        nilRules: BalanceNilRules? = null,
    ): Int =
        RecruitInterestEngine.baselineSnapshot(
            prospect = prospect,
            school = school,
            recommended = recommended,
            spendDelta = spendDelta,
            promiseDelta = promiseDelta,
            nilOfferUsd = nilOfferUsd,
            nilRules = nilRules,
        ).liveInterest
}

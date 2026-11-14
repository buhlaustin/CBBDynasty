package dev.novux.dynasty.cbb.core.recruiting

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.facilities.FacilityGradeEngine
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommended
import dev.novux.dynasty.cbb.core.balance.BalanceSignLikelihoodBands

enum class SignLikelihoodBadge {
    Likely,
    Possible,
    Reach,
    LongShot,
}

data class RecommendedProspectRow(
    val prospect: RecruitProspect,
    val baselineInterest: Int,
    val liveInterest: Int,
    val adjustedScore: Double,
    val badge: SignLikelihoodBadge,
    val inState: Boolean,
    val positionNeed: Boolean,
    val influenceBoard: ProspectInfluenceBoard,
)

object SignLikelihoodEngine {
    fun evaluate(
        prospect: RecruitProspect,
        school: SchoolRecruitingContext,
        recommended: BalanceRecruitingRecommended,
        nilOfferUsd: Int? = null,
        nilRules: BalanceNilRules? = null,
        spendDelta: Int = 0,
        promiseDelta: Int = 0,
    ): RecommendedProspectRow {
        val baselineSnapshot = RecruitInterestTwoSurfaces.baselineSnapshotForSignLikelihood(
            prospect,
            school,
            recommended,
        )
        val baseline = baselineSnapshot.baselineInterest
        val live = RecruitInterestTwoSurfaces.liveInterestMeter(
            prospect = prospect,
            school = school,
            recommended = recommended,
            spendDelta = spendDelta,
            promiseDelta = promiseDelta,
            nilOfferUsd = nilOfferUsd,
            nilRules = nilRules,
        )
        var adjusted = baseline.toDouble()
        adjusted -= prestigeGapPenalty(prospect.stars, school.programPrestige, recommended)
        if (prospect.position in school.positionsNeed) {
            adjusted += recommended.rosterNeedBonus
        }
        val facilityPitch = FacilityGradeEngine.summarize(school.facilityGrades).recruitingPitchMultiplier
        adjusted *= facilityPitch
        val inState = prospect.homeState.equals(school.schoolHomeState, ignoreCase = true)
        val positionNeed = prospect.position in school.positionsNeed
        val badgeInput = RecruitNilInterestBadgeRefreshPolicy.badgeScoreForRow(
            adjustedBaselineScore = adjusted,
            liveInterest = live,
            nilOfferUsd = nilOfferUsd,
        )
        val badge = badgeForAdjustedScore(badgeInput, recommended.bands)
        val influence = InfluenceBoardEngine.build(
            prospect = prospect,
            userSchool = school,
            userProgramId = "user-program",
            recommended = recommended,
        )
        return RecommendedProspectRow(
            prospect = prospect,
            baselineInterest = baseline,
            liveInterest = live,
            adjustedScore = adjusted,
            badge = badge,
            inState = inState,
            positionNeed = positionNeed,
            influenceBoard = influence,
        )
    }

    fun hiddenOnRecommendedTab(
        row: RecommendedProspectRow,
        school: SchoolRecruitingContext,
        recommended: BalanceRecruitingRecommended,
    ): Boolean {
        val rank = row.prospect.nationalHsRank
        if (rank != null && rank <= recommended.eliteNationalRankHideThrough) {
            val pipelineSave = row.inState ||
                row.prospect.homeState.uppercase() in school.pipelineStates.map { it.uppercase() }
            if (row.adjustedScore < recommended.eliteShowMinAdjustedScore && !pipelineSave) {
                return true
            }
        }
        if (school.programPrestige < 40 && row.prospect.stars >= 5 && row.badge == SignLikelihoodBadge.LongShot) {
            return true
        }
        return false
    }

    private fun prestigeGapPenalty(
        stars: Int,
        programPrestige: Int,
        recommended: BalanceRecruitingRecommended,
    ): Double {
        val floor = recommended.prestigeFloorByStars[stars.toString()] ?: 0
        val gap = (floor - programPrestige).coerceAtLeast(0)
        return gap * recommended.prestigeGapPenaltyPerPoint
    }

    fun badgeForAdjustedScore(score: Double, bands: BalanceSignLikelihoodBands): SignLikelihoodBadge =
        when {
            score >= bands.likelyMin -> SignLikelihoodBadge.Likely
            score >= bands.possibleMin -> SignLikelihoodBadge.Possible
            score >= bands.reachMin -> SignLikelihoodBadge.Reach
            else -> SignLikelihoodBadge.LongShot
        }
}

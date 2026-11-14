package dev.novux.dynasty.cbb.core.seasonreview

import dev.novux.dynasty.cbb.core.awards.AwardHonorLine
import dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityCategory
import dev.novux.dynasty.cbb.core.school.SchoolSeasonRecord

data class SeasonReviewFacilityGradeRow(
    val category: FacilityCategory,
    val grade: Int,
    val deltaFromBaseline: Int,
)

data class SeasonReviewSnapshot(
    val seasonIndex: Int,
    val schoolDisplayName: String,
    val record: SchoolSeasonRecord,
    val postseasonFinishLabel: String,
    val netRank: Int?,
    val mediaPollRank: Int?,
    val coachesPollRank: Int?,
    val mediaTop25: Boolean,
    val coachesTop25: Boolean,
    val prestigeStart: Int,
    val prestigeEnd: Int,
    val facilityGrades: List<SeasonReviewFacilityGradeRow>,
    val facilityGradeAverage: Int,
    val currentCycleWeeklyPool: Int,
    val nextCycleWeeklyPool: Int,
    val nextCycleNilPoolUsd: Long,
    val recruitingTierLabel: String,
    val jobSecurityScore: Int,
    val jobSecurityLabel: String,
    val adTrustScore: Int,
    val contractYearsLeft: Int,
    val awardHighlights: List<AwardHonorLine> = emptyList(),
)

package dev.novux.dynasty.cbb.core.school

import dev.novux.dynasty.cbb.core.facilities.FacilityGradeSummary

/** School hub Overview block — read-only for opponents. */
data class SchoolHubOverview(
    val teamId: String,
    val displayName: String,
    val conferenceName: String,
    val isUserSchool: Boolean,
    val programPrestige: Int,
    val sosRating: Int,
    val powerIndex: Int,
    val teamOffRating: Int,
    val teamDefRating: Int,
    val record: SchoolSeasonRecord,
    val rankings: SchoolRankingsSnapshot,
    val facilitySummary: FacilityGradeSummary? = null,
    /** User school only — program academics grade 0–100 ([MEGA-GDD-033]). */
    val programAcademicsGrade: Int? = null,
    val recruitingBudgetMultiplier: Double? = null,
    val nilCycleSummary: SchoolHubNilCycleSummary? = null,
)

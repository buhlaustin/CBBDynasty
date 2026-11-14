package dev.novux.dynasty.cbb.core.coach.card

import dev.novux.dynasty.cbb.core.coach.CoachArchetype

data class HeadCoachCard(
    val dynastyId: String,
    val displayName: String,
    val age: Int,
    val schoolDisplayName: String,
    val seasonRecordLabel: String,
    val archetype: CoachArchetype,
    val offensiveSchemeLabel: String,
    val defensiveSchemeLabel: String,
    val contractYearsLeft: Int,
    val contractLengthYears: Int,
    /** Annual base salary in USD for contract UI and staff surfaces. */
    val annualSalaryUsd: Long,
    /** Remaining buyout obligation in USD ([MEGA-COACH-119]). */
    val buyoutUsd: Long,
    val ratings: HeadCoachRatings,
    val almaMaterLabel: String?,
    val avatarPresetId: String = "",
)

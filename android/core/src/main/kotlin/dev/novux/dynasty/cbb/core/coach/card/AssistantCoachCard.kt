package dev.novux.dynasty.cbb.core.coach.card

import dev.novux.dynasty.cbb.core.coach.staff.CoachTrackRole

data class AssistantCoachCard(
    val staffId: String,
    val displayName: String,
    val age: Int,
    val schoolDisplayName: String,
    val seasonRecordLabel: String,
    val role: CoachTrackRole,
    val yearsInRole: Int,
    val yearsAtSchool: Int,
    val contractYearsLeft: Int,
    val contractLengthYears: Int,
    val ratings: AssistantCoachRatings,
    val avatarPresetId: String = "",
    val annualSalaryUsd: Long = 0L,
    val buyoutUsd: Long = 0L,
    val hasHeadCoachCareer: Boolean = false,
)

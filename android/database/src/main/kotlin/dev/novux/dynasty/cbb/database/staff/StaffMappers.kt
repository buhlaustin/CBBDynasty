package dev.novux.dynasty.cbb.database.staff

import dev.novux.dynasty.cbb.core.coach.CoachTrackPortraitIds
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachCard
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachRatings
import dev.novux.dynasty.cbb.core.coach.staff.CoachTrackRole
import dev.novux.dynasty.cbb.core.coach.staff.StaffKind
import dev.novux.dynasty.cbb.core.coach.staff.SupportStaffMember
import dev.novux.dynasty.cbb.core.coach.staff.SupportStaffRole

fun AssistantCoachCard.toEntity(dynastyId: String): DynastyStaffMemberEntity {
    val parts = displayName.split(" ", limit = 2)
    return DynastyStaffMemberEntity(
        staffId = staffId,
        dynastyId = dynastyId,
        staffKindWire = StaffKind.CoachTrack.name,
        roleWire = role.wire,
        firstName = parts.firstOrNull().orEmpty(),
        lastName = parts.drop(1).firstOrNull().orEmpty(),
        age = age,
        recruitingRating = ratings.recruiting,
        developmentRating = ratings.development,
        scoutingRating = ratings.scouting,
        yearsInRole = yearsInRole,
        yearsAtSchool = yearsAtSchool,
        contractYearsLeft = contractYearsLeft,
        contractLengthYears = contractLengthYears,
    )
}

fun SupportStaffMember.toEntity(dynastyId: String): DynastyStaffMemberEntity {
    val parts = displayName.split(" ", limit = 2)
    return DynastyStaffMemberEntity(
        staffId = staffId,
        dynastyId = dynastyId,
        staffKindWire = StaffKind.Support.name,
        roleWire = role.wire,
        firstName = parts.firstOrNull().orEmpty(),
        lastName = parts.drop(1).firstOrNull().orEmpty(),
        supportContributionRating = contributionRating,
        contractYearsLeft = contractYearsLeft,
        contractLengthYears = contractYearsLeft + 1,
    )
}

fun DynastyStaffMemberEntity.toAssistant(schoolDisplayName: String, record: String): AssistantCoachCard? {
    if (staffKindWire != StaffKind.CoachTrack.name) return null
    val role = CoachTrackRole.fromWire(roleWire) ?: return null
    val ratings = AssistantCoachRatings(recruitingRating, developmentRating, scoutingRating)
    val salary = 175_000L + ratings.recruiting * 2_800L + ratings.development * 1_200L
    return AssistantCoachCard(
        staffId = staffId,
        displayName = "$firstName $lastName".trim(),
        age = age,
        schoolDisplayName = schoolDisplayName,
        seasonRecordLabel = record,
        role = role,
        yearsInRole = yearsInRole,
        yearsAtSchool = yearsAtSchool,
        contractYearsLeft = contractYearsLeft,
        contractLengthYears = contractLengthYears,
        ratings = ratings,
        avatarPresetId = CoachTrackPortraitIds.forStaffId(staffId),
        annualSalaryUsd = salary,
        buyoutUsd = salary / 2,
    )
}

fun DynastyStaffMemberEntity.toSupport(): SupportStaffMember? {
    if (staffKindWire != StaffKind.Support.name) return null
    val role = SupportStaffRole.fromWire(roleWire) ?: return null
    return SupportStaffMember(
        staffId = staffId,
        displayName = "$firstName $lastName".trim(),
        role = role,
        contributionRating = supportContributionRating,
        contractYearsLeft = contractYearsLeft,
    )
}

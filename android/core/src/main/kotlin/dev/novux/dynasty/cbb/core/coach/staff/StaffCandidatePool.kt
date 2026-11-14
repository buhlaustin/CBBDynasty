package dev.novux.dynasty.cbb.core.coach.staff

import dev.novux.dynasty.cbb.core.coach.CoachTrackPortraitIds
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachCard
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachRatings
import java.util.UUID
import kotlin.random.Random

object StaffCandidatePool {
    private val hireFirst = listOf("Sam", "Alex", "Riley", "Quinn", "Jamie")
    private val hireLast = listOf("Brooks", "Hayes", "Nguyen", "Coleman", "Porter")
    private val poachFirst = listOf("Marcus", "Elena", "Derek", "Priya")
    private val poachLast = listOf("Whitfield", "Santos", "Okafor", "Linden")

    fun generateAssistantHire(
        dynastyId: String,
        schoolDisplayName: String,
        role: CoachTrackRole,
        seed: Long,
        seasonRecordLabel: String = "0-0",
    ): AssistantCoachCard {
        val random = Random(seed)
        val staffId = "asst-$dynastyId-${UUID.randomUUID()}"
        val ratings = AssistantCoachRatings(
            recruiting = 60 + random.nextInt(25),
            development = 60 + random.nextInt(25),
            scouting = 60 + random.nextInt(25),
        )
        val salary = 165_000L + ratings.recruiting * 2_500L
        return AssistantCoachCard(
            staffId = staffId,
            displayName = "${hireFirst.random(random)} ${hireLast.random(random)}",
            age = 28 + random.nextInt(20),
            schoolDisplayName = schoolDisplayName,
            seasonRecordLabel = seasonRecordLabel,
            role = role,
            yearsInRole = 0,
            yearsAtSchool = 0,
            contractYearsLeft = 3,
            contractLengthYears = 3,
            ratings = ratings,
            avatarPresetId = CoachTrackPortraitIds.forStaffId(staffId),
            annualSalaryUsd = salary,
            buyoutUsd = salary / 3,
        )
    }

    fun generateAssistantPoach(
        dynastyId: String,
        schoolDisplayName: String,
        role: CoachTrackRole,
        seed: Long,
        seasonRecordLabel: String = "0-0",
    ): AssistantCoachCard {
        val random = Random(seed xor 0x5A17L)
        val base = generateAssistantHire(dynastyId, schoolDisplayName, role, seed, seasonRecordLabel)
        return base.copy(
            displayName = "${poachFirst.random(random)} ${poachLast.random(random)}",
            ratings = base.ratings.copy(
                recruiting = (base.ratings.recruiting + 8).coerceAtMost(95),
                development = (base.ratings.development + 6).coerceAtMost(95),
                scouting = (base.ratings.scouting + 5).coerceAtMost(95),
            ),
            yearsInRole = 1,
            contractYearsLeft = 2,
            buyoutUsd = base.annualSalaryUsd,
        )
    }

    fun generateSupportHire(
        dynastyId: String,
        role: SupportStaffRole,
        seed: Long,
    ): SupportStaffMember {
        val random = Random(seed)
        return SupportStaffMember(
            staffId = "support-$dynastyId-${UUID.randomUUID()}",
            displayName = "${hireFirst.random(random)} ${hireLast.random(random)}",
            role = role,
            contributionRating = 58 + random.nextInt(28),
            contractYearsLeft = 2,
        )
    }
}

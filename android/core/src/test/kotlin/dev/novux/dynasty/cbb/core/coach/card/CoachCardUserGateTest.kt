package dev.novux.dynasty.cbb.core.coach.card

import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.core.coach.staff.CoachTrackRole
import dev.novux.dynasty.cbb.core.coach.staff.SchoolStaffRoster
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachCardUserGateTest {
    private fun headCoach(name: String, school: String) = HeadCoachCard(
        dynastyId = "d",
        displayName = name,
        age = 45,
        schoolDisplayName = school,
        seasonRecordLabel = "10-5",
        archetype = CoachArchetype.Developer,
        offensiveSchemeLabel = "Balanced",
        defensiveSchemeLabel = "Man",
        contractYearsLeft = 3,
        contractLengthYears = 4,
        annualSalaryUsd = 500_000L,
        buyoutUsd = 1_000_000L,
        ratings = HeadCoachRatings(80, 78, 75, 82),
        almaMaterLabel = "State U",
    )

    @Test
    fun userHeadCoachMatchesStaffRoster() {
        val card = headCoach("Coach A", "Eagles")
        val roster = SchoolStaffRoster(
            headCoach = card,
            assistants = emptyList(),
            supportStaff = emptyList(),
        )
        assertTrue(CoachCardUserGate.isUserHeadCoach(card, roster))
    }

    @Test
    fun opponentHeadCoachDoesNotMatch() {
        val user = headCoach("Coach A", "Eagles")
        val opponent = headCoach("Coach B", "Tigers")
        val roster = SchoolStaffRoster(headCoach = user, assistants = emptyList(), supportStaff = emptyList())
        assertFalse(CoachCardUserGate.isUserHeadCoach(opponent, roster))
    }

    @Test
    fun userAssistantMatchesByStaffId() {
        val assistant = AssistantCoachCard(
            staffId = "a1",
            displayName = "Asst",
            age = 38,
            schoolDisplayName = "Eagles",
            seasonRecordLabel = "10-5",
            role = CoachTrackRole.RecruitingCoordinator,
            yearsInRole = 2,
            yearsAtSchool = 2,
            contractYearsLeft = 2,
            contractLengthYears = 3,
            ratings = AssistantCoachRatings(70, 72, 68),
            hasHeadCoachCareer = false,
            annualSalaryUsd = 120_000L,
        )
        val roster = SchoolStaffRoster(
            headCoach = headCoach("HC", "Eagles"),
            assistants = listOf(assistant),
            supportStaff = emptyList(),
        )
        assertTrue(CoachCardUserGate.isUserAssistant(assistant, roster))
    }
}

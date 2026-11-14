package dev.novux.dynasty.cbb.core.recruiting.ai

import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiLeagueProgramGeneratorTest {
    @Test
    fun fromPackTeams_excludesUser_usesPackPrestigeAndNil() {
        val teams = listOf(
            overlay("user-team", prestige = 78, state = "KY"),
            overlay("rival", prestige = 55, state = "IN"),
        )
        val programs = AiLeagueProgramGenerator.fromPackTeams(
            teams = teams,
            excludeTeamId = "user-team",
            nilPoolAnnualUsd = { 1_200_000L },
        )
        assertEquals(1, programs.size)
        assertEquals("rival", programs.single().programId)
        assertEquals(55, programs.single().programPrestige)
        assertEquals(1_200_000, programs.single().nilPoolAnnualUsd)
        assertTrue(programs.single().pipelineStates.contains("IN"))
    }

    private fun overlay(
        teamId: String,
        prestige: Int,
        state: String,
    ) = TeamDisplayOverlay(
        teamId = teamId,
        conferenceId = "b1g",
        displayName = teamId,
        shortName = teamId,
        homeCity = "City",
        homeState = state,
        primaryColorHex = "#000000",
        secondaryColorHex = "#FFFFFF",
        logoUri = null,
        postseasonEligible = true,
        programPrestige = prestige,
    )
}

package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.recruiting.ai.AiLeagueProgramGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AiRecruitingSameRulesPathTest {
    @Test
    fun megaRecr068_packTeamIds_notDemoLeagueSynthetic() {
        assertEquals(AiRecruitingSameRulesPath.RECR_ID, "MEGA-RECR-068")
        val packBacked = AiLeagueProgramGenerator.fromPackTeams(
            teams = listOf(
                TeamDisplayOverlay(
                    teamId = "ohio-state",
                    conferenceId = "b1g",
                    displayName = "Ohio State",
                    shortName = "OSU",
                    homeCity = "Columbus",
                    homeState = "OH",
                    primaryColorHex = "#bb0000",
                    secondaryColorHex = "#666666",
                    logoUri = null,
                    postseasonEligible = true,
                    programPrestige = 68,
                ),
            ),
            excludeTeamId = null,
            nilPoolAnnualUsd = { 1_750_000L },
        )
        assertEquals("ohio-state", packBacked.single().programId)
        assertFalse(packBacked.single().programId.startsWith("team-conf-ai-"))
    }
}

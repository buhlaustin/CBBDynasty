package dev.novux.dynasty.cbb.core.coach.card.deep

import dev.novux.dynasty.cbb.core.history.DynastyHistoryArchive
import dev.novux.dynasty.cbb.core.history.GoatListEntry
import dev.novux.dynasty.cbb.core.history.HistoryYearCard
import dev.novux.dynasty.cbb.core.history.PlayerCareerHistory
import dev.novux.dynasty.cbb.core.history.PlayerSeasonHistoryLine
import dev.novux.dynasty.cbb.core.offseason.weektwo.NbaDraftOutcome
import dev.novux.dynasty.cbb.core.offseason.weektwo.NbaDraftResultLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachCardDeepTabsEngineTest {
    private val userTeam = "team_a"
    private val archive = DynastyHistoryArchive(
        dynastyId = "d1",
        careers = mapOf(
            "p1" to PlayerCareerHistory(
                playerId = "p1",
                displayName = "Ace Guard",
                finalTeamId = userTeam,
                seasons = listOf(
                    PlayerSeasonHistoryLine(
                        playerId = "p1",
                        seasonIndex = 2,
                        teamId = userTeam,
                        displayName = "Ace Guard",
                        winSharesSeason = 4.0,
                        peakOvrSeason = 88,
                        minutesSeason = 900,
                        conferencePoty = true,
                    ),
                ),
            ),
        ),
        yearCards = listOf(
            HistoryYearCard(
                seasonIndex = 1,
                ncbaChampionSchool = "State U",
                ncbaMvpLine = null,
                nationalHofInductees = listOf("Legend Forward"),
                goatNumberOne = null,
            ),
        ),
        goatSnapshot = listOf(
            GoatListEntry(
                rank = 1,
                playerId = "p2",
                displayName = "Legend Forward",
                schoolLabel = "State U",
                goatScore = 99.0,
                careerWinShares = 20.0,
                nationalHof = true,
                chips = listOf("HOF 2026"),
            ),
        ),
    )

    @Test
    fun coachedIdsIncludeOnlyUserTeamSeasons() {
        val coached = CoachCardDeepTabsEngine.coachedPlayerIds(archive, userTeam)
        assertEquals(setOf("p1"), coached)
    }

    @Test
    fun hallOfFameMergesNationalAndConference() {
        val coached = setOf("p1", "p2")
        val entries = CoachCardDeepTabsEngine.hallOfFame(coached, archive, userTeam)
        assertTrue(entries.any { it.playerId == "p2" && it.isNational })
        assertTrue(entries.any { it.playerId == "p1" && !it.isNational })
    }

    @Test
    fun draftResumeFiltersCoachedPlayers() {
        val lines = mapOf(
            1 to listOf(
                NbaDraftResultLine("p1", "Ace Guard", 70, "Lottery", NbaDraftOutcome.Drafted, "Pick 12"),
                NbaDraftResultLine("other", "Other", 10, "None", NbaDraftOutcome.ReturnedToRoster, null),
            ),
        )
        val draft = CoachCardDeepTabsEngine.draftResume(setOf("p1"), "State U", lines)
        assertEquals(1, draft.size)
        assertTrue(draft.single().drafted)
    }
}

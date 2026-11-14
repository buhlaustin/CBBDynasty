package dev.novux.dynasty.cbb.core.coach.achievements

import dev.novux.dynasty.cbb.core.brackets.NationalBracketSnapshot
import dev.novux.dynasty.cbb.core.brackets.NcbaMainBracketSnapshot
import dev.novux.dynasty.cbb.core.brackets.NcbaRegionId
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class CoachAchievementEngineTest {
    @Test
    fun nationalChampionAddsTitleAndTourneyRecord() {
        val user = "team-a"
        val main = NcbaMainBracketSnapshot(
            seasonIndex = 1,
            entrants = emptyList(),
            gameResults = listOf(
                LeagueGameResult(user, "x", 70, 60, rsWeekIndex = 1),
            ),
            regionalChampions = mapOf(NcbaRegionId.East to user),
            championTeamId = user,
            completed = true,
        )
        val standings = LeagueStandingsState(
            seasonIndex = 1,
            records = mapOf(user to LeagueTeamRecord(user, wins = 30, losses = 5)),
            ncbaMainBracket = main,
        )
        val ledger = CoachAchievementEngine.finalizeSeason(
            CoachAchievementEngine.empty("dyn-1"),
            standings,
            user,
        )
        val view = HeadCoachAchievements.fromLedger(ledger)
        assertEquals(1, view.nationalTitles)
        assertEquals(1, view.finalFours)
        assertEquals("1-0", view.ncbaTourneyRecordLabel)
        assertEquals("30-5", view.recordLabel)
    }

    @Test
    fun nciHeritageTitlesCounted() {
        val user = "t"
        val standings = LeagueStandingsState(
            seasonIndex = 2,
            records = mapOf(user to LeagueTeamRecord(user, 20, 10)),
            nciBracket = NationalBracketSnapshot(
                tournamentId = "NCI",
                seasonIndex = 2,
                invites = emptyList(),
                gameResults = listOf(LeagueGameResult(user, "b", 80, 70, 1)),
                championTeamId = user,
                completed = true,
            ),
            heritageBracket = NationalBracketSnapshot(
                tournamentId = "Heritage",
                seasonIndex = 2,
                invites = emptyList(),
                gameResults = emptyList(),
                championTeamId = null,
                completed = false,
            ),
        )
        val snap = CoachAchievementEngine.snapshot(
            CoachAchievementEngine.empty("d"),
            standings,
            user,
        )
        assertEquals(1, snap.nciTitles)
        assertEquals("1-0", snap.nciRecordLabel)
    }
}

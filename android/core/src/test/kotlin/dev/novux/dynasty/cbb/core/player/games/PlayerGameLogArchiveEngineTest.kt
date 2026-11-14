package dev.novux.dynasty.cbb.core.player.games

import dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedPlayerBoxScore
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedTeamBoxScore
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerGameLogArchiveEngineTest {
    @Test
    fun buildsEntryFromUserTeamPlayerBox() {
        val archive = ProvisionalGameBoxArchive(
            homeBox = ArchivedTeamBoxScore(
                teamId = "home",
                points = 80,
                fieldGoalsMade = 30,
                fieldGoalsAttempted = 60,
                threePointsMade = 8,
                threePointsAttempted = 20,
                freeThrowsMade = 12,
                freeThrowsAttempted = 14,
                turnovers = 10,
                possessions = 70,
                personalFouls = 15,
            ),
            awayBox = ArchivedTeamBoxScore(
                teamId = "away",
                points = 72,
                fieldGoalsMade = 28,
                fieldGoalsAttempted = 58,
                threePointsMade = 6,
                threePointsAttempted = 18,
                freeThrowsMade = 10,
                freeThrowsAttempted = 12,
                turnovers = 12,
                possessions = 68,
                personalFouls = 17,
            ),
            homePlayerBox = listOf(
                ArchivedPlayerBoxScore(
                    playerId = "p1",
                    minutesSeconds = 32 * 60,
                    points = 22,
                    fieldGoalsMade = 8,
                    fieldGoalsAttempted = 14,
                    threePointsMade = 2,
                    threePointsAttempted = 5,
                    freeThrowsMade = 4,
                    freeThrowsAttempted = 5,
                    offensiveRebounds = 1,
                    defensiveRebounds = 6,
                    assists = 5,
                    steals = 2,
                    blocks = 1,
                    turnovers = 3,
                    personalFouls = 2,
                ),
            ),
        )
        val mark = ProvisionalGameMark(
            gameId = "game-1",
            homeScore = 80,
            awayScore = 72,
            possessionEngine = true,
            boxArchive = archive,
        )
        val entries = PlayerGameLogArchiveEngine.entriesForRosterPlayers(
            mark = mark,
            userTeamIsHome = true,
            opponentLabel = "Rival U",
            rsWeekIndex = 4,
            rosterPlayerIds = setOf("p1", "p2"),
        )
        assertEquals(1, entries.size)
        val row = entries.single()
        assertTrue(row.fromSimArchive)
        assertEquals(22, row.points)
        assertEquals(7, row.rebounds)
        assertEquals(32, row.minutes)
        assertEquals("Rival U", row.opponentLabel)
    }
}

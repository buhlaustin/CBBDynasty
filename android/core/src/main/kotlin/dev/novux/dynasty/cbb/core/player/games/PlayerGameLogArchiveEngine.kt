package dev.novux.dynasty.cbb.core.player.games

import dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedPlayerBoxScore
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameBoxArchive
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark

/** Game log rows from possession-sim box archives ([MEGA-COACH-155]). */
object PlayerGameLogArchiveEngine {
    const val MEGA_ID: String = "MEGA-COACH-155"

    fun entriesForRosterPlayers(
        mark: ProvisionalGameMark,
        userTeamIsHome: Boolean,
        opponentLabel: String,
        rsWeekIndex: Int,
        rosterPlayerIds: Set<String>,
    ): List<PlayerGameLogEntry> {
        val archive = mark.boxArchive ?: return emptyList()
        if (!mark.possessionEngine) return emptyList()
        val userLines = userPlayerBox(archive, userTeamIsHome)
        val userTeamScore = if (userTeamIsHome) mark.homeScore else mark.awayScore
        val oppScore = if (userTeamIsHome) mark.awayScore else mark.homeScore
        return rosterPlayerIds.mapNotNull { playerId ->
            val line = userLines.firstOrNull { it.playerId == playerId } ?: return@mapNotNull null
            line.toGameLogEntry(
                gameKey = "${mark.gameId}_$playerId",
                rsWeekIndex = rsWeekIndex,
                opponentLabel = opponentLabel,
                isHome = userTeamIsHome,
                teamScore = userTeamScore,
                opponentScore = oppScore,
            )
        }
    }

    private fun userPlayerBox(archive: ProvisionalGameBoxArchive, userTeamIsHome: Boolean) =
        if (userTeamIsHome) archive.homePlayerBox else archive.awayPlayerBox

    private fun ArchivedPlayerBoxScore.toGameLogEntry(
        gameKey: String,
        rsWeekIndex: Int,
        opponentLabel: String,
        isHome: Boolean,
        teamScore: Int,
        opponentScore: Int,
    ): PlayerGameLogEntry = PlayerGameLogEntry(
        gameKey = gameKey,
        rsWeekIndex = rsWeekIndex,
        opponentLabel = opponentLabel,
        isHome = isHome,
        teamScore = teamScore,
        opponentScore = opponentScore,
        points = points,
        assists = assists,
        rebounds = offensiveRebounds + defensiveRebounds,
        steals = steals,
        blocks = blocks,
        turnovers = turnovers,
        fieldGoalsMade = fieldGoalsMade,
        fieldGoalsAttempted = fieldGoalsAttempted,
        threePointsMade = threePointsMade,
        threePointsAttempted = threePointsAttempted,
        freeThrowsMade = freeThrowsMade,
        freeThrowsAttempted = freeThrowsAttempted,
        minutes = (minutesSeconds / 60).coerceAtLeast(0),
        fromSimArchive = true,
    )
}

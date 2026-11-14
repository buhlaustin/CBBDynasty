package dev.novux.dynasty.cbb.core.rankings

import dev.novux.dynasty.cbb.core.league.LeagueGameResult

/**
 * Head-to-head mini-league among tied teams (3+) — [planning/game-design.md#conference-standings--tiebreakers].
 */
object ConferenceStandingsH2hMiniLeague {
    fun recordAmong(
        teamId: String,
        poolTeamIds: Set<String>,
        conferenceGames: List<LeagueGameResult>,
    ): MiniLeagueRecord {
        val poolGames = conferenceGames.filter { game ->
            game.homeTeamId in poolTeamIds && game.awayTeamId in poolTeamIds
        }
        var wins = 0
        var losses = 0
        var pointDiff = 0
        poolGames.forEach { game ->
            when (teamId) {
                game.homeTeamId -> {
                    pointDiff += game.homeScore - game.awayScore
                    if (game.homeScore > game.awayScore) wins++ else losses++
                }
                game.awayTeamId -> {
                    pointDiff += game.awayScore - game.homeScore
                    if (game.awayScore > game.homeScore) wins++ else losses++
                }
            }
        }
        return MiniLeagueRecord(wins = wins, losses = losses, pointDifferential = pointDiff)
    }

    fun compareRecords(a: MiniLeagueRecord, b: MiniLeagueRecord): Int =
        compareByDescending<MiniLeagueRecord> { it.wins }
            .thenBy { it.losses }
            .thenByDescending { it.pointDifferential }
            .compare(a, b)

    data class MiniLeagueRecord(
        val wins: Int,
        val losses: Int,
        val pointDifferential: Int,
    )
}

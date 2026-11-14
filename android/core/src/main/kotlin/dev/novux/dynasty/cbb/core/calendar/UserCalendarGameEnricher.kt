package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.advance.UserLeagueGameIds
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.dashboard.DashboardLastGameSnapshot
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState

/** Attach scores / box archives for calendar taps ([MEGA-FRAME-048]). */
object UserCalendarGameEnricher {
    fun enrichRows(
        rows: List<UserCalendarWeekRow>,
        userTeamId: String,
        activePosition: CalendarPosition,
        standings: LeagueStandingsState?,
        provisionalGames: Map<String, ProvisionalGameMark>,
        lastGame: DashboardLastGameSnapshot?,
    ): List<UserCalendarWeekRow> =
        rows.map { row ->
            if (row.rowKind != UserCalendarRowKind.Game) return@map row
            row.copy(
                games = row.games.map { game ->
                    game.copy(
                        playedResult = resolvePlayedResult(
                            game = game,
                            userTeamId = userTeamId,
                            activePosition = activePosition,
                            standings = standings,
                            provisionalGames = provisionalGames,
                            lastGame = lastGame,
                        ),
                    )
                },
            )
        }

  private fun resolvePlayedResult(
        game: UserCalendarGameEntry,
        userTeamId: String,
        activePosition: CalendarPosition,
        standings: LeagueStandingsState?,
        provisionalGames: Map<String, ProvisionalGameMark>,
        lastGame: DashboardLastGameSnapshot?,
    ): UserCalendarGameResult? {
        val gameId = UserLeagueGameIds.forPosition(game.gamePosition, game.gameIndexInWeek)
        provisionalGames[gameId]?.let { mark ->
            val userHome = game.homeGame
            val userScore = if (userHome) mark.homeScore else mark.awayScore
            val oppScore = if (userHome) mark.awayScore else mark.homeScore
            return UserCalendarGameResult(
                userScore = userScore,
                opponentScore = oppScore,
                userTeamIsHome = userHome,
                boxArchive = mark.boxArchive,
                provisionalReplay = true,
            )
        }
        if (!game.isPastWeek) return null
        val league = findLeagueResult(userTeamId, game, standings) ?: return null
        val userHome = league.homeTeamId == userTeamId
        val userScore = if (userHome) league.homeScore else league.awayScore
        val oppScore = if (userHome) league.awayScore else league.homeScore
        val box = lastGame?.takeIf { matchesLastGame(game, lastGame, userScore, oppScore) }?.boxArchive
        return UserCalendarGameResult(
            userScore = userScore,
            opponentScore = oppScore,
            userTeamIsHome = userHome,
            boxArchive = box,
        )
    }

    private fun matchesLastGame(
        game: UserCalendarGameEntry,
        last: DashboardLastGameSnapshot,
        userScore: Int,
        oppScore: Int,
    ): Boolean =
        last.opponentDisplayName == game.opponentDisplayName &&
            last.phase == game.gamePosition.phase &&
            last.weekIndex == game.gamePosition.weekIndex &&
            last.userScore == userScore &&
            last.opponentScore == oppScore

    private fun findLeagueResult(
        userTeamId: String,
        game: UserCalendarGameEntry,
        standings: LeagueStandingsState?,
    ): LeagueGameResult? {
        standings ?: return null
        val opponentId = game.opponentTeamId
        val all = buildList {
            addAll(standings.seasonResults)
            standings.conferenceTournaments?.resultsByConference?.values?.forEach {
                addAll(it.gameResults)
            }
        }
        return all.firstOrNull { result ->
            val involvesUser = userTeamId == result.homeTeamId || userTeamId == result.awayTeamId
            val involvesOpp = opponentId == result.homeTeamId || opponentId == result.awayTeamId
            if (!involvesUser || !involvesOpp) return@firstOrNull false
            when (game.gamePosition.phase) {
                CalendarPhase.RegularSeason -> result.rsWeekIndex == game.gamePosition.weekIndex
                CalendarPhase.ConferenceTournament -> result.rsWeekIndex == game.gamePosition.weekIndex
                else -> true
            }
        }
    }
}

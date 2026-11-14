package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState

object DashboardLastUserGameResolver {
    fun fromSnapshot(snapshot: DashboardLastGameSnapshot?): DashboardLastGameSummary? {
        snapshot ?: return null
        return DashboardLastGameSummary(
            snapshot = snapshot,
            hasBoxScore = snapshot.boxArchive != null,
        )
    }

    fun fromStandings(
        userTeamId: String,
        userTeamDisplayName: String,
        standings: LeagueStandingsState?,
        teamDisplayName: (String) -> String,
    ): DashboardLastGameSummary? {
        standings ?: return null
        val last = lastUserResult(userTeamId, standings) ?: return null
        val userHome = userTeamId == last.homeTeamId
        val opponentId = if (userHome) last.awayTeamId else last.homeTeamId
        val userScore = if (userHome) last.homeScore else last.awayScore
        val oppScore = if (userHome) last.awayScore else last.homeScore
        val snapshot = DashboardLastGameSnapshot(
            opponentDisplayName = teamDisplayName(opponentId),
            userScore = userScore,
            opponentScore = oppScore,
            userWon = userScore > oppScore,
            weekIndex = last.rsWeekIndex,
            phase = inferPhase(standings, last.rsWeekIndex),
            userTeamIsHome = userHome,
            userTeamDisplayName = userTeamDisplayName,
            boxArchive = null,
        )
        return DashboardLastGameSummary(snapshot = snapshot, hasBoxScore = false)
    }

    private fun lastUserResult(userTeamId: String, standings: LeagueStandingsState): LeagueGameResult? {
        val all = buildList {
            addAll(standings.seasonResults)
            standings.conferenceTournaments?.resultsByConference?.values?.forEach { conf ->
                addAll(conf.gameResults)
            }
            standings.ncbaOpeningRound?.gameResults?.let { addAll(it) }
            standings.ncbaMainBracket?.gameResults?.let { addAll(it) }
            standings.nciBracket?.gameResults?.let { addAll(it) }
            standings.heritageBracket?.gameResults?.let { addAll(it) }
        }.filter { userTeamId == it.homeTeamId || userTeamId == it.awayTeamId }
        return all.maxWithOrNull(compareBy<LeagueGameResult> { it.rsWeekIndex }.thenBy { it.homeTeamId })
    }

    private fun inferPhase(standings: LeagueStandingsState, weekIndex: Int): CalendarPhase {
        if (standings.ncbaOpeningRound?.gameResults?.any { it.rsWeekIndex == weekIndex } == true ||
            standings.ncbaMainBracket?.gameResults?.any { it.rsWeekIndex == weekIndex } == true ||
            standings.nciBracket?.gameResults?.any { it.rsWeekIndex == weekIndex } == true ||
            standings.heritageBracket?.gameResults?.any { it.rsWeekIndex == weekIndex } == true
        ) {
            return CalendarPhase.Postseason
        }
        if (standings.conferenceTournaments?.resultsByConference?.values?.any { season ->
                season.gameResults.any { it.rsWeekIndex == weekIndex }
            } == true
        ) {
            return CalendarPhase.ConferenceTournament
        }
        return CalendarPhase.RegularSeason
    }
}

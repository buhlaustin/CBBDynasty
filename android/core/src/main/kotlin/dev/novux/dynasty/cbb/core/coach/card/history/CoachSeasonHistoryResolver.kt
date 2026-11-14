package dev.novux.dynasty.cbb.core.coach.card.history

import dev.novux.dynasty.cbb.core.history.HistoryYearCard
import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState

object CoachSeasonHistoryResolver {
    fun conferenceRecordLabel(
        standings: LeagueStandingsState?,
        userTeamId: String,
        conferenceTeamIds: Set<String>,
    ): String {
        if (standings == null || conferenceTeamIds.isEmpty()) return "—"
        val (wins, losses) = conferenceRecord(standings.seasonResults, userTeamId, conferenceTeamIds)
        if (wins + losses == 0) return "—"
        return "$wins-$losses"
    }

    fun scheduleSummaryLine(standings: LeagueStandingsState?, userTeamId: String): String {
        if (standings == null) return "—"
        val played = standings.seasonResults.count { it.involves(userTeamId) }
        val record = standings.recordFor(userTeamId)
        return "$played games · overall ${record.wins}-${record.losses}"
    }

    fun tournamentPathLine(
        standings: LeagueStandingsState?,
        userTeamId: String,
        conferenceId: String?,
    ): String {
        if (standings == null) return "—"
        val parts = buildList {
            conferenceId?.let { confId ->
                standings.conferenceTournaments?.resultsByConference?.get(confId)?.let { result ->
                    when (userTeamId) {
                        result.championTeamId -> add("Conference tournament champion")
                        result.runnerUpTeamId -> add("Conference tournament runner-up")
                        in result.qualifiedTeamIds -> add("Conference tournament")
                    }
                }
            }
            val main = standings.ncbaMainBracket
            if (main != null) {
                val entrant = main.entrants.firstOrNull { it.teamId == userTeamId }
                if (entrant != null) {
                    add("NCBA seed #${entrant.ncbaSeed}")
                    if (main.championTeamId == userTeamId) add("NCBA champion")
                }
            }
            standings.selectionSunday?.ncbaField?.firstOrNull { it.teamId == userTeamId }?.let { entry ->
                if (main?.entrants?.none { it.teamId == userTeamId } != false) {
                    add("NCBA field (${entry.bidType.name})")
                }
            }
            standings.nciBracket?.let { bracket ->
                if (bracket.championTeamId == userTeamId) add("NCI champion")
            }
            standings.heritageBracket?.let { bracket ->
                if (bracket.championTeamId == userTeamId) add("Heritage champion")
            }
        }
        return parts.distinct().joinToString(" · ").ifBlank { "No postseason path recorded" }
    }

    fun newsTagLines(yearCard: HistoryYearCard?): List<String> =
        buildList {
            yearCard?.ncbaMvpLine?.let { add(it) }
            yearCard?.recordsNoted?.let { addAll(it) }
            yearCard?.goatNumberOne?.let { add("GOAT #1: $it") }
        }.distinct()

    private fun conferenceRecord(
        games: List<LeagueGameResult>,
        userTeamId: String,
        conferenceTeamIds: Set<String>,
    ): Pair<Int, Int> {
        var wins = 0
        var losses = 0
        games.forEach { game ->
            if (game.homeTeamId !in conferenceTeamIds || game.awayTeamId !in conferenceTeamIds) return@forEach
            if (game.homeTeamId != userTeamId && game.awayTeamId != userTeamId) return@forEach
            val userHome = game.homeTeamId == userTeamId
            val userWon = (userHome && game.homeScore > game.awayScore) ||
                (!userHome && game.awayScore > game.homeScore)
            if (userWon) wins++ else losses++
        }
        return wins to losses
    }

    private fun LeagueGameResult.involves(teamId: String): Boolean =
        homeTeamId == teamId || awayTeamId == teamId
}

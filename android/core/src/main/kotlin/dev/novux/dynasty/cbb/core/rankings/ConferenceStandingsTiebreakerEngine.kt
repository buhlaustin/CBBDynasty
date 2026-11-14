package dev.novux.dynasty.cbb.core.rankings

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueTeamRecord
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import kotlin.math.abs

/**
 * Applies pack `standingsTiebreakers` stack after W-L ([planning/game-design.md#conference-standings--tiebreakers]).
 */
object ConferenceStandingsTiebreakerEngine {
    private const val TOP_N_IN_CONFERENCE = 4

    fun sortConferenceTeams(
        conferenceId: String,
        teams: List<TeamDisplayOverlay>,
        records: Map<String, LeagueTeamRecord>,
        seasonResults: List<LeagueGameResult>,
        tiebreakers: List<String>,
    ): List<ConferenceTeamRecordRow> {
        val teamIds = teams.map { it.teamId }.toSet()
        val confGames = seasonResults.filter { game ->
            game.homeTeamId in teamIds && game.awayTeamId in teamIds
        }
        val rows = teams.map { team ->
            val rec = records[team.teamId] ?: LeagueTeamRecord(teamId = team.teamId)
            val confRec = conferenceRecord(team.teamId, confGames)
            ConferenceTeamRecordRow(
                teamId = team.teamId,
                displayName = team.displayName,
                wins = confRec.first,
                losses = confRec.second,
            )
        }
        val stack = tiebreakers.ifEmpty { DEFAULT_STACK }
        val context = SortContext(
            conferenceId = conferenceId,
            teamIds = teamIds,
            confGames = confGames,
            overallRecords = records,
            stack = stack,
        )
        val grouped = rows.groupBy { it.wins to it.losses }
        val recordKeys = grouped.keys.sortedWith(
            compareByDescending<Pair<Int, Int>> { it.first }.thenBy { it.second },
        )
        return recordKeys.flatMap { key ->
            sortTiedGroup(grouped[key].orEmpty(), context)
        }
    }

    private fun sortTiedGroup(
        group: List<ConferenceTeamRecordRow>,
        context: SortContext,
    ): List<ConferenceTeamRecordRow> {
        if (group.size <= 1) return group
        var buckets = listOf(group)
        for (key in context.stack) {
            val next = mutableListOf<List<ConferenceTeamRecordRow>>()
            for (bucket in buckets) {
                if (bucket.size <= 1) {
                    next.add(bucket)
                    continue
                }
                next.addAll(splitByTiebreakerKey(bucket, key, context))
            }
            buckets = next
            if (buckets.all { it.size == 1 }) break
        }
        return buckets.flatMap { bucket ->
            if (bucket.size <= 1) bucket else bucket.sortedBy { it.displayName }
        }
    }

    private fun splitByTiebreakerKey(
        bucket: List<ConferenceTeamRecordRow>,
        key: String,
        context: SortContext,
    ): List<List<ConferenceTeamRecordRow>> {
        val normalized = key.trim().lowercase()
        return when (normalized) {
            "h2h" -> splitByMiniLeagueOrPairwise(bucket, context.confGames) { pool, teamId ->
                ConferenceStandingsH2hMiniLeague.recordAmong(teamId, pool, context.confGames).let { rec ->
                    Triple(rec.wins, rec.losses, rec.pointDifferential)
                }
            }
            "h2h-point-diff" -> splitByMiniLeagueOrPairwise(bucket, context.confGames) { pool, teamId ->
                val rec = ConferenceStandingsH2hMiniLeague.recordAmong(teamId, pool, context.confGames)
                Triple(rec.pointDifferential, 0, 0)
            }
            "record-vs-top-teams" -> splitByMetric(bucket) { row ->
                val top = topTeamsInConference(context)
                winsAgainstSet(row.teamId, top, context.confGames)
            }
            "record-vs-common-opponents" -> splitByPairwiseCommon(bucket, context)
            "point-differential" -> splitByMetric(bucket) { row ->
                conferencePointDiff(row.teamId, context.confGames)
            }
            "coin-flip" -> splitByMetric(bucket) { row ->
                abs((row.teamId + context.conferenceId).hashCode())
            }
            else -> listOf(bucket)
        }
    }

    private fun splitByMetric(
        bucket: List<ConferenceTeamRecordRow>,
        metric: (ConferenceTeamRecordRow) -> Int,
    ): List<List<ConferenceTeamRecordRow>> =
        bucket.groupBy { metric(it) }
            .entries
            .sortedByDescending { it.key }
            .map { it.value }

    private fun splitByPairwiseCommon(
        bucket: List<ConferenceTeamRecordRow>,
        context: SortContext,
    ): List<List<ConferenceTeamRecordRow>> {
        if (bucket.size == 2) {
            val a = bucket[0]
            val b = bucket[1]
            val cmp = compareCommonOpponents(a.teamId, b.teamId, context)
            if (cmp == 0) return listOf(bucket)
            return if (cmp < 0) listOf(listOf(a), listOf(b)) else listOf(listOf(b), listOf(a))
        }
        return splitByMetric(bucket) { row ->
            val opps = opponentsPlayed(row.teamId, context.confGames)
            val common = opps.intersect(bucket.map { it.teamId }.toSet())
            winsAgainstSet(row.teamId, common, context.confGames)
        }
    }

    private fun splitByMiniLeagueOrPairwise(
        bucket: List<ConferenceTeamRecordRow>,
        games: List<LeagueGameResult>,
        miniKey: (Set<String>, String) -> Triple<Int, Int, Int>,
    ): List<List<ConferenceTeamRecordRow>> {
        if (bucket.size == 2) {
            val a = bucket[0]
            val b = bucket[1]
            val cmp = compareH2H(a.teamId, b.teamId, games)
            if (cmp == 0) return listOf(bucket)
            return if (cmp < 0) listOf(listOf(a), listOf(b)) else listOf(listOf(b), listOf(a))
        }
        val pool = bucket.map { it.teamId }.toSet()
        return bucket.groupBy { miniKey(pool, it.teamId) }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Triple<Int, Int, Int>, List<ConferenceTeamRecordRow>>> { it.key.first }
                    .thenBy { it.key.second }
                    .thenByDescending { it.key.third },
            )
            .map { it.value }
    }

    private val DEFAULT_STACK = listOf("h2h", "h2h-point-diff", "record-vs-top-teams", "coin-flip")

    private fun conferenceRecord(teamId: String, confGames: List<LeagueGameResult>): Pair<Int, Int> {
        var wins = 0
        var losses = 0
        confGames.forEach { game ->
            val homeWon = game.homeScore > game.awayScore
            when (teamId) {
                game.homeTeamId -> if (homeWon) wins++ else losses++
                game.awayTeamId -> if (homeWon) losses++ else wins++
            }
        }
        return wins to losses
    }

    private fun compareH2H(a: String, b: String, games: List<LeagueGameResult>): Int =
        h2hWins(b, a, games).compareTo(h2hWins(a, b, games))

    private fun h2hWins(team: String, opponent: String, games: List<LeagueGameResult>): Int =
        games.count { match ->
            (match.homeTeamId == team && match.awayTeamId == opponent && match.homeScore > match.awayScore) ||
                (match.awayTeamId == team && match.homeTeamId == opponent && match.awayScore > match.homeScore)
        }

    private fun topTeamsInConference(context: SortContext): Set<String> {
        val ranked = context.teamIds
            .map { id ->
                val rec = conferenceRecord(id, context.confGames)
                id to (rec.first - rec.second)
            }
            .sortedByDescending { it.second }
            .map { it.first }
        return ranked.take(TOP_N_IN_CONFERENCE).toSet()
    }

    private fun winsAgainstSet(team: String, opponents: Set<String>, games: List<LeagueGameResult>): Int =
        games.count { game ->
            val opp = when (team) {
                game.homeTeamId -> game.awayTeamId
                game.awayTeamId -> game.homeTeamId
                else -> null
            }
            opp != null && opp in opponents && (
                (game.homeTeamId == team && game.homeScore > game.awayScore) ||
                    (game.awayTeamId == team && game.awayScore > game.homeScore)
                )
        }

    private fun compareCommonOpponents(a: String, b: String, context: SortContext): Int {
        val oppsA = opponentsPlayed(a, context.confGames)
        val oppsB = opponentsPlayed(b, context.confGames)
        val common = oppsA.intersect(oppsB)
        val winsA = winsAgainstSet(a, common, context.confGames)
        val winsB = winsAgainstSet(b, common, context.confGames)
        return winsB.compareTo(winsA)
    }

    private fun opponentsPlayed(team: String, games: List<LeagueGameResult>): Set<String> =
        games.mapNotNull { game ->
            when (team) {
                game.homeTeamId -> game.awayTeamId
                game.awayTeamId -> game.homeTeamId
                else -> null
            }
        }.toSet()

    private fun conferencePointDiff(team: String, games: List<LeagueGameResult>): Int =
        games.sumOf { game ->
            when (team) {
                game.homeTeamId -> game.homeScore - game.awayScore
                game.awayTeamId -> game.awayScore - game.homeScore
                else -> 0
            }
        }

    private data class SortContext(
        val conferenceId: String,
        val teamIds: Set<String>,
        val confGames: List<LeagueGameResult>,
        val overallRecords: Map<String, LeagueTeamRecord>,
        val stack: List<String>,
    )
}

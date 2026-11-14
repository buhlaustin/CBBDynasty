package dev.novux.dynasty.cbb.core.preview

import dev.novux.dynasty.cbb.core.league.LeagueGameResult
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview
import kotlin.math.roundToInt

/** Side-by-side team preview stats — works with zero games via ratings. */
object GamePreviewScoutingBuilder {
    private const val LEAGUE_PPG = 72.0
    private const val LEAGUE_RPG = 34.0
    private const val LEAGUE_PACE = 68.0

    fun fromOverview(
        overview: SchoolHubOverview,
        standings: LeagueStandingsState?,
    ): GamePreviewTeamScouting {
        val results = standings?.seasonResults.orEmpty()
        val accrued = accumulateTeamStats(overview.teamId, results)
        val line = if (accrued.games > 0) {
            val ppg = accrued.points / accrued.games.toDouble()
            val pace = accrued.possessions / accrued.games
            val offEff = (accrued.points / accrued.possessions * 100.0).roundToInt().coerceIn(80, 125)
            GamePreviewScoutingLine(
                pointsPerGame = ppg,
                reboundsPerGame = pace * 0.50,
                offensiveEfficiency = offEff,
                pace = pace,
                netRank = standings?.netRankByTeam?.get(overview.teamId) ?: overview.rankings.netRank,
                mediaPollRank = standings?.mediaPollRankByTeam?.get(overview.teamId)
                    ?: overview.rankings.mediaPollRank,
            )
        } else {
            ratingFallback(overview)
        }
        return GamePreviewTeamScouting(
            teamId = overview.teamId,
            displayName = overview.displayName,
            line = line,
        )
    }

    fun fromPrestige(teamId: String, displayName: String, prestige: Int): GamePreviewTeamScouting {
        val p = prestige.coerceIn(1, 99)
        val off = 58 + (p * 0.35).toInt()
        return GamePreviewTeamScouting(
            teamId = teamId,
            displayName = displayName,
            line = ratingFallbackFromRatings(off, netRank = null, mediaPollRank = null),
        )
    }

    private fun ratingFallback(overview: SchoolHubOverview): GamePreviewScoutingLine =
        ratingFallbackFromRatings(
            overview.teamOffRating,
            overview.rankings.netRank,
            overview.rankings.mediaPollRank,
        )

    private fun ratingFallbackFromRatings(
        offensiveRating: Int,
        netRank: Int?,
        mediaPollRank: Int?,
    ): GamePreviewScoutingLine {
        val delta = (offensiveRating - 70).toDouble()
        return GamePreviewScoutingLine(
            pointsPerGame = LEAGUE_PPG + delta * 0.28,
            reboundsPerGame = LEAGUE_RPG + delta * 0.06,
            offensiveEfficiency = (100 + delta * 0.85).roundToInt().coerceIn(90, 118),
            pace = LEAGUE_PACE + delta * 0.04,
            netRank = netRank,
            mediaPollRank = mediaPollRank,
        )
    }

    private data class TeamAccum(
        var games: Int = 0,
        var points: Int = 0,
        var possessions: Double = 0.0,
    )

    private fun accumulateTeamStats(teamId: String, results: List<LeagueGameResult>): TeamAccum {
        val acc = TeamAccum()
        results.forEach { game ->
            val home = game.homeTeamId == teamId
            val away = game.awayTeamId == teamId
            if (!home && !away) return@forEach
            val scored = if (home) game.homeScore else game.awayScore
            acc.points += scored
            acc.possessions += estimatePossessions(game.homeScore, game.awayScore)
            acc.games++
        }
        return acc
    }

    private fun estimatePossessions(homeScore: Int, awayScore: Int): Double {
        val total = homeScore + awayScore
        return (68.0 + (total - 144) * 0.08).coerceIn(58.0, 78.0)
    }
}

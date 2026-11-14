package dev.novux.dynasty.cbb.core.player.stats

import kotlin.math.roundToInt

data class PlayerRateStats(
    val pointsPerGame: Double,
    val assistsPerGame: Double,
    val reboundsPerGame: Double,
    val stealsPerGame: Double,
    val blocksPerGame: Double,
    val turnoversPerGame: Double,
    val fieldGoalsMadePerGame: Double,
    val fieldGoalsAttemptedPerGame: Double,
    val fieldGoalPct: Double,
    val threePointsMadePerGame: Double,
    val threePointsAttemptedPerGame: Double,
    val threePointPct: Double,
    val freeThrowsMadePerGame: Double,
    val freeThrowsAttemptedPerGame: Double,
    val freeThrowPct: Double,
)

data class PlayerStatsSummary(
    val gamesPlayedSeason: Int,
    val seasonAverages: PlayerRateStats,
    val seasonTotals: PlayerCountingStats,
    val careerGamesPlayed: Int,
    val careerAverages: PlayerRateStats,
    val careerTotals: PlayerCountingStats,
)

object PlayerStatsEngine {
    fun seasonSummary(
        gamesPlayed: Int,
        seasonTotals: PlayerCountingStats,
        priorSeasonTotals: List<PlayerCountingStats> = emptyList(),
        priorGamesPlayed: List<Int> = emptyList(),
    ): PlayerStatsSummary {
        val careerTotals = priorSeasonTotals.fold(seasonTotals, PlayerCountingStats::plus)
        val careerGp = priorGamesPlayed.sum() + gamesPlayed
        return PlayerStatsSummary(
            gamesPlayedSeason = gamesPlayed,
            seasonAverages = rateStats(seasonTotals, gamesPlayed),
            seasonTotals = seasonTotals,
            careerGamesPlayed = careerGp,
            careerAverages = rateStats(careerTotals, careerGp),
            careerTotals = careerTotals,
        )
    }

    fun rateStats(totals: PlayerCountingStats, gamesPlayed: Int): PlayerRateStats {
        if (gamesPlayed <= 0) {
            return PlayerRateStats(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0,
                0.0, 0.0, 0.0,
                0.0, 0.0, 0.0,
            )
        }
        val gp = gamesPlayed.toDouble()
        return PlayerRateStats(
            pointsPerGame = totals.points / gp,
            assistsPerGame = totals.assists / gp,
            reboundsPerGame = totals.rebounds / gp,
            stealsPerGame = totals.steals / gp,
            blocksPerGame = totals.blocks / gp,
            turnoversPerGame = totals.turnovers / gp,
            fieldGoalsMadePerGame = totals.fieldGoalsMade / gp,
            fieldGoalsAttemptedPerGame = totals.fieldGoalsAttempted / gp,
            fieldGoalPct = pct(totals.fieldGoalsMade, totals.fieldGoalsAttempted),
            threePointsMadePerGame = totals.threePointsMade / gp,
            threePointsAttemptedPerGame = totals.threePointsAttempted / gp,
            threePointPct = pct(totals.threePointsMade, totals.threePointsAttempted),
            freeThrowsMadePerGame = totals.freeThrowsMade / gp,
            freeThrowsAttemptedPerGame = totals.freeThrowsAttempted / gp,
            freeThrowPct = pct(totals.freeThrowsMade, totals.freeThrowsAttempted),
        )
    }

    private fun pct(made: Int, attempted: Int): Double =
        if (attempted <= 0) 0.0 else made.toDouble() / attempted.toDouble()

    fun formatPct(value: Double): String = "${(value * 1000).roundToInt() / 10.0}%"

    fun formatPerGame(value: Double): String = "%.1f".format(value)
}

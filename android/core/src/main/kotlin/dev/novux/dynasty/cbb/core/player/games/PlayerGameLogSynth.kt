package dev.novux.dynasty.cbb.core.player.games

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

object PlayerGameLogSynth {
    private val opponentPool = listOf(
        "North Ridge",
        "Cedar Valley",
        "Lakehurst",
        "Riverton State",
        "East Marshall",
        "Westmont",
        "Highland Tech",
        "Summit U",
        "Fairview",
        "Oak Creek",
        "Prairie A&M",
        "Blue Ridge",
        "Coastal Tech",
        "Ironwood",
        "Lakeside",
    )

    fun buildLog(
        player: DynastyPlayer,
        seasonIndex: Int,
        gamesPlayed: Int,
        seasonTotals: PlayerCountingStats,
    ): List<PlayerGameLogEntry> {
        if (gamesPlayed <= 0) return emptyList()
        val rnd = Random(player.playerId.hashCode().toLong() xor seasonIndex.toLong())
        val winBias = (player.ratings.ovr - 50).coerceIn(-20, 25)
        val pts = distribute(seasonTotals.points, gamesPlayed) { rnd.nextDouble(0.6, 1.4) }
        val ast = distribute(seasonTotals.assists, gamesPlayed) { rnd.nextDouble(0.5, 1.5) }
        val reb = distribute(seasonTotals.rebounds, gamesPlayed) { rnd.nextDouble(0.5, 1.5) }
        val stl = distribute(seasonTotals.steals, gamesPlayed) { rnd.nextDouble(0.3, 1.7) }
        val blk = distribute(seasonTotals.blocks, gamesPlayed) { rnd.nextDouble(0.2, 1.8) }
        val to = distribute(seasonTotals.turnovers, gamesPlayed) { rnd.nextDouble(0.5, 1.5) }
        val fgm = distribute(seasonTotals.fieldGoalsMade, gamesPlayed) { rnd.nextDouble(0.7, 1.3) }
        val fga = distribute(seasonTotals.fieldGoalsAttempted, gamesPlayed) { rnd.nextDouble(0.7, 1.3) }
        val tpm = distribute(seasonTotals.threePointsMade, gamesPlayed) { rnd.nextDouble(0.4, 1.6) }
        val tpa = distribute(seasonTotals.threePointsAttempted, gamesPlayed) { rnd.nextDouble(0.5, 1.5) }
        val ftm = distribute(seasonTotals.freeThrowsMade, gamesPlayed) { rnd.nextDouble(0.5, 1.5) }
        val fta = distribute(seasonTotals.freeThrowsAttempted, gamesPlayed) { rnd.nextDouble(0.5, 1.5) }

        return (0 until gamesPlayed).map { index ->
            val week = index + 1
            val opponent = opponentPool[(index + seasonIndex) % opponentPool.size]
            val isHome = rnd.nextBoolean()
            val won = rnd.nextInt(100) < (52 + winBias)
            val teamScore = rnd.nextInt(62, 92)
            val margin = rnd.nextInt(3, 18)
            val oppScore = if (won) max(55, teamScore - margin) else teamScore + margin
            PlayerGameLogEntry(
                gameKey = "${player.playerId}_s${seasonIndex}_g$week",
                rsWeekIndex = week,
                opponentLabel = opponent,
                isHome = isHome,
                teamScore = teamScore,
                opponentScore = oppScore,
                points = pts[index],
                assists = ast[index],
                rebounds = reb[index],
                steals = stl[index],
                blocks = blk[index],
                turnovers = to[index],
                fieldGoalsMade = fgm[index],
                fieldGoalsAttempted = max(fgm[index], fga[index]),
                threePointsMade = tpm[index],
                threePointsAttempted = max(tpm[index], tpa[index]),
                freeThrowsMade = ftm[index],
                freeThrowsAttempted = max(ftm[index], fta[index]),
                minutes = rnd.nextInt(18, 36),
            )
        }.sortedByDescending { it.rsWeekIndex }
    }

    private fun distribute(
        total: Int,
        count: Int,
        weight: () -> Double,
    ): List<Int> {
        if (count <= 0) return emptyList()
        if (total <= 0) return List(count) { 0 }
        val weights = List(count) { weight() }
        val sum = weights.sum()
        val raw = weights.map { total * (it / sum) }
        val floored = raw.map { it.toInt() }.toMutableList()
        var remainder = total - floored.sum()
        val order = raw.mapIndexed { i, v -> i to (v - v.toInt()) }
            .sortedByDescending { it.second }
        var o = 0
        while (remainder > 0 && o < order.size) {
            floored[order[o].first]++
            remainder--
            o++
        }
        return floored
    }
}

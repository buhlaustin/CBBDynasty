package dev.novux.dynasty.cbb.core.player.stats

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Synthesizes counting stats from games played + ratings until possession sim persists player box lines.
 * Deterministic per player + GP so stats are stable across card re-opens.
 */
object PlayerStatsSynth {
    fun totalsForSeason(player: DynastyPlayer, gamesPlayed: Int): PlayerCountingStats {
        if (gamesPlayed <= 0) return PlayerCountingStats.ZERO
        val seed = player.playerId.hashCode().toLong() xor gamesPlayed.toLong()
        val rnd = Random(seed)
        val r = player.ratings
        val ppg = (r.off / 7.5 + r.ovr / 45.0 + rnd.nextDouble(0.5, 2.5)).coerceIn(4.0, 26.0)
        val points = (ppg * gamesPlayed).roundToInt()
        val apg = (r.passing / 22.0 + rnd.nextDouble(0.2, 1.2)).coerceIn(0.5, 8.0)
        val rpg = (r.rebounding / 18.0 + rnd.nextDouble(0.3, 1.5)).coerceIn(1.0, 12.0)
        val spg = (r.stealing / 35.0 + rnd.nextDouble(0.05, 0.5)).coerceIn(0.1, 2.5)
        val bpg = (r.blocking / 40.0 + rnd.nextDouble(0.05, 0.4)).coerceIn(0.0, 3.0)
        val tpg = (r.handling / 30.0 + rnd.nextDouble(0.5, 1.8)).coerceIn(0.8, 4.5)

        val fgaPerGame = (ppg * 0.85 + rnd.nextDouble(2.0, 5.0)).coerceAtLeast(6.0)
        val fga = (fgaPerGame * gamesPlayed).roundToInt()
        val fgPct = (0.38 + r.shootingInside / 250.0 + rnd.nextDouble(-0.04, 0.06)).coerceIn(0.32, 0.62)
        val fgm = (fga * fgPct).roundToInt()

        val tpa = (fga * (0.28 + r.shootingThree / 400.0)).roundToInt().coerceAtMost(fga)
        val tpPct = (0.30 + r.shootingThree / 350.0).coerceIn(0.25, 0.45)
        val tpm = (tpa * tpPct).roundToInt()

        val fta = (fga * 0.35 + gamesPlayed * rnd.nextDouble(1.0, 3.0)).roundToInt()
        val ftPct = (0.68 + r.shootingMid / 500.0).coerceIn(0.55, 0.92)
        val ftm = (fta * ftPct).roundToInt()

        return PlayerCountingStats(
            points = points,
            assists = (apg * gamesPlayed).roundToInt(),
            rebounds = (rpg * gamesPlayed).roundToInt(),
            steals = (spg * gamesPlayed).roundToInt(),
            blocks = (bpg * gamesPlayed).roundToInt(),
            turnovers = (tpg * gamesPlayed).roundToInt(),
            fieldGoalsMade = fgm,
            fieldGoalsAttempted = fga,
            threePointsMade = tpm,
            threePointsAttempted = tpa,
            freeThrowsMade = ftm,
            freeThrowsAttempted = fta,
        )
    }
}

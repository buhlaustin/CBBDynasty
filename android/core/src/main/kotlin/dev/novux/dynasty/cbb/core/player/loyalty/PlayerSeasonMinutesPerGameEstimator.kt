package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.player.games.PlayerGameLogEntry

/** Season minutes per game from archived/sim game logs ([MEGA-COACH-170]). */
object PlayerSeasonMinutesPerGameEstimator {
    fun average(entries: List<PlayerGameLogEntry>, gamesPlayed: Int): Double? {
        if (gamesPlayed <= 0 || entries.isEmpty()) return null
        val sample = entries.take(gamesPlayed.coerceAtMost(entries.size))
        if (sample.isEmpty()) return null
        val totalMinutes = sample.sumOf { it.minutes.coerceAtLeast(0) }
        return totalMinutes.toDouble() / sample.size
    }
}

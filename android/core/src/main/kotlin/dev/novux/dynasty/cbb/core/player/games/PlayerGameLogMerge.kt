package dev.novux.dynasty.cbb.core.player.games

import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats

internal fun PlayerGameLogEntry.toCountingStats(): PlayerCountingStats = PlayerCountingStats(
    points = points,
    assists = assists,
    rebounds = rebounds,
    steals = steals,
    blocks = blocks,
    turnovers = turnovers,
    fieldGoalsMade = fieldGoalsMade,
    fieldGoalsAttempted = fieldGoalsAttempted,
    threePointsMade = threePointsMade,
    threePointsAttempted = threePointsAttempted,
    freeThrowsMade = freeThrowsMade,
    freeThrowsAttempted = freeThrowsAttempted,
)

object PlayerGameLogMerge {
    fun mergeForSeason(
        player: DynastyPlayer,
        seasonIndex: Int,
        gamesPlayed: Int,
        seasonTotals: PlayerCountingStats,
        archived: List<PlayerGameLogEntry>,
    ): List<PlayerGameLogEntry> {
        if (gamesPlayed <= 0) return emptyList()
        val sim = archived.filter { it.fromSimArchive }.sortedByDescending { it.rsWeekIndex }
        if (sim.size >= gamesPlayed) return sim.take(gamesPlayed)
        val remaining = gamesPlayed - sim.size
        if (remaining <= 0) return sim
        val simTotals = sim.fold(PlayerCountingStats.ZERO) { acc, line -> acc + line.toCountingStats() }
        val tailTotals = seasonTotals.minus(simTotals)
        val synth = PlayerGameLogSynth.buildLog(player, seasonIndex, remaining, tailTotals)
        return (sim + synth).sortedByDescending { it.rsWeekIndex }
    }
}

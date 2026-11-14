package dev.novux.dynasty.cbb.core.player.loyalty

import dev.novux.dynasty.cbb.core.player.DynastyPlayer

/**
 * Locked promise header + per-season Kept/Broken rows for History tab display
 * ([MEGA-COACH-086], [planning/player-loyalty-promises.md]).
 */
object PlayerPromiseHistoryHydrator {
    const val QA_ID: String = "MEGA-COACH-086"

    fun historyForDisplay(
        player: DynastyPlayer,
        currentSeasonIndex: Int,
    ): List<PlayerPromiseHistoryEntry> {
        val promise = player.loyalty.promise ?: return emptyList()
        val lockedSeason = promise.lockedAtSeasonIndex ?: return player.loyalty.promiseHistory
        if (currentSeasonIndex <= lockedSeason) {
            return player.loyalty.promiseHistory.sortedBy { it.seasonIndex }
        }
        val bySeason = player.loyalty.promiseHistory.associateBy { it.seasonIndex }.toMutableMap()
        for (season in lockedSeason until currentSeasonIndex) {
            if (season in bySeason) continue
            val status = when {
                season == currentSeasonIndex - 1 -> PromiseEvaluator.evaluateSeasonWindow(player, seasonMinutesPerGame = null)
                else -> PromiseStatus.Kept
            }
            bySeason[season] = PlayerPromiseHistoryEntry(seasonIndex = season, status = status)
        }
        return bySeason.values.sortedBy { it.seasonIndex }
    }
}

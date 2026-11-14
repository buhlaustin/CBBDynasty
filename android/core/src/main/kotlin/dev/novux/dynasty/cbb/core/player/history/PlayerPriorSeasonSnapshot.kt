package dev.novux.dynasty.cbb.core.player.history

import dev.novux.dynasty.cbb.core.player.stats.PlayerCountingStats

/** One archived season line on the player card History tab ([planning/player-cards.md#history]). */
data class PlayerPriorSeasonSnapshot(
    val seasonIndex: Int,
    val gamesPlayed: Int,
    val totals: PlayerCountingStats,
    val schoolDisplayName: String? = null,
) {
    fun showsTransferFrom(currentSchoolDisplayName: String): Boolean {
        val prior = schoolDisplayName?.trim().orEmpty()
        val current = currentSchoolDisplayName.trim()
        return prior.isNotEmpty() && current.isNotEmpty() && !prior.equals(current, ignoreCase = true)
    }

    fun pointsPerGame(): Double =
        if (gamesPlayed <= 0) 0.0 else totals.points.toDouble() / gamesPlayed.toDouble()

    fun assistsPerGame(): Double =
        if (gamesPlayed <= 0) 0.0 else totals.assists.toDouble() / gamesPlayed.toDouble()

    fun reboundsPerGame(): Double =
        if (gamesPlayed <= 0) 0.0 else totals.rebounds.toDouble() / gamesPlayed.toDouble()
}

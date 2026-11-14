package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect

/**
 * Fires “Review {name} — #{rank}” when a **board** (watchlist) prospect’s national Top 60 rank
 * changes ([planning/recruiting-engine.md#in-season-dashboard-tasks], MEGA-RECR-147).
 */
object RecruitingTop60MovementPolicy {
    const val QA_ID: String = "MEGA-RECR-147"

    data class Movement(
        val prospectId: String,
        val prospectName: String,
        val rank: Int,
        val previousRank: Int?,
    )

    fun detectMovements(
        watchlistIds: Set<String>,
        prospectsById: Map<String, RecruitProspect>,
        previousRankByProspect: Map<String, Int>,
        currentRankByProspect: Map<String, Int>,
    ): List<Movement> =
        watchlistIds.mapNotNull { prospectId ->
            val current = currentRankByProspect[prospectId] ?: return@mapNotNull null
            val previous = previousRankByProspect[prospectId]
            if (previous != null && previous == current) return@mapNotNull null
            val name = prospectsById[prospectId]?.displayName ?: prospectId
            Movement(prospectId, name, current, previous)
        }

    fun snapshotAfterObserve(
        watchlistIds: Set<String>,
        currentRankByProspect: Map<String, Int>,
    ): Map<String, Int> =
        watchlistIds.mapNotNull { id ->
            currentRankByProspect[id]?.let { id to it }
        }.toMap()
}

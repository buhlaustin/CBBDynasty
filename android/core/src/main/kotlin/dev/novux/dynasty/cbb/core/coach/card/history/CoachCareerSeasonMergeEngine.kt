package dev.novux.dynasty.cbb.core.coach.card.history

/** Merges persisted career seasons with live in-season row for History tab ([MEGA-COACH-193]). */
object CoachCareerSeasonMergeEngine {
    fun merge(
        persisted: List<CoachSeasonHistoryRow>,
        liveRows: List<CoachSeasonHistoryRow>,
        currentSeasonIndex: Int,
    ): List<CoachSeasonHistoryRow> {
        val persistedBy = persisted.associateBy { it.seasonIndex }
        val liveBy = liveRows.associateBy { it.seasonIndex }
        val seasons = (persistedBy.keys + liveBy.keys).distinct().sortedDescending()
        return seasons.mapNotNull { seasonIndex ->
            when {
                seasonIndex == currentSeasonIndex -> liveBy[seasonIndex] ?: persistedBy[seasonIndex]
                else -> persistedBy[seasonIndex] ?: liveBy[seasonIndex]
            }
        }
    }
}

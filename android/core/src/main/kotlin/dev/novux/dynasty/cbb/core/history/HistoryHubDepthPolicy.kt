package dev.novux.dynasty.cbb.core.history

/** Record book tab merges year-card notes, champions, and GOAT anchors ([MEGA-SHELL-098]). */
object HistoryHubDepthPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-098"

    fun majorRecordFeedLines(snapshot: HistoryHubSnapshot): List<String> {
        val recordLines = HistoryHubSurfacePolicy.recordBookLines(snapshot)
        val championLines = snapshot.yearCards.mapNotNull { card ->
            card.ncbaChampionSchool?.let { school ->
                "Season ${card.seasonIndex + 1} champion: $school"
            }
        }
        val goatLines = snapshot.goatOverall.take(5).map { entry ->
            "GOAT #${entry.rank}: ${entry.displayName} (${entry.schoolLabel})"
        }
        return (recordLines + championLines + goatLines).distinct().sorted()
    }
}
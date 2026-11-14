package dev.novux.dynasty.cbb.core.history

/** History hub surfaces GOAT, archive, and record book lines ([MEGA-GDD-062]). */
object HistoryHubSurfacePolicy {
    const val MEGA_ID: String = "MEGA-GDD-062"

    fun recordBookLines(snapshot: HistoryHubSnapshot): List<String> =
        snapshot.yearCards
            .flatMap { it.recordsNoted }
            .distinct()
            .sorted()
}
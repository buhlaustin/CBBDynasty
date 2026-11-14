package dev.novux.dynasty.cbb.core.coach.card.history

/** One head-coach season on the History tab ([MEGA-COACH-111]). */
typealias CoachSeasonHistory = CoachSeasonHistoryRow

/** Collapsed row + expandable detail for a coached season. */
data class CoachSeasonHistoryRow(
    val seasonIndex: Int,
    val schoolDisplayName: String,
    val recordLabel: String,
    val conferenceRecordLabel: String,
    val postseasonSummary: String,
    val highlightLabels: List<String> = emptyList(),
    val detail: CoachSeasonHistoryDetail,
)

data class CoachSeasonHistoryDetail(
    val scheduleSummaryLine: String,
    val tournamentPathLine: String,
    val newsTagLines: List<String>,
)

object CoachSeasonHistoryPolicy {
    const val MEGA_ID: String = "MEGA-COACH-111"
}

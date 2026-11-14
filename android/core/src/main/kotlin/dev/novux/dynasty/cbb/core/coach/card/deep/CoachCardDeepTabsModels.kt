package dev.novux.dynasty.cbb.core.coach.card.deep

import dev.novux.dynasty.cbb.core.coach.card.history.CoachSeasonHistoryRow

/** Player coached into national or conference hall recognition. */
data class CoachHofResumeEntry(
    val playerId: String,
    val displayName: String,
    val schoolLabel: String,
    val honorSeasonIndex: Int,
    val isNational: Boolean,
)

/** Draft departure from the coach's roster. */
data class CoachDraftResumeEntry(
    val playerId: String,
    val displayName: String,
    val draftSeasonIndex: Int,
    val pickSummary: String?,
    val schoolLabel: String,
    val drafted: Boolean,
)

data class CoachCardDeepTabsSnapshot(
    val dynastyId: String,
    val seasonHistory: List<CoachSeasonHistoryRow>,
    val hallOfFame: List<CoachHofResumeEntry>,
    val draftDepartures: List<CoachDraftResumeEntry>,
) {
    companion object {
        fun empty(dynastyId: String) = CoachCardDeepTabsSnapshot(
            dynastyId = dynastyId,
            seasonHistory = emptyList(),
            hallOfFame = emptyList(),
            draftDepartures = emptyList(),
        )
    }
}

package dev.novux.dynasty.cbb.core.coach.card.history

import org.junit.Assert.assertEquals
import org.junit.Test

class CoachCareerSeasonMergeEngineTest {
    @Test
    fun prefersPersistedForPastSeasonsAndLiveForCurrent() {
        val persisted = listOf(
            row(1, "10-5"),
            row(2, "8-7"),
        )
        val live = listOf(
            row(2, "9-6"),
            row(3, "4-0"),
        )
        val merged = CoachCareerSeasonMergeEngine.merge(persisted, live, currentSeasonIndex = 3)
        assertEquals(listOf(3, 2, 1), merged.map { it.seasonIndex })
        assertEquals("4-0", merged.first { it.seasonIndex == 3 }.recordLabel)
        assertEquals("8-7", merged.first { it.seasonIndex == 2 }.recordLabel)
    }

    private fun row(seasonIndex: Int, record: String) = CoachSeasonHistoryRow(
        seasonIndex = seasonIndex,
        schoolDisplayName = "State",
        recordLabel = record,
        conferenceRecordLabel = "—",
        postseasonSummary = "—",
        detail = CoachSeasonHistoryDetail("s", "t", emptyList()),
    )
}

package dev.novux.dynasty.cbb.core.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryHubSurfacePolicyTest {
    @Test
    fun aggregatesDistinctRecordLines() {
        val snapshot = HistoryHubSnapshot(
            dynastyId = "d1",
            seasonIndex = 2,
            yearCards = listOf(
                HistoryYearCard(
                    seasonIndex = 1,
                    ncbaChampionSchool = null,
                    ncbaMvpLine = null,
                    nationalHofInductees = emptyList(),
                    goatNumberOne = null,
                    recordsNoted = listOf("Most wins", "Triple-doubles"),
                ),
                HistoryYearCard(
                    seasonIndex = 2,
                    ncbaChampionSchool = null,
                    ncbaMvpLine = null,
                    nationalHofInductees = emptyList(),
                    goatNumberOne = null,
                    recordsNoted = listOf("Most wins", "50-point game"),
                ),
            ),
            goatOverall = emptyList(),
            goatByPosition = emptyMap(),
        )
        assertEquals(
            listOf("50-point game", "Most wins", "Triple-doubles"),
            HistoryHubSurfacePolicy.recordBookLines(snapshot),
        )
    }
}
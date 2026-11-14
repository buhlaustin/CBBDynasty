package dev.novux.dynasty.cbb.core.save

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastyExportEngineTest {
    @Test
    fun build_producesVersionedJson() {
        val summary = SaveSlotSummary(
            slotIndex = 2,
            isEmpty = false,
            dynastyId = "dyn-export-test",
            schoolDisplayName = "Ohio State",
            coachDisplayName = "Coach Ryan",
            seasonLabel = "2027",
            wins = 12,
            losses = 4,
        )
        val metadata = DynastyBootstrap.newDynasty(
            dynastyId = "dyn-export-test",
            packId = DynastySchema.DEFAULT_PACK_ID,
        ).copy(seasonIndex = 3, phase = CalendarPhase.RegularSeason)
        val checkpoint = WeekCheckpointState(
            activePosition = CalendarPosition(
                seasonIndex = 3,
                phase = CalendarPhase.RegularSeason,
                weekIndex = 8,
            ),
        )
        val archive = DynastyExportEngine.build(
            slotIndex = 2,
            summary = summary,
            metadata = metadata,
            weekCheckpoint = checkpoint,
            rosterPlayerCount = 15,
            exportedAtEpochMillis = 1_700_000_000_000L,
        )
        val json = archive.toJson()
        assertTrue(json.contains("dyn-export-test"))
        assertTrue(json.contains("Ohio State"))
        assertTrue(json.contains("\"exportFormatVersion\":1"))
        assertTrue(json.contains("RegularSeason"))
    }
}

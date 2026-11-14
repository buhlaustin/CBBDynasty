package dev.novux.dynasty.cbb.core.save

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Fixture representing a dynasty saved on a prior app build, loaded after an APK update (ADR-001).
 */
class SaveLoadAfterUpdateFixtureTest {
    private val slot =
        DynastySaveValidator.SlotSnapshot(
            slotIndex = 0,
            dynastyId = "dynasty-pre-update",
            packId = DynastySchema.DEFAULT_PACK_ID,
            isEmpty = false,
        )

    private val metadataSnap =
        DynastySaveValidator.MetadataSnapshot(
            dynastyId = "dynasty-pre-update",
            packId = DynastySchema.DEFAULT_PACK_ID,
            seasonIndex = 2,
            phase = CalendarPhase.RegularSeason,
        )

    private val dynastyHeader =
        DynastyMetadata(
            dynastyId = "dynasty-pre-update",
            packId = DynastySchema.DEFAULT_PACK_ID,
            seasonIndex = 2,
            phase = CalendarPhase.RegularSeason,
            dynastySchemaVersion = 1,
            balanceRulesetId = DynastySchema.DEFAULT_BALANCE_RULESET_ID,
            balanceSchemaVersion = DynastySchema.DEFAULT_BALANCE_SCHEMA_VERSION,
            simTuningId = DynastySchema.DEFAULT_SIM_TUNING_ID,
            simSchemaVersion = DynastySchema.DEFAULT_SIM_SCHEMA_VERSION,
        )

    @Test
    fun loadFixture_afterAppUpdate_passesSaveValidator() {
        assertNull(DynastySaveValidator.validateBeforeLoad(slot, metadataSnap))
    }

    @Test
    fun loadFixture_pinsBalanceAndSimBundlesFromPriorBuild() {
        assertEquals(DynastySchema.DEFAULT_BALANCE_RULESET_ID, dynastyHeader.balanceRulesetId)
        assertEquals(DynastySchema.DEFAULT_SIM_TUNING_ID, dynastyHeader.simTuningId)
        assertEquals(DynastySchema.DEFAULT_BALANCE_SCHEMA_VERSION, dynastyHeader.balanceSchemaVersion)
        assertEquals(DynastySchema.DEFAULT_SIM_SCHEMA_VERSION, dynastyHeader.simSchemaVersion)
    }

    @Test
    fun loadFixture_midSeasonCalendar_passesValidation() {
        val calendar =
            DynastySaveValidator.CalendarSnapshot(
                seasonIndex = 2,
                phase = CalendarPhase.RegularSeason,
                activeWeekIndex = 8,
                committedSeasonIndex = 2,
                committedPhase = CalendarPhase.RegularSeason,
                committedWeekIndex = 7,
            )
        assertNull(DynastySaveValidator.validateCalendar(calendar))
    }
}

package dev.novux.dynasty.cbb.database.save

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.novux.dynasty.cbb.core.save.DynastyLoadFailure
import dev.novux.dynasty.cbb.core.save.DynastySaveValidator
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.save.toSummary
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Occupied slot header without dynasty metadata simulates truncated/corrupt saves ([MEGA-QA-030]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CorruptOccupiedSlotPersistenceTest {
    private lateinit var database: DynastyDatabase

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, DynastyDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun occupiedSlotWithoutMetadata_failsValidateBeforeLoad() = runBlocking {
        val dynastyId = "orphan-dynasty-id"
        database.saveSlotDao().upsert(
            SaveSlotEntity(
                slotIndex = 1,
                dynastyId = dynastyId,
                lastPlayedAtEpochMillis = 1L,
                schoolDisplayName = "Ghost U",
                coachDisplayName = "Coach",
                seasonLabel = "2025-26",
                wins = 0,
                losses = 0,
                packId = DynastySchema.DEFAULT_PACK_ID,
            ),
        )
        val slot = database.saveSlotDao().getByIndex(1)!!.toSummary()
        val failure = DynastySaveValidator.validateBeforeLoad(
            DynastySaveValidator.SlotSnapshot(
                slotIndex = 1,
                dynastyId = slot.dynastyId,
                packId = slot.packId,
                isEmpty = false,
            ),
            metadata = null,
        )
        assertEquals(DynastyLoadFailure.MissingMetadata(dynastyId), failure)
    }

    @Test
    fun deleteSlot_clearsHeaderAfterOrphanOccupiedRow() = runBlocking {
        database.saveSlotDao().upsert(
            SaveSlotEntity(
                slotIndex = 2,
                dynastyId = "missing-meta",
                lastPlayedAtEpochMillis = 1L,
                schoolDisplayName = "Ghost",
                coachDisplayName = "Coach",
                seasonLabel = "2025-26",
                wins = 0,
                losses = 0,
                packId = DynastySchema.DEFAULT_PACK_ID,
            ),
        )
        database.saveSlotDao().clearSlot(2)
        val cleared = database.saveSlotDao().getByIndex(2)!!
        assertTrue(cleared.dynastyId.isNullOrBlank())
    }
}

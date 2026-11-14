package dev.novux.dynasty.cbb.database.qa

import androidx.room.withTransaction
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.core.qa.ReleaseRoomR8Path
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.calendar.toCalendarEntity
import dev.novux.dynasty.cbb.database.calendar.toCheckpointState
import dev.novux.dynasty.cbb.database.dynasty.DynastyMetadataEntity
import dev.novux.dynasty.cbb.database.dynasty.toDomain
import dev.novux.dynasty.cbb.database.dynasty.toEntity
import dev.novux.dynasty.cbb.database.save.SaveSlotEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Release Room read/write smoke — three RS week advances ([MEGA-QA-153]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ReleaseRoomR8SmokeTest {
    private lateinit var database: DynastyDatabase
    private val dynastyId = "dynasty-room-r8-smoke"

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, DynastyDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        val bootstrap = WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason)
        database.saveSlotDao().upsert(
            SaveSlotEntity(
                slotIndex = 0,
                dynastyId = dynastyId,
                lastPlayedAtEpochMillis = System.currentTimeMillis(),
                schoolDisplayName = "Smoke U",
                coachDisplayName = "Coach",
                seasonLabel = "Season 1",
                wins = 0,
                losses = 0,
                packId = DynastySchema.DEFAULT_PACK_ID,
            ),
        )
        database.dynastyMetadataDao().upsert(
            DynastyMetadataEntity(
                dynastyId = dynastyId,
                seasonIndex = 1,
                phase = CalendarPhase.RegularSeason,
                packId = DynastySchema.DEFAULT_PACK_ID,
                dynastySchemaVersion = DynastySchema.CURRENT_VERSION,
                balanceRulesetId = DynastySchema.DEFAULT_BALANCE_RULESET_ID,
                balanceSchemaVersion = DynastySchema.DEFAULT_BALANCE_SCHEMA_VERSION,
                simTuningId = DynastySchema.DEFAULT_SIM_TUNING_ID,
                simSchemaVersion = DynastySchema.DEFAULT_SIM_SCHEMA_VERSION,
            ),
        )
        database.dynastyCalendarDao().upsert(bootstrap.toCalendarEntity(dynastyId))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun threeRegularSeasonAdvances_persistLoadableRows() = runBlocking {
        val calendarDao = database.dynastyCalendarDao()
        val provisionalDao = database.provisionalGameDao()
        val metadataDao = database.dynastyMetadataDao()
        val slotDao = database.saveSlotDao()

        var state = calendarDao.getByDynastyId(dynastyId)!!.toCheckpointState(provisionalDao.listForDynasty(dynastyId))
        val startWeek = state.activePosition.weekIndex

        repeat(ReleaseRoomR8Path.TARGET_ADVANCE_WEEKS) {
            val advance = state.advanceWeek()
            database.withTransaction {
                calendarDao.upsert(advance.nextState.toCalendarEntity(dynastyId))
                provisionalDao.deleteAllForDynasty(dynastyId)
                val metadata = metadataDao.getById(dynastyId)?.toDomain()
                if (metadata != null) {
                    val active = advance.nextState.activePosition
                    metadataDao.upsert(
                        metadata.copy(
                            seasonIndex = active.seasonIndex,
                            phase = active.phase,
                        ).toEntity(),
                    )
                }
                val slot = slotDao.getByIndex(0)!!
                slotDao.upsert(slot.copy(lastPlayedAtEpochMillis = System.currentTimeMillis()))
            }
            state = calendarDao.getByDynastyId(dynastyId)!!
                .toCheckpointState(provisionalDao.listForDynasty(dynastyId))
        }

        assertEquals(
            startWeek + ReleaseRoomR8Path.TARGET_ADVANCE_WEEKS,
            state.activePosition.weekIndex,
        )
        val slot = slotDao.getByIndex(0)
        assertNotNull(slot)
        assertEquals(dynastyId, slot!!.dynastyId)
        val meta = metadataDao.getById(dynastyId)
        assertEquals(state.activePosition.phase, meta!!.phase)
    }
}

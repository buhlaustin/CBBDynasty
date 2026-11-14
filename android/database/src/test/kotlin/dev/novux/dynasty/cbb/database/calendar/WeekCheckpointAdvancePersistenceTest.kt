package dev.novux.dynasty.cbb.database.calendar

import androidx.room.withTransaction
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.dynasty.DynastyMetadataEntity
import dev.novux.dynasty.cbb.database.dynasty.toDomain
import dev.novux.dynasty.cbb.database.dynasty.toEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class WeekCheckpointAdvancePersistenceTest {
    private lateinit var database: DynastyDatabase
    private val dynastyId = "dynasty-mega-qa-028"

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, DynastyDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        val bootstrap = WeekCheckpointState.bootstrap(1, CalendarPhase.RegularSeason)
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
    fun advanceWeek_singleTransaction_producesLoadableCalendarRow() = runBlocking {
        val calendarDao = database.dynastyCalendarDao()
        val provisionalDao = database.provisionalGameDao()
        val metadataDao = database.dynastyMetadataDao()
        val row = calendarDao.getByDynastyId(dynastyId)!!
        val loaded = row.toCheckpointState(provisionalDao.listForDynasty(dynastyId))
        val mark = ProvisionalGameMark(
            gameId = "game-rs-1",
            homeScore = 71,
            awayScore = 68,
            simGeneration = 1,
        )
        val preAdvance = loaded.stageSim(mark)
        val advanceResult = preAdvance.advanceWeek()
        database.withTransaction {
            calendarDao.upsert(advanceResult.nextState.toCalendarEntity(dynastyId))
            provisionalDao.deleteAllForDynasty(dynastyId)
            val metadata = metadataDao.getById(dynastyId)?.toDomain()
            if (metadata != null) {
                val active = advanceResult.nextState.activePosition
                metadataDao.upsert(
                    metadata.copy(
                        seasonIndex = active.seasonIndex,
                        phase = active.phase,
                    ).toEntity(),
                )
            }
        }
        val reloadedRow = calendarDao.getByDynastyId(dynastyId)
        assertNotNull(reloadedRow)
        val reloaded = reloadedRow!!.toCheckpointState(provisionalDao.listForDynasty(dynastyId))
        assertEquals(advanceResult.nextState.activePosition, reloaded.activePosition)
        assertEquals(advanceResult.nextState.committedThrough, reloaded.committedThrough)
        assertEquals(0, reloaded.provisionalGames.size)
        val meta = metadataDao.getById(dynastyId)
        assertEquals(reloaded.activePosition.seasonIndex, meta!!.seasonIndex)
        assertEquals(reloaded.activePosition.phase, meta.phase)
    }
}

package dev.novux.dynasty.cbb.database.save

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.platform.app.InstrumentationRegistry
import dev.novux.dynasty.cbb.core.coach.CoachCreateProfile
import dev.novux.dynasty.cbb.core.roster.RosterConstants
import dev.novux.dynasty.cbb.core.roster.SeasonOneRosterGenerator
import dev.novux.dynasty.cbb.core.save.DynastyBootstrap
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.core.save.NewGameDraft
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.calendar.bootstrapCalendarEntity
import dev.novux.dynasty.cbb.database.dynasty.toCoachEntity
import dev.novux.dynasty.cbb.database.dynasty.toEntity
import dev.novux.dynasty.cbb.database.roster.toEntities
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Mirrors `SaveSlotRepository.signNewGameFromWizard` DB transaction ([MEGA-QA-029]). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NewGameSignTransactionRollbackTest {
    private lateinit var database: DynastyDatabase

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, DynastyDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        database.saveSlotDao().upsertAll(
            ensureSlotRows(database.saveSlotDao().listAll()),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun failedTransactionBeforeSlot_leavesSlotEmptyAndNoOrphanCoach() = runBlocking {
        val slotDao = database.saveSlotDao()
        val slotIndex = 0
        val dynastyId = "dynasty-sign-rollback"
        val draft = NewGameDraft(
            coach = CoachCreateProfile(firstName = "Pat", lastName = "Summitt"),
            selectedTeamId = "team-conf-test-001",
            schoolSummaryAcknowledged = true,
        )
        val metadata = DynastyBootstrap.newDynasty(
            dynastyId = dynastyId,
            packId = DynastySchema.DEFAULT_PACK_ID,
            balanceRulesetId = DynastySchema.DEFAULT_BALANCE_RULESET_ID,
            balanceSchemaVersion = DynastySchema.DEFAULT_BALANCE_SCHEMA_VERSION,
            simTuningId = DynastySchema.DEFAULT_SIM_TUNING_ID,
            simSchemaVersion = DynastySchema.DEFAULT_SIM_SCHEMA_VERSION,
        )
        try {
            database.withTransaction {
                database.dynastyMetadataDao().upsert(metadata.toEntity())
                database.dynastyCoachDao().upsert(draft.toCoachEntity(dynastyId))
                error("simulate kill mid-sign")
            }
        } catch (_: IllegalStateException) {
        }
        val slot = slotDao.getByIndex(slotIndex)!!
        assertTrue(slot.dynastyId.isNullOrBlank())
        assertNull(database.dynastyCoachDao().getByDynastyId(dynastyId))
        assertNull(database.dynastyMetadataDao().getById(dynastyId))
    }

    @Test
    fun committedSignTransaction_writesCoachRosterAndSlotTogether() = runBlocking {
        val slotDao = database.saveSlotDao()
        val slotIndex = 0
        val dynastyId = "dynasty-sign-complete"
        val draft = NewGameDraft(
            coach = CoachCreateProfile(firstName = "Geno", lastName = "Auriemma"),
            selectedTeamId = "team-conf-test-001",
            schoolSummaryAcknowledged = true,
        )
        val metadata = DynastyBootstrap.newDynasty(
            dynastyId = dynastyId,
            packId = DynastySchema.DEFAULT_PACK_ID,
            balanceRulesetId = DynastySchema.DEFAULT_BALANCE_RULESET_ID,
            balanceSchemaVersion = DynastySchema.DEFAULT_BALANCE_SCHEMA_VERSION,
            simTuningId = DynastySchema.DEFAULT_SIM_TUNING_ID,
            simSchemaVersion = DynastySchema.DEFAULT_SIM_SCHEMA_VERSION,
        )
        val roster = SeasonOneRosterGenerator.generate(dynastyId).toEntities()
        database.withTransaction {
            database.dynastyMetadataDao().upsert(metadata.toEntity())
            database.dynastyCoachDao().upsert(draft.toCoachEntity(dynastyId))
            database.dynastyRosterDao().upsertAll(roster)
            database.dynastyCalendarDao().upsert(metadata.bootstrapCalendarEntity())
            slotDao.upsert(
                SaveSlotEntity(
                    slotIndex = slotIndex,
                    dynastyId = dynastyId,
                    lastPlayedAtEpochMillis = 1L,
                    schoolDisplayName = "Test U",
                    coachDisplayName = draft.coach.displayName(),
                    seasonLabel = "2025-26",
                    wins = 0,
                    losses = 0,
                    packId = DynastySchema.DEFAULT_PACK_ID,
                ),
            )
        }
        assertEquals(dynastyId, slotDao.getByIndex(slotIndex)!!.dynastyId)
        assertEquals(RosterConstants.SCHOLARSHIP_CAP, database.dynastyRosterDao().listForDynasty(dynastyId).size)
        assertEquals(dynastyId, database.dynastyCoachDao().getByDynastyId(dynastyId)!!.dynastyId)
    }
}

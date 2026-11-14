package dev.novux.dynasty.cbb.data.calendar

import androidx.room.withTransaction
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.save.DynastyMetadata
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.calendar.bootstrapCalendarEntity
import dev.novux.dynasty.cbb.database.calendar.toCalendarEntity
import dev.novux.dynasty.cbb.database.calendar.toCheckpointState
import dev.novux.dynasty.cbb.database.calendar.toEntity
import dev.novux.dynasty.cbb.database.dynasty.toEntity
import dev.novux.dynasty.cbb.database.dynasty.toDomain

class WeekCheckpointRepository(
    private val database: DynastyDatabase,
) {
    private val calendarDao = database.dynastyCalendarDao()
    private val provisionalDao = database.provisionalGameDao()
    private val metadataDao = database.dynastyMetadataDao()

    /** Loads existing checkpoint or null if never written (e.g. legacy row). */
    suspend fun loadCheckpoint(dynastyId: String): WeekCheckpointState? {
        val row = calendarDao.getByDynastyId(dynastyId) ?: return null
        val provisional = provisionalDao.listForDynasty(dynastyId)
        return row.toCheckpointState(provisional)
    }

    /** Repairs a missing calendar row from dynasty metadata (app-created saves always write both). */
    suspend fun repairCalendarFromMetadata(metadata: DynastyMetadata): WeekCheckpointState {
        val initial = metadata.bootstrapCalendarEntity()
        calendarDao.upsert(initial)
        return initial.toCheckpointState(emptyList())
    }

    suspend fun loadOrBootstrap(metadata: DynastyMetadata): WeekCheckpointState =
        loadCheckpoint(metadata.dynastyId)
            ?: repairCalendarFromMetadata(metadata)

    /** Persists sim staging (watch/fast-sim). Not used mid-`advanceDynastyWeek` — see MEGA-QA-028. */
    suspend fun persist(dynastyId: String, state: WeekCheckpointState) {
        database.withTransaction {
            calendarDao.upsert(state.toCalendarEntity(dynastyId))
            provisionalDao.deleteAllForDynasty(dynastyId)
            if (state.provisionalGames.isNotEmpty()) {
                provisionalDao.upsertAll(
                    state.provisionalGames.values.map { it.toEntity(dynastyId) },
                )
            }
        }
    }

    suspend fun stageSim(dynastyId: String, state: WeekCheckpointState, mark: ProvisionalGameMark): WeekCheckpointState {
        val next = state.stageSim(mark)
        persist(dynastyId, next)
        return next
    }

    suspend fun advanceWeek(
        dynastyId: String,
        state: WeekCheckpointState,
    ): WeekCheckpointState.AdvanceResult {
        val result = state.advanceWeek()
        database.withTransaction {
            calendarDao.upsert(result.nextState.toCalendarEntity(dynastyId))
            provisionalDao.deleteAllForDynasty(dynastyId)
            val metadata = metadataDao.getById(dynastyId)?.toDomain()
            if (metadata != null) {
                val active = result.nextState.activePosition
                metadataDao.upsert(
                    metadata.copy(
                        seasonIndex = active.seasonIndex,
                        phase = active.phase,
                    ).toEntity(),
                )
            }
        }
        return result
    }

    suspend fun deleteForDynasty(dynastyId: String) {
        database.withTransaction {
            calendarDao.deleteByDynastyId(dynastyId)
            provisionalDao.deleteAllForDynasty(dynastyId)
        }
    }

    suspend fun insertBootstrapCalendar(metadata: DynastyMetadata) {
        calendarDao.upsert(metadata.bootstrapCalendarEntity())
    }
}

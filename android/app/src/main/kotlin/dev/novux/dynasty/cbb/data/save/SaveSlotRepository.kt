package dev.novux.dynasty.cbb.data.save

import androidx.room.withTransaction
import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear
import dev.novux.dynasty.cbb.core.calendar.SeasonReviewGate
import dev.novux.dynasty.cbb.core.calendar.SeasonYearAdvanceEngine
import dev.novux.dynasty.cbb.core.save.DynastyBootstrap
import dev.novux.dynasty.cbb.core.save.DynastyExportArchive
import dev.novux.dynasty.cbb.core.save.DynastyExportEngine
import dev.novux.dynasty.cbb.core.save.DynastyWizardBootstrapPolicy
import dev.novux.dynasty.cbb.core.save.DynastyLoadFailure
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult
import dev.novux.dynasty.cbb.core.save.DynastyWeekAdvanceBlockReason
import dev.novux.dynasty.cbb.core.save.DynastySaveValidator
import dev.novux.dynasty.cbb.core.roster.SeasonOneRosterGenerator
import dev.novux.dynasty.cbb.core.save.NewGameDraft
import dev.novux.dynasty.cbb.core.save.NewGameSignValidator
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.save.SaveSlotSummary
import dev.novux.dynasty.cbb.core.save.SaveSlots
import dev.novux.dynasty.cbb.core.save.SlotAdvanceSerialGuard
import dev.novux.dynasty.cbb.database.calendar.DynastyCalendarEntity
import dev.novux.dynasty.cbb.data.calendar.WeekCheckpointRepository
import dev.novux.dynasty.cbb.data.balance.BalanceParseException
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.sim.SimTuningParseException
import dev.novux.dynasty.cbb.core.advance.SimWeekAdvanceInput
import dev.novux.dynasty.cbb.core.advance.UserLeagueFastSimBundle
import dev.novux.dynasty.cbb.core.dashboard.InSeasonWeekGames
import dev.novux.dynasty.cbb.core.advance.SimWeekAdvanceResolver
import dev.novux.dynasty.cbb.core.advance.SimWeekConfirmation
import dev.novux.dynasty.cbb.core.advance.UserWeekGameStatus
import dev.novux.dynasty.cbb.core.injuries.InjuryCalendarPolicy
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.bye.ByeWeekPolicy
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ArchivedGameInjuryEvent
import dev.novux.dynasty.cbb.core.calendar.checkpoint.WeekCheckpointState
import dev.novux.dynasty.cbb.core.injuries.InjuryApplyContext
import dev.novux.dynasty.cbb.core.preseason.SeasonStartFocus
import dev.novux.dynasty.cbb.core.calendar.CalendarWeekLabel
import dev.novux.dynasty.cbb.core.calendar.PhaseWeekPolicy
import dev.novux.dynasty.cbb.core.advance.InSeasonAdvanceConfirmPolicy
import dev.novux.dynasty.cbb.core.advance.OffseasonAdvanceConfirmPolicy
import dev.novux.dynasty.cbb.core.advance.RegularSeasonWeekBatchPolicy
import dev.novux.dynasty.cbb.core.portal.PortalCyclePolicy
import dev.novux.dynasty.cbb.data.news.NewsFeedRepository
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.core.recruiting.SeasonOneRecruitingBootstrap
import dev.novux.dynasty.cbb.core.recruiting.SigningDayPreseasonRecruitingTransition
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.recruiting.ai.AiRecruitingWeekRunner
import dev.novux.dynasty.cbb.data.portal.PortalCycleRepository
import dev.novux.dynasty.cbb.data.carousel.CoachingCarouselRepository
import dev.novux.dynasty.cbb.data.chemistry.ChemistryRepository
import dev.novux.dynasty.cbb.data.discipline.DisciplineRepository
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.dashboard.DashboardLastGameSnapshotBuilder
import dev.novux.dynasty.cbb.core.player.games.PlayerGameLogArchiveEngine
import dev.novux.dynasty.cbb.data.dashboard.DashboardLastGameRepository
import dev.novux.dynasty.cbb.core.player.games.PlayerGameLogEntry
import dev.novux.dynasty.cbb.data.player.PlayerPromiseStatusRepository
import dev.novux.dynasty.cbb.data.player.PlayerStatsRepository
import dev.novux.dynasty.cbb.data.sim.UserLeagueGameSimFactory
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.core.coach.CoachArchetype
import dev.novux.dynasty.cbb.data.coach.CoachAchievementsRepository
import dev.novux.dynasty.cbb.data.coach.CoachSkillPointRepository
import dev.novux.dynasty.cbb.data.recruiting.AiRecruitingRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingManualWeekSpendRepository
import dev.novux.dynasty.cbb.data.seasonreview.SeasonReviewProgressRepository
import dev.novux.dynasty.cbb.data.sim.SimTuningRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import dev.novux.dynasty.cbb.database.dynasty.toCoachEntity
import dev.novux.dynasty.cbb.database.roster.toEntities
import dev.novux.dynasty.cbb.database.dynasty.toEntity
import dev.novux.dynasty.cbb.database.dynasty.toDomain
import dev.novux.dynasty.cbb.database.save.SaveSlotEntity
import dev.novux.dynasty.cbb.database.save.ensureSlotRows
import dev.novux.dynasty.cbb.database.save.toSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

private val SCHEDULED_GAME_PHASES = setOf(
    CalendarPhase.Exhibition,
    CalendarPhase.RegularSeason,
    CalendarPhase.ConferenceTournament,
    CalendarPhase.Postseason,
)

class SaveSlotRepository(
    private val database: DynastyDatabase,
    private val weekCheckpointRepository: WeekCheckpointRepository,
    private val packRepository: PackRepository,
    private val balanceRulesetRepository: BalanceRulesetRepository,
    private val simTuningRepository: SimTuningRepository,
    private val recruitingEconomyRepository: RecruitingEconomyRepository,
    private val portalCycleRepository: PortalCycleRepository,
    private val aiRecruitingRepository: AiRecruitingRepository,
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val disciplineRepository: DisciplineRepository,
    private val chemistryRepository: ChemistryRepository,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
    private val preseasonScheduleRepository: PreseasonScheduleRepository,
    private val coachSkillPointRepository: CoachSkillPointRepository,
    private val coachingCarouselRepository: CoachingCarouselRepository,
    private val nilAnnualPoolRepository: dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository,
    private val dynastyAutomationRepository: dev.novux.dynasty.cbb.data.settings.DynastyAutomationRepository,
    private val appSettingsRepository: dev.novux.dynasty.cbb.data.settings.AppSettingsRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val incomingClassSeedRepository: IncomingClassSeedRepository,
    private val newsFeedRepository: NewsFeedRepository,
    private val userLeagueGameSimFactory: UserLeagueGameSimFactory,
    private val coachAchievementsRepository: CoachAchievementsRepository,
    private val seasonReviewProgressRepository: SeasonReviewProgressRepository,
    private val dashboardLastGameRepository: DashboardLastGameRepository,
    private val recruitingManualWeekSpendRepository: RecruitingManualWeekSpendRepository,
    private val packDynastyIdentityRepository: dev.novux.dynasty.cbb.data.pack.PackDynastyIdentityRepository,
) {
    private val slotDao = database.saveSlotDao()
    private val metadataDao = database.dynastyMetadataDao()
    private val coachDao = database.dynastyCoachDao()
    private val rosterDao = database.dynastyRosterDao()
    private val staffDao = database.dynastyStaffDao()
    private val calendarDao = database.dynastyCalendarDao()
    private val initMutex = Mutex()
    private val advanceSerialGuard = SlotAdvanceSerialGuard()
    private val newGameSignMutex = Mutex()

    suspend fun ensureInitialized() {
        initMutex.withLock {
            val existing = slotDao.listAll()
            if (existing.size < SaveSlots.COUNT) {
                val merged = ensureSlotRows(existing)
                slotDao.upsertAll(merged)
            }
        }
    }

    fun observeSlots(): Flow<List<SaveSlotSummary>> =
        slotDao.observeAll()
            .map { rows -> ensureSlotRows(rows).map { it.toSummary() } }
            .catch { emit(defaultEmptySlotSummaries()) }

    fun observeContinueSlot(): Flow<SaveSlotSummary?> =
        observeSlots().map { slots ->
            slots
                .filter { !it.isEmpty }
                .maxByOrNull { it.lastPlayedAtEpochMillis ?: 0L }
        }

    suspend fun listSlots(): List<SaveSlotSummary> = try {
        ensureInitialized()
        ensureSlotRows(slotDao.listAll()).map { it.toSummary() }
    } catch (_: Exception) {
        defaultEmptySlotSummaries()
    }

    suspend fun continueSlot(): SaveSlotSummary? = try {
        ensureInitialized()
        slotDao.getMostRecentlyPlayed()?.toSummary()
    } catch (_: Exception) {
        null
    }

    suspend fun firstEmptySlotIndex(): Int? {
        ensureInitialized()
        return ensureSlotRows(slotDao.listAll())
            .firstOrNull { it.dynastyId.isNullOrBlank() }
            ?.slotIndex
    }

    suspend fun loadSlot(slotIndex: Int): DynastyLoadResult = withContext(Dispatchers.IO) {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        try {
            ensureInitialized()
            val entity = slotDao.getByIndex(slotIndex)
                ?: return@withContext DynastyLoadResult.Unreadable(
                    slotIndex,
                    DynastyLoadFailure.DatabaseRead("slot row missing"),
                )
            val summary = entity.toSummary()
            if (summary.isEmpty) return@withContext DynastyLoadResult.SlotEmpty

            val slotSnap = DynastySaveValidator.SlotSnapshot(
                slotIndex = slotIndex,
                dynastyId = summary.dynastyId,
                packId = summary.packId,
                isEmpty = false,
            )
            val metadataEntity = summary.dynastyId?.let { metadataDao.getById(it) }
            val metadata = metadataEntity?.toDomain()
            val metaSnap = metadata?.let {
                DynastySaveValidator.MetadataSnapshot(
                    dynastyId = it.dynastyId,
                    packId = it.packId,
                    seasonIndex = it.seasonIndex,
                    phase = it.phase,
                )
            }
            DynastySaveValidator.validateBeforeLoad(slotSnap, metaSnap)?.let { failure ->
                return@withContext DynastyLoadResult.Unreadable(slotIndex, failure)
            }
            val dynastyIdForCore = summary.dynastyId!!
            val hasCoach = coachDao.getByDynastyId(dynastyIdForCore) != null
            val rosterCount = rosterDao.listForDynasty(dynastyIdForCore).size
            DynastySaveValidator.validateSignedDynastyCore(hasCoach, rosterCount)?.let { failure ->
                return@withContext DynastyLoadResult.Unreadable(slotIndex, failure)
            }
            val meta = metadata
                ?: return@withContext DynastyLoadResult.Unreadable(
                    slotIndex,
                    DynastyLoadFailure.MissingMetadata(summary.dynastyId!!),
                )

            packRepository.restorePack(meta.packId)

            val balanceRuleset = try {
                balanceRulesetRepository.restorePinned(
                    meta.balanceRulesetId,
                    meta.balanceSchemaVersion,
                )
            } catch (e: BalanceParseException) {
                return@withContext DynastyLoadResult.Unreadable(
                    slotIndex,
                    DynastyLoadFailure.InvalidBalanceRuleset(e.message ?: "balance load failed"),
                )
            }

            val simTuning = try {
                simTuningRepository.restorePinned(meta.simTuningId, meta.simSchemaVersion)
            } catch (e: SimTuningParseException) {
                return@withContext DynastyLoadResult.Unreadable(
                    slotIndex,
                    DynastyLoadFailure.InvalidSimTuning(e.message ?: "sim tuning load failed"),
                )
            }

            val calendarRow = calendarDao.getByDynastyId(meta.dynastyId)
            calendarRow?.toCalendarSnapshot()?.let { snap ->
                DynastySaveValidator.validateCalendar(snap)?.let { failure ->
                    return@withContext DynastyLoadResult.Unreadable(slotIndex, failure)
                }
            }

            val weekCheckpoint = if (calendarRow == null) {
                database.withTransaction {
                    weekCheckpointRepository.repairCalendarFromMetadata(meta)
                }
            } else {
                weekCheckpointRepository.loadCheckpoint(meta.dynastyId)
                    ?: return@withContext DynastyLoadResult.Unreadable(
                        slotIndex,
                        DynastyLoadFailure.InvalidCalendar("calendar row unreadable"),
                    )
            }

            val now = System.currentTimeMillis()
            val updatedSummary = summary.copy(lastPlayedAtEpochMillis = now)
            slotDao.update(entity.copy(lastPlayedAtEpochMillis = now))
            DynastyLoadResult.Success(
                LoadedDynasty(
                    slotIndex = slotIndex,
                    dynastyId = meta.dynastyId,
                    summary = updatedSummary,
                    metadata = meta,
                    weekCheckpoint = weekCheckpoint,
                    balanceRuleset = balanceRuleset,
                    simTuning = simTuning,
                ),
            )
        } catch (e: Exception) {
            DynastyLoadResult.Unreadable(
                slotIndex,
                DynastyLoadFailure.fromReadException(e),
            )
        }
    }

    /** True when occupied slot header rows are present (fast list hint; load still validates). */
    suspend fun isSlotLikelyLoadable(slotIndex: Int): Boolean {
        return try {
            ensureInitialized()
            val entity = slotDao.getByIndex(slotIndex) ?: return false
            val summary = entity.toSummary()
            when {
                summary.isEmpty -> true
                summary.dynastyId.isNullOrBlank() -> true
                else -> metadataDao.getById(summary.dynastyId!!) != null
            }
        } catch (_: Exception) {
            false
        }
    }

    /** Portable export manifest for share/backup ([MEGA-COACH-183], ADR-001). */
    suspend fun exportDynastyArchive(slotIndex: Int): DynastyExportArchive? = withContext(Dispatchers.IO) {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        try {
            ensureInitialized()
            val entity = slotDao.getByIndex(slotIndex) ?: return@withContext null
            val summary = entity.toSummary()
            if (summary.isEmpty) return@withContext null
            val dynastyId = summary.dynastyId ?: return@withContext null
            val meta = metadataDao.getById(dynastyId)?.toDomain() ?: return@withContext null
            val checkpoint = weekCheckpointRepository.loadCheckpoint(dynastyId) ?: return@withContext null
            val rosterCount = rosterDao.listForDynasty(dynastyId).size
            DynastyExportEngine.build(slotIndex, summary, meta, checkpoint, rosterCount)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun deleteSlot(slotIndex: Int) {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        try {
            ensureInitialized()
            val dynastyId = slotDao.getByIndex(slotIndex)?.dynastyId
            database.withTransaction {
                if (!dynastyId.isNullOrBlank()) {
                    metadataDao.deleteById(dynastyId)
                    coachDao.deleteByDynastyId(dynastyId)
                    rosterDao.deleteForDynasty(dynastyId)
                    staffDao.deleteByDynastyId(dynastyId)
                    weekCheckpointRepository.deleteForDynasty(dynastyId)
                    database.coachCareerSeasonDao().deleteForDynasty(dynastyId)
                    database.playerSeasonStatsDao().deleteForDynasty(dynastyId)
                }
                slotDao.clearSlot(slotIndex)
            }
        } catch (_: Exception) {
            runCatching { slotDao.clearSlot(slotIndex) }
        }
    }

    private fun defaultEmptySlotSummaries(): List<SaveSlotSummary> =
        SaveSlots.INDICES.map { index ->
            SaveSlotSummary(slotIndex = index, isEmpty = true)
        }

    /**
     * Creates a new dynasty with default metadata (offseason, season 1, default pack).
     * Full new-game wizard will replace placeholder display names.
     */
    suspend fun createEmptyDynasty(
        slotIndex: Int,
        schoolDisplayName: String,
        coachDisplayName: String,
        packId: String = DynastySchema.DEFAULT_PACK_ID,
        dynastyId: String = UUID.randomUUID().toString(),
    ): LoadedDynasty? {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        ensureInitialized()
        val existing = slotDao.getByIndex(slotIndex)?.toSummary()
        if (existing != null && !existing.isEmpty) return null

        val shippedBalance = balanceRulesetRepository.ensureShippedDefaultLoaded()
        val shippedSim = simTuningRepository.ensureShippedDefaultLoaded()
        val metadata = DynastyBootstrap.newDynasty(
            dynastyId = dynastyId,
            packId = packId,
            balanceRulesetId = shippedBalance.balanceRulesetId,
            balanceSchemaVersion = shippedBalance.schemaVersion,
            simTuningId = shippedSim.simTuningId,
            simSchemaVersion = shippedSim.schemaVersion,
        )
        val seasonLabel = SeasonMembershipYear.label(metadata.seasonIndex)
        val now = System.currentTimeMillis()

        database.withTransaction {
            metadataDao.upsert(metadata.toEntity())
            weekCheckpointRepository.insertBootstrapCalendar(metadata)
            slotDao.upsert(
                SaveSlotEntity(
                    slotIndex = slotIndex,
                    dynastyId = dynastyId,
                    lastPlayedAtEpochMillis = now,
                    schoolDisplayName = schoolDisplayName,
                    coachDisplayName = coachDisplayName,
                    seasonLabel = seasonLabel,
                    wins = 0,
                    losses = 0,
                    packId = packId,
                ),
            )
        }

        val weekCheckpoint = weekCheckpointRepository.loadOrBootstrap(metadata)
        val summary = SaveSlotSummary(
            slotIndex = slotIndex,
            isEmpty = false,
            dynastyId = dynastyId,
            schoolDisplayName = schoolDisplayName,
            coachDisplayName = coachDisplayName,
            seasonLabel = seasonLabel,
            wins = 0,
            losses = 0,
            lastPlayedAtEpochMillis = now,
            packId = packId,
        )
        return LoadedDynasty(
            slotIndex = slotIndex,
            dynastyId = dynastyId,
            summary = summary,
            metadata = metadata,
            weekCheckpoint = weekCheckpoint,
            balanceRuleset = shippedBalance,
            simTuning = shippedSim,
        )
    }

    private fun DynastyCalendarEntity.toCalendarSnapshot(): DynastySaveValidator.CalendarSnapshot =
        DynastySaveValidator.CalendarSnapshot(
            seasonIndex = seasonIndex,
            phase = phase,
            activeWeekIndex = activeWeekIndex,
            committedSeasonIndex = committedSeasonIndex,
            committedPhase = committedPhase,
            committedWeekIndex = committedWeekIndex,
        )

    /**
     * Signs the wizard contract: creates metadata, coach row, calendar at Offseason week 1.
     */
    suspend fun signNewGameFromWizard(
        draft: NewGameDraft,
        schoolDisplayName: String,
        packId: String = DynastySchema.DEFAULT_PACK_ID,
    ): LoadedDynasty? = withContext(Dispatchers.IO) {
        newGameSignMutex.withLock {
        if (NewGameSignValidator.validate(draft) != null) return@withLock null
        val slotIndex = firstEmptySlotIndex() ?: return@withLock null
        val dynastyId = UUID.randomUUID().toString()
        val coachName = draft.coach.displayName()
        val shippedBalance = balanceRulesetRepository.ensureShippedDefaultLoaded()
        val shippedSim = simTuningRepository.ensureShippedDefaultLoaded()
        val metadata = DynastyBootstrap.newDynasty(
            dynastyId = dynastyId,
            packId = packId,
            balanceRulesetId = shippedBalance.balanceRulesetId,
            balanceSchemaVersion = shippedBalance.schemaVersion,
            simTuningId = shippedSim.simTuningId,
            simSchemaVersion = shippedSim.schemaVersion,
        )
        val seasonLabel = SeasonMembershipYear.label(metadata.seasonIndex)
        val now = System.currentTimeMillis()
        val rosterEntities =
            SeasonOneRosterGenerator.generate(dynastyId).toEntities()

        // MEGA-QA-029: metadata/coach/roster/calendar commit before slot row becomes visible.
        database.withTransaction {
            metadataDao.upsert(metadata.toEntity())
            coachDao.upsert(draft.toCoachEntity(dynastyId))
            rosterDao.upsertAll(rosterEntities)
            dynastyRosterRepository.normalizeDefaultLineup(dynastyId)
            weekCheckpointRepository.insertBootstrapCalendar(metadata)
            slotDao.upsert(
                SaveSlotEntity(
                    slotIndex = slotIndex,
                    dynastyId = dynastyId,
                    lastPlayedAtEpochMillis = now,
                    schoolDisplayName = schoolDisplayName,
                    coachDisplayName = coachName,
                    seasonLabel = seasonLabel,
                    wins = 0,
                    losses = 0,
                    packId = packId,
                ),
            )
        }
        coachAchievementsRepository.ensureEmpty(dynastyId)

        val weekCheckpoint = weekCheckpointRepository.loadOrBootstrap(metadata)
        val summary = SaveSlotSummary(
            slotIndex = slotIndex,
            isEmpty = false,
            dynastyId = dynastyId,
            schoolDisplayName = schoolDisplayName,
            coachDisplayName = coachName,
            seasonLabel = seasonLabel,
            wins = 0,
            losses = 0,
            lastPlayedAtEpochMillis = now,
            packId = packId,
        )
        val loaded = LoadedDynasty(
            slotIndex = slotIndex,
            dynastyId = dynastyId,
            summary = summary,
            metadata = metadata,
            weekCheckpoint = weekCheckpoint,
            balanceRuleset = shippedBalance,
            simTuning = shippedSim,
        )
        val appDefaults = appSettingsRepository.currentSettings()
        dynastyAutomationRepository.seedFromAppDefaults(dynastyId, appDefaults)
        if (appDefaults.defaultCoachAutoSpend) {
            coachSkillPointRepository.setAutoSpend(dynastyId, true, CoachArchetype.Balanced)
        }
        bootstrapSeasonOneRecruitingAfterSign(loaded, draft)
        packDynastyIdentityRepository.recordMembershipYearAtSign(
            dynastyId,
            packRepository.activeSession().index.membershipYear,
        )
        loaded
        }
    }

    /**
     * Season 1 wizard sign bootstrap ([DynastyWizardBootstrapPolicy]) — class, news, recruiting ledger,
     * and NIL annual pool for the chosen school (MEGA-RECR-053, MEGA-RECR-100).
     */
    private suspend fun bootstrapSeasonOneRecruitingAfterSign(loaded: LoadedDynasty, draft: NewGameDraft) {
        if (DynastyWizardBootstrapPolicy.stepsAfterSign(loaded.metadata.seasonIndex).isEmpty()) return
        if (!SeasonOneRecruitingBootstrap.appliesToMembershipYear(loaded.metadata.seasonIndex)) return
        val seasonIndex = SeasonOneRecruitingBootstrap.poolStorageSeasonIndex(loaded.metadata.seasonIndex)
        incomingClassSeedRepository.ensurePool(loaded.dynastyId, seasonIndex)
        newsFeedRepository.publishRecruitingSpotlightWhenPoolReady(loaded.dynastyId, seasonIndex)
        val coach = coachDao.getByDynastyId(loaded.dynastyId) ?: return
        val teamId = draft.selectedTeamId ?: coach.userTeamId
        val session = packRepository.restorePack(loaded.summary.packId ?: DynastySchema.DEFAULT_PACK_ID)
        val team = session.index.team(teamId) ?: return
        val ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded, programPrestigeOverride = team.programPrestige)
        val gated = if (
            SigningDayPreseasonRecruitingTransition.seasonOneOffseasonWeekOne(
                loaded.weekCheckpoint.activePosition,
                loaded.metadata.seasonIndex,
            )
        ) {
            SigningDayPreseasonRecruitingTransition.seasonOneOffseasonWeekOneLedger(ledger)
        } else {
            ledger
        }
        recruitingEconomyRepository.persistLedger(loaded.dynastyId, gated)
        nilAnnualPoolRepository.openCyclePoolUsd(
            dynastyId = loaded.dynastyId,
            balanceNil = loaded.balanceRuleset.nil,
            cycleSeasonIndex = loaded.metadata.seasonIndex,
            conferenceId = team.conferenceId,
            programPrestige = team.programPrestige,
        )
    }

    suspend fun createEmptyDynastyInFirstOpenSlot(
        schoolDisplayName: String,
        coachDisplayName: String,
        packId: String = DynastySchema.DEFAULT_PACK_ID,
    ): LoadedDynasty? {
        val slotIndex = firstEmptySlotIndex() ?: return null
        return createEmptyDynasty(
            slotIndex = slotIndex,
            schoolDisplayName = schoolDisplayName,
            coachDisplayName = coachDisplayName,
            packId = packId,
        )
    }

    /**
     * End of Season CTA — advances into Offseason Week 1 of the next membership year ([planning/season-review.md]).
     */
    suspend fun buildSimWeekAdvanceConfirmation(
        slotIndex: Int,
        unresolvedOptionalTaskCount: Int = 0,
    ): SimWeekConfirmation? = withContext(Dispatchers.IO) {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        ensureInitialized()
        val loaded = when (val initial = loadSlot(slotIndex)) {
            is DynastyLoadResult.Success -> initial.loaded
            else -> return@withContext null
        }
        val ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
        val manualSpent = recruitingManualWeekSpendRepository.totalForDynasty(loaded)
        val input = buildSimWeekAdvanceInputSuspended(
            loaded,
            ledger,
            unresolvedOptionalTaskCount = unresolvedOptionalTaskCount,
            userManualRecruitingSpent = manualSpent,
            dynastySeed = loaded.dynastyId.hashCode().toLong(),
            preDialogOnly = true,
        )
        SimWeekAdvanceResolver.buildConfirmation(input)
    }

    /**
     * Preseason **Start season** — jumps to exhibition or first RS week per schedule
     * ([planning/preseason.md#advance-to-regular-season]).
     */
    suspend fun enterInSeasonFromPreseason(
        slotIndex: Int,
        focus: SeasonStartFocus,
    ): DynastyLoadResult {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        ensureInitialized()
        val loaded = when (val initial = loadSlot(slotIndex)) {
            is DynastyLoadResult.Success -> initial.loaded
            else -> return initial
        }
        if (loaded.weekCheckpoint.activePosition.phase != CalendarPhase.Preseason) {
            return DynastyLoadResult.Unreadable(
                slotIndex,
                DynastyLoadFailure.InvalidCalendar("expected preseason"),
            )
        }
        val preseasonWeek = loaded.weekCheckpoint.activePosition
        val nextState = WeekCheckpointState(
            activePosition = focus.calendarPosition,
            committedThrough = preseasonWeek,
            provisionalGames = emptyMap(),
        )
        weekCheckpointRepository.persist(loaded.dynastyId, nextState)
        val metadata = metadataDao.getById(loaded.dynastyId)?.toDomain()
        if (metadata != null) {
            metadataDao.upsert(
                metadata.copy(
                    seasonIndex = focus.calendarPosition.seasonIndex,
                    phase = focus.calendarPosition.phase,
                ).toEntity(),
            )
        }
        return loadSlot(slotIndex)
    }

    /** Advances the active dynasty one calendar week and persists checkpoint + metadata. */
    suspend fun advanceDynastyWeek(
        slotIndex: Int,
        userManualRecruitingSpent: Int = 0,
    ): DynastyLoadResult = withContext(Dispatchers.IO) {
        advanceSerialGuard.withSlotAdvanceLock(slotIndex) {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        ensureInitialized()
        val loaded = when (val initial = loadSlot(slotIndex)) {
            is DynastyLoadResult.Success -> initial.loaded
            else -> return@withSlotAdvanceLock initial
        }
        if (disciplineRepository.hasPending(loaded.dynastyId, loaded.metadata.seasonIndex)) {
            return@withSlotAdvanceLock DynastyLoadResult.WeekAdvanceBlocked(
                slotIndex = slotIndex,
                reason = DynastyWeekAdvanceBlockReason.DisciplinePending,
                loaded = loaded,
            )
        }
        val ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
        val dynastySeed = loaded.dynastyId.hashCode().toLong()
        val manualSpent = userManualRecruitingSpent.takeIf { it > 0 }
            ?: recruitingManualWeekSpendRepository.totalForDynasty(loaded)
        val input = buildSimWeekAdvanceInputSuspended(
            loaded,
            ledger,
            0,
            manualSpent,
            dynastySeed,
        )
        val preAdvance = SimWeekAdvanceResolver.resolvePreAdvance(
            loaded.weekCheckpoint,
            input,
            dynastySeed,
        )
        if (preAdvance.fastSimmedGames > 0) {
            applyFastSimInjuries(
                dynastyId = loaded.dynastyId,
                marks = preAdvance.checkpointAfterPreAdvance.provisionalGames.values,
                weekIndex = loaded.weekCheckpoint.activePosition.weekIndex,
            )
        }
        // Skip intermediate persist so kill mid-advance leaves prior week or final week only (MEGA-QA-028).
        val checkpoint = preAdvance.checkpointAfterPreAdvance
        val aiWeek = aiRecruitingRepository.runLeagueWeekIfActive(loaded, ledger, manualSpent)
        val recruitingSpentClosingWeek = aiWeek?.userPointsSpent ?: 0
        val rosterSizeBeforeWeek = dynastyRosterRepository.listPlayers(loaded.dynastyId).size
        val advanceResult = weekCheckpointRepository.advanceWeek(loaded.dynastyId, checkpoint)
        leagueWeekBatchRepository.afterWeekAdvance(
            dynastyId = loaded.dynastyId,
            seasonIndex = loaded.metadata.seasonIndex,
            committedWeek = advanceResult.committedWeek,
            userGamesCommitted = advanceResult.gamesCommitted,
        )
        dynastyRosterRepository.tickInjuriesForWeekAdvance(
            loaded.dynastyId,
            advanceResult.committedWeek.phase,
        )
        val teamPlayedGame = advanceResult.committedWeek.phase in SCHEDULED_GAME_PHASES
        val playersAfterDiscipline = dynastyRosterRepository.listPlayers(loaded.dynastyId)
        disciplineRepository.afterWeekAdvance(
            dynastyId = loaded.dynastyId,
            seasonIndex = loaded.metadata.seasonIndex,
            phase = advanceResult.committedWeek.phase,
            rsWeekIndex = advanceResult.committedWeek.weekIndex,
            players = playersAfterDiscipline,
            teamPlayedGame = teamPlayedGame,
            rollSeed = loaded.dynastyId.hashCode().toLong() + advanceResult.committedWeek.weekIndex,
        )
        val balance = balanceRulesetRepository.activeRuleset()
        val userGamesThisWeek = advanceResult.gamesCommitted.size.coerceAtLeast(1)
        val (offScheme, defScheme) = userLeagueGameSimFactory.resolveSchemesForFatigueTick(loaded.dynastyId)
        dynastyRosterRepository.tickFatigueForWeekAdvance(
            loaded.dynastyId,
            advanceResult.committedWeek.phase,
            teamPlayedGame,
            balance.fatigue,
            userGamesThisWeek = if (teamPlayedGame) userGamesThisWeek else 1,
            offensiveScheme = offScheme,
            defensiveScheme = defScheme,
        )
        if (teamPlayedGame && advanceResult.gamesCommitted.isNotEmpty()) {
            ingestPlayerGameLogsFromCommittedMarks(
                loaded = loaded,
                committedWeek = advanceResult.committedWeek,
                marks = advanceResult.gamesCommitted,
            )
            dynastyRosterRepository.recordGamesPlayedAfterCommittedWeek(
                loaded.dynastyId,
                teamPlayedGame = true,
            )
            playerStatsRepository.persistSeasonStatsForRoster(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                players = dynastyRosterRepository.listDynastyPlayers(loaded.dynastyId),
                schoolDisplayName = loaded.summary.schoolDisplayName.orEmpty(),
            )
        }
        val userWonGame = if (teamPlayedGame) {
            preAdvance.checkpointAfterPreAdvance.provisionalGames.values.lastOrNull()?.let { mark ->
                mark.homeScore > mark.awayScore
            }
        } else {
            null
        }
        val playersForChemistry = dynastyRosterRepository.listPlayers(loaded.dynastyId)
        val chemistryTickInput = dev.novux.dynasty.cbb.core.chemistry.ChemistryWeeklyTickInputPolicy.build(
            players = playersForChemistry,
            headCoachArchetype = coachArchetype(loaded.dynastyId),
            rosterSizeBeforeWeek = rosterSizeBeforeWeek,
            teamPlayedGame = teamPlayedGame,
            userWonGame = userWonGame,
        )
        chemistryRepository.afterWeekAdvance(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
            advanceResult.committedWeek.phase,
            playersForChemistry,
            chemistryTickInput,
        )
        PlayerPromiseStatusRepository(dynastyRosterRepository, playerStatsRepository)
            .refreshInSeasonAfterWeek(loaded.dynastyId, loaded.metadata.seasonIndex)
        coachSkillPointRepository.ensureSundayGrant(
            loaded.dynastyId,
            advanceResult.committedWeek,
            coachArchetype(loaded.dynastyId),
        )
        val meta = metadataDao.getById(loaded.dynastyId)?.toDomain()
        if (meta != null) {
            val slotEntity = slotDao.getByIndex(slotIndex)
            if (slotEntity != null) {
                slotDao.upsert(
                    slotEntity.copy(seasonLabel = SeasonMembershipYear.label(meta.seasonIndex)),
                )
            }
        }
        val afterAdvance = when (val again = loadSlot(slotIndex)) {
            is DynastyLoadResult.Success -> again.loaded
            else -> return@withSlotAdvanceLock again
        }
        recruitingEconomyRepository.onCalendarWeekAdvanced(afterAdvance, recruitingSpentClosingWeek)
        val pos = afterAdvance.weekCheckpoint.activePosition
        if (SigningDayPreseasonRecruitingTransition.enteringPreseason(pos)) {
            val nextPoolSeason = SigningDayPreseasonRecruitingTransition.nextEnrollingClassSeasonIndex(
                afterAdvance.metadata.seasonIndex,
            )
            incomingClassSeedRepository.ensurePool(afterAdvance.dynastyId, nextPoolSeason)
        }
        if (pos.phase == CalendarPhase.Offseason) {
            portalCycleRepository.onOffseasonWeekStarted(
                afterAdvance.dynastyId,
                afterAdvance.metadata.seasonIndex,
                pos.weekIndex,
            )
        }
        loadSlot(slotIndex)
        }
    }

    private suspend fun ingestPlayerGameLogsFromCommittedMarks(
        loaded: LoadedDynasty,
        committedWeek: CalendarPosition,
        marks: Map<String, ProvisionalGameMark>,
    ) {
        val marksWithBox = marks.values.filter { it.possessionEngine && it.boxArchive != null }
        if (marksWithBox.isEmpty()) return
        val coach = coachDao.getByDynastyId(loaded.dynastyId) ?: return
        val userTeamId = coach.userTeamId
        val ctx = resolveGameLogWeekContext(
            dynastyId = loaded.dynastyId,
            seasonIndex = loaded.metadata.seasonIndex,
            userTeamId = userTeamId,
            weekIndex = committedWeek.weekIndex,
        ) ?: GameLogWeekContext(userTeamIsHome = true, opponentLabel = "Opponent")
        val rosterIds = dynastyRosterRepository.listPlayers(loaded.dynastyId).map { it.playerId }.toSet()
        val byPlayer = mutableMapOf<String, MutableList<PlayerGameLogEntry>>()
        marksWithBox.forEach { mark ->
            val entries = PlayerGameLogArchiveEngine.entriesForRosterPlayers(
                mark = mark,
                userTeamIsHome = ctx.userTeamIsHome,
                opponentLabel = ctx.opponentLabel,
                rsWeekIndex = committedWeek.weekIndex,
                rosterPlayerIds = rosterIds,
            )
            entries.forEach { entry ->
                val playerId = entry.gameKey.removePrefix("${mark.gameId}_")
                byPlayer.getOrPut(playerId) { mutableListOf() }.add(entry)
            }
        }
        if (byPlayer.isNotEmpty()) {
            playerStatsRepository.ingestCommittedSimMarks(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                entriesByPlayerId = byPlayer,
            )
        }
        marksWithBox.firstOrNull()?.let { mark ->
            val school = loaded.summary.schoolDisplayName.orEmpty().ifBlank { "Your program" }
            val snapshot = DashboardLastGameSnapshotBuilder.fromCommittedMark(
                mark = mark,
                committedWeek = committedWeek,
                userTeamIsHome = ctx.userTeamIsHome,
                opponentDisplayName = ctx.opponentLabel,
                userTeamDisplayName = school,
            )
            dashboardLastGameRepository.save(loaded.dynastyId, loaded.metadata.seasonIndex, snapshot)
        }
    }

    private data class GameLogWeekContext(
        val userTeamIsHome: Boolean,
        val opponentLabel: String,
    )

    private suspend fun resolveGameLogWeekContext(
        dynastyId: String,
        seasonIndex: Int,
        userTeamId: String,
        weekIndex: Int,
    ): GameLogWeekContext? {
        val plan = preseasonScheduleRepository.loadPlan(dynastyId, seasonIndex) ?: return null
        val game = plan.regularSeason.firstOrNull { it.rsWeekIndex == weekIndex } ?: return null
        val opponentId = game.opponentTeamId
        val opponentLabel = packRepository.activeSession().index.team(opponentId)?.displayName ?: opponentId
        return GameLogWeekContext(userTeamIsHome = game.homeGame, opponentLabel = opponentLabel)
    }

    private suspend fun applyFastSimInjuries(
        dynastyId: String,
        marks: Collection<ProvisionalGameMark>,
        weekIndex: Int,
    ) {
        marks.forEach { mark ->
            val archived = mark.boxArchive?.userTeamInjuries.orEmpty()
            if (archived.isEmpty()) return@forEach
            val events = archived.map { ArchivedGameInjuryEvent.toDomain(it) }
            val before = dynastyRosterRepository.listPlayers(dynastyId)
            val rsGames = before.firstOrNull { it.playerId == events.first().playerId }
                ?.rsGamesPlayedSeason ?: 0
            dynastyRosterRepository.applyGameInjuries(
                dynastyId,
                events,
                InjuryApplyContext(
                    rsWeekIndex = weekIndex,
                    rsGameIndex = weekIndex.coerceAtLeast(1),
                    rsGamesPlayed = rsGames,
                ),
            )
        }
    }

    private suspend fun buildSimWeekAdvanceInputSuspended(
        loaded: LoadedDynasty,
        ledger: dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger,
        unresolvedOptionalTaskCount: Int,
        userManualRecruitingSpent: Int,
        dynastySeed: Long,
        /** When true, skip recruit seeding and fast-sim bundle assembly ([MEGA-QA-008]). */
        preDialogOnly: Boolean = false,
    ): SimWeekAdvanceInput {
        val position = loaded.weekCheckpoint.activePosition
        val nextLabel = CalendarWeekLabel.display(PhaseWeekPolicy.nextPosition(position))
        val offseasonWeek = position.weekIndex.takeIf { position.phase == CalendarPhase.Offseason }
        val hubOpen = RecruitingCyclePolicy.hubNavigable(position.phase, offseasonWeek, ledger.cycleClosed)
        val portalOpen = PortalCyclePolicy.portalRecruitingWindowOpen(position.phase, offseasonWeek)
        val plan = preseasonScheduleRepository.loadPlan(loaded.dynastyId, loaded.metadata.seasonIndex)
        val exhibitionCount = preseasonScheduleRepository.loadExhibitionCount(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        val weekGames = InSeasonWeekGames.forPosition(position, plan, exhibitionCount)
        val hasScheduledGame = weekGames.isNotEmpty()
        val unplayed = UserWeekGameStatus.unplayedUserGames(loaded.weekCheckpoint, hasScheduledGame)
        val automation = dynastyAutomationRepository.load(loaded.dynastyId)
        val estimated = if (hubOpen && automation.recruitingAutoMaintain && userManualRecruitingSpent == 0) {
            val pool = if (preDialogOnly) {
                incomingClassSeedRepository.loadPool(loaded.dynastyId, loaded.metadata.seasonIndex)
            } else {
                incomingClassSeedRepository.ensurePool(loaded.dynastyId, loaded.metadata.seasonIndex)
            }
            if (pool != null) {
                val economy = BalanceRecruitingParser.parse(loaded.balanceRuleset.recruiting)
                val recommended = BalanceRecruitingRecommendedParser.parse(loaded.balanceRuleset.recruiting)
                val userSchool = nilAnnualPoolRepository.userSchoolRecruitingContext(loaded)
                    ?: SchoolRecruitingContext(
                        programPrestige = recruitingEconomyRepository.resolveUserProgramPrestige(loaded),
                        schoolHomeState = "OH",
                        pipelineStates = setOf("KY", "IN", "MI", "PA", "WV"),
                        nilPoolAnnualUsd =
                            dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository.DEFAULT_FALLBACK_USD.toInt(),
                    )
                val actionCosts = BalanceRecruitingActionsParser.parse(loaded.balanceRuleset.recruiting)
                AiRecruitingWeekRunner.estimateUserAutoMaintainSpend(
                    prospectPool = pool,
                    economy = economy,
                    recommended = recommended,
                    userSchool = userSchool,
                    userProgramId = "user-${loaded.dynastyId}",
                    userPointsAvailable = ledger.pointsAvailable,
                    actionCosts = actionCosts,
                )
            } else {
                0
            }
        } else {
            0
        }
        val injuredTicking = if (InjuryCalendarPolicy.ticksOnWeekAdvance(position.phase)) {
            dynastyRosterRepository.listPlayers(loaded.dynastyId)
                .count { it.injuryActive && !it.medicalRedshirtSeason }
        } else {
            0
        }
        val userLeagueFastSim: UserLeagueFastSimBundle? = if (!preDialogOnly && unplayed > 0) {
            userLeagueGameSimFactory.buildFastSimBundle(loaded, unplayed, dynastySeed)
        } else {
            null
        }
        return SimWeekAdvanceInput(
            nextWeekLabel = nextLabel,
            unplayedUserGameCount = unplayed,
            unplayedUserGameOpponentDisplayName = weekGames.firstOrNull()?.opponentDisplayName,
            recruitingPointsAvailable = ledger.pointsAvailable,
            userManualRecruitingSpent = userManualRecruitingSpent,
            recruitingHubOpen = hubOpen,
            estimatedAutoRecruitingSpend = estimated,
            portalRecruitingWeekOpen = portalOpen,
            unresolvedOptionalTaskCount = unresolvedOptionalTaskCount,
            injuredPlayersTicking = injuredTicking,
            userOnByeWeek = ByeWeekPolicy.isByeWeek(position.phase, hasScheduledGame),
            recruitingAutoMaintainEnabled = automation.recruitingAutoMaintain,
            userLeagueFastSim = userLeagueFastSim,
            simulatesLeagueWeekBatch = RegularSeasonWeekBatchPolicy.isRegularSeasonWeek(position),
            offseasonMilestoneAdvance = OffseasonAdvanceConfirmPolicy.requiresLightweightDialog(
                position.phase,
                offseasonWeek,
            ),
            portalRecruitingWindowClosed = position.phase == CalendarPhase.Offseason &&
                (offseasonWeek?.let { PortalCyclePolicy.portalWindowClosed(it) } == true) &&
                !portalOpen,
            inSeasonDestinationAcknowledgment = InSeasonAdvanceConfirmPolicy
                .requiresDestinationAcknowledgment(position.phase),
        )
    }

    suspend fun coachArchetypeForDynasty(dynastyId: String): CoachArchetype = coachArchetype(dynastyId)

    private suspend fun coachArchetype(dynastyId: String): CoachArchetype {
        val wire = coachDao.getByDynastyId(dynastyId)?.archetypeWire
        return CoachArchetype.fromWire(wire.orEmpty()) ?: CoachArchetype.Balanced
    }

    suspend fun completeSeasonReviewEnterOffseason(slotIndex: Int): DynastyLoadResult {
        require(slotIndex in SaveSlots.INDICES) { "slotIndex out of range" }
        ensureInitialized()
        val loaded = when (val initial = loadSlot(slotIndex)) {
            is DynastyLoadResult.Success -> initial.loaded
            else -> return initial
        }
        val reviewProgress = seasonReviewProgressRepository.load(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        if (!SeasonReviewGate.canEnterOffseason(loaded.weekCheckpoint, reviewProgress.acknowledged)) {
            return DynastyLoadResult.Unreadable(
                slotIndex,
                DynastyLoadFailure.InvalidCalendar(
                    if (SeasonReviewGate.isActive(loaded.weekCheckpoint) && !reviewProgress.acknowledged) {
                        "acknowledge season recap before entering offseason"
                    } else {
                        "dynasty is not in End of Season week"
                    },
                ),
            )
        }
        val advancePlan = SeasonYearAdvanceEngine.planEnterOffseason(loaded.weekCheckpoint)
        weekCheckpointRepository.advanceWeek(loaded.dynastyId, loaded.weekCheckpoint)
        val meta = metadataDao.getById(loaded.dynastyId)?.toDomain()
            ?: return DynastyLoadResult.Unreadable(
                slotIndex,
                DynastyLoadFailure.MissingMetadata(loaded.dynastyId),
            )
        check(meta.seasonIndex == advancePlan.newSeasonIndex) {
            "calendar season index out of sync after year advance"
        }
        val slotEntity = slotDao.getByIndex(slotIndex)
            ?: return DynastyLoadResult.Unreadable(
                slotIndex,
                DynastyLoadFailure.DatabaseRead("slot row missing"),
            )
        slotDao.upsert(
            slotEntity.copy(seasonLabel = SeasonMembershipYear.label(meta.seasonIndex)),
        )
        val coach = coachDao.getByDynastyId(loaded.dynastyId)
        val team = coach?.let { packRepository.activeSession().index.team(it.userTeamId) }
        recruitingEconomyRepository.openNextCycleAfterSeasonReview(
            dynastyId = loaded.dynastyId,
            balance = loaded.balanceRuleset,
            nextSeasonIndex = meta.seasonIndex,
            postRecapPrestige = team?.programPrestige
                ?: recruitingEconomyRepository.resolveUserProgramPrestige(loaded),
        )
        if (team != null) {
            nilAnnualPoolRepository.openCyclePoolUsd(
                dynastyId = loaded.dynastyId,
                balanceNil = loaded.balanceRuleset.nil,
                cycleSeasonIndex = meta.seasonIndex,
                conferenceId = team.conferenceId,
                programPrestige = team.programPrestige,
            )
        }
        coachingCarouselRepository.loadOrCreate(loaded.dynastyId, meta.seasonIndex)
        portalCycleRepository.loadOrSimulate(loaded.dynastyId, meta.seasonIndex)
        dynastyRosterRepository.clearNilDealRaisedFlags(loaded.dynastyId)
        return loadSlot(slotIndex)
    }
}

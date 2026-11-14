package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingParser
import dev.novux.dynasty.cbb.core.balance.BalanceRuleset
import dev.novux.dynasty.cbb.core.recruiting.OfficialVisitDistanceBand
import dev.novux.dynasty.cbb.core.recruiting.RecruitingActionSpend
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPointAction
import dev.novux.dynasty.cbb.core.recruiting.RecruitingRefundableAction
import dev.novux.dynasty.cbb.core.recruiting.RecruitingRefundableActionSpend
import dev.novux.dynasty.cbb.core.recruiting.RecruitingSpendFailure
import dev.novux.dynasty.cbb.core.recruiting.OfficialVisitDistanceResolver
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubActionGateEvaluator
import dev.novux.dynasty.cbb.core.recruiting.RecruitingProspectActionGates
import dev.novux.dynasty.cbb.core.recruiting.RecruitingReservationRefund
import dev.novux.dynasty.cbb.core.recruiting.RecruitingSpendResult
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.offseason.weektwo.OffseasonScholarshipPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.academics.ProgramAcademicsGradeEngine
import dev.novux.dynasty.cbb.core.academics.AcademicsRecruitingBudgetEngine
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPrestigePool
import dev.novux.dynasty.cbb.core.recruiting.RecruitInterestSpendDelta
import dev.novux.dynasty.cbb.core.recruiting.RecruitingWeekGrant
import dev.novux.dynasty.cbb.core.recruiting.SigningDayPreseasonRecruitingTransition
import dev.novux.dynasty.cbb.core.save.DynastySchema
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.flow.first

private val Context.recruitingEconomyStore: DataStore<Preferences> by preferencesDataStore(
    name = "recruiting_economy",
)

class RecruitingEconomyRepository(
    private val context: Context,
    private val reservationRepository: RecruitingReservationRepository,
    private val dynastyRosterRepository: DynastyRosterRepository,
    private val offseasonWeekNineRepository: OffseasonWeekNineRepository,
    private val database: DynastyDatabase,
    private val packRepository: PackRepository,
    private val interestSpendRepository: RecruitingInterestSpendRepository,
    private val manualWeekSpendRepository: RecruitingManualWeekSpendRepository,
) {
    suspend fun loadInterestSpendDeltaByProspect(dynasty: LoadedDynasty): Map<String, Int> {
        val ledger = loadOrOpenCycle(dynasty)
        return interestSpendRepository.loadSpendDeltaByProspect(dynasty.dynastyId, ledger.cycleSeasonIndex)
    }

    suspend fun loadPointSpendByProspect(dynasty: LoadedDynasty): Map<String, Int> {
        val ledger = loadOrOpenCycle(dynasty)
        return interestSpendRepository.loadPointSpendByProspect(dynasty.dynastyId, ledger.cycleSeasonIndex)
    }

    private suspend fun recordProspectSpend(
        dynasty: LoadedDynasty,
        prospectId: String,
        pointsCharged: Int,
        interestDelta: Int,
    ) {
        val ledger = loadOrOpenCycle(dynasty)
        if (pointsCharged > 0) {
            interestSpendRepository.addPointSpend(
                dynasty.dynastyId,
                ledger.cycleSeasonIndex,
                prospectId,
                pointsCharged,
            )
            manualWeekSpendRepository.recordSpend(
                dynasty.dynastyId,
                dynasty.weekCheckpoint.activePosition,
                pointsCharged,
            )
        }
        if (interestDelta > 0) {
            interestSpendRepository.addSpendDelta(
                dynasty.dynastyId,
                ledger.cycleSeasonIndex,
                prospectId,
                interestDelta,
            )
        }
    }
    suspend fun resolveProgramPrestigeForDynasty(dynastyId: String, packId: String?): Int {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return DEFAULT_PROGRAM_PRESTIGE
        val effectivePackId = packId ?: DynastySchema.DEFAULT_PACK_ID
        val session = packRepository.restorePack(effectivePackId)
        val team = session.index.team(coach.userTeamId) ?: return DEFAULT_PROGRAM_PRESTIGE
        return team.programPrestige
    }

    suspend fun resolveUserProgramPrestige(dynasty: LoadedDynasty): Int =
        resolveProgramPrestigeForDynasty(dynasty.dynastyId, dynasty.summary.packId)

    suspend fun loadOrOpenCycle(
        dynasty: LoadedDynasty,
        programPrestigeOverride: Int? = null,
    ): RecruitingCycleLedger {
        val economy = economyFrom(dynasty.balanceRuleset)
        val programPrestige = programPrestigeOverride ?: resolveUserProgramPrestige(dynasty)
        val prefs = context.recruitingEconomyStore.data.first()
        val key = ledgerKey(dynasty.dynastyId)
        val raw = prefs[key]
        if (raw != null) {
            val decoded = decode(raw)
            if (decoded.cycleSeasonIndex != dynasty.metadata.seasonIndex) {
                val academicsGrade = programAcademicsGrade(dynasty.dynastyId, dynasty.metadata.seasonIndex)
                val ledger = RecruitingWeekGrant.openCycle(
                    economy,
                    cycleSeasonIndex = dynasty.metadata.seasonIndex,
                    programPrestige = programPrestige,
                    programAcademicsGrade = academicsGrade,
                )
                persist(dynasty.dynastyId, ledger)
                return ledger
            }
            return alignLedgerPrestige(dynasty, economy, decoded, programPrestige)
        }
        val academicsGrade = programAcademicsGrade(dynasty.dynastyId, dynasty.metadata.seasonIndex)
        val ledger = RecruitingWeekGrant.openCycle(
            economy,
            cycleSeasonIndex = dynasty.metadata.seasonIndex,
            programPrestige = programPrestige,
            programAcademicsGrade = academicsGrade,
        )
        persist(dynasty.dynastyId, ledger)
        return ledger
    }

    private suspend fun alignLedgerPrestige(
        dynasty: LoadedDynasty,
        economy: BalanceRecruitingEconomy,
        ledger: RecruitingCycleLedger,
        programPrestige: Int,
    ): RecruitingCycleLedger {
        if (ledger.programPrestige == programPrestige) return ledger
        val academicsGrade = ledger.programAcademicsGrade
            ?: programAcademicsGrade(dynasty.dynastyId, dynasty.metadata.seasonIndex)
        val tierBase = RecruitingPrestigePool.weeklyPoolPoints(economy, programPrestige)
        val weeklyPoolBase = AcademicsRecruitingBudgetEngine.effectiveWeeklyPool(tierBase, academicsGrade)
        val multiplier = AcademicsRecruitingBudgetEngine.budgetMultiplier(academicsGrade)
        val poolDelta = weeklyPoolBase - ledger.weeklyPoolBase
        val aligned = ledger.copy(
            programPrestige = programPrestige,
            weeklyPoolBase = weeklyPoolBase,
            pointsAvailable = (ledger.pointsAvailable + poolDelta).coerceAtLeast(0),
            programAcademicsGrade = academicsGrade,
            academicsBudgetMultiplier = multiplier,
        )
        persist(dynasty.dynastyId, aligned)
        return aligned
    }

    suspend fun revokeRefundableReservation(
        dynasty: LoadedDynasty,
        prospectId: String,
        action: RecruitingRefundableAction,
    ) {
        val active = reservationRepository.loadActive(dynasty.dynastyId)
        val match = active.firstOrNull { it.prospectId == prospectId && it.action == action } ?: return
        val ledger = loadOrOpenCycle(dynasty)
        val refunded = ledger.pointsAvailable + match.pointsReserved
        persist(dynasty.dynastyId, ledger.withPointsAvailable(refunded))
        reservationRepository.saveActive(
            dynasty.dynastyId,
            RecruitingRefundableActionSpend.revokeReservation(active, match.reservationId),
        )
    }

    suspend fun spendPointAction(
        dynasty: LoadedDynasty,
        prospectId: String,
        action: RecruitingPointAction,
        bonusAmount: Int? = null,
    ): RecruitingSpendResult {
        val actions = BalanceRecruitingActionsParser.parse(dynasty.balanceRuleset.recruiting)
        val ledger = loadOrOpenCycle(dynasty)
        val result = RecruitingActionSpend.spend(
            action = action,
            pointsAvailable = ledger.pointsAvailable,
            actions = actions,
            bonusAmount = bonusAmount,
        )
        if (result.success) {
            persist(dynasty.dynastyId, ledger.withPointsAvailable(result.pointsRemaining))
            val delta = RecruitInterestSpendDelta.forPointAction(action, actions, bonusAmount)
            recordProspectSpend(dynasty, prospectId, result.pointsCharged, delta)
            return result.copy(interestDeltaApplied = delta)
        }
        return result
    }

    suspend fun spendRefundableAction(
        dynasty: LoadedDynasty,
        prospectId: String,
        offer: Boolean,
        scout: Boolean,
        officialVisit: Boolean,
        visitDistance: OfficialVisitDistanceBand = OfficialVisitDistanceBand.Regional,
    ): RecruitingSpendResult {
        val actions = BalanceRecruitingActionsParser.parse(dynasty.balanceRuleset.recruiting)
        val ledger = loadOrOpenCycle(dynasty)
        val active = reservationRepository.loadActive(dynasty.dynastyId)
        val consumption = reservationRepository.loadSeasonConsumption(dynasty.dynastyId, ledger.cycleSeasonIndex)
        val pos = dynasty.weekCheckpoint.activePosition
        val offseasonWeek = pos.weekIndex.takeIf { pos.phase == CalendarPhase.Offseason }
        if (offer && !OffseasonScholarshipPolicy.scholarshipsOpen(pos.phase, offseasonWeek)) {
            return RecruitingSpendResult(
                action = RecruitingPointAction.ScholarshipOffer,
                pointsCharged = 0,
                pointsRemaining = ledger.pointsAvailable,
                countsAsWeekReservation = false,
                success = false,
                failure = RecruitingSpendFailure.ScholarshipsNotOpen,
            )
        }
        val (result, reservation) = when {
            offer -> RecruitingRefundableActionSpend.spendScholarshipOffer(prospectId, ledger.pointsAvailable, actions)
            scout -> RecruitingRefundableActionSpend.spendScoutTheirGame(
                prospectId,
                ledger.pointsAvailable,
                actions,
                active,
                consumption,
                pos.phase,
            )
            officialVisit -> RecruitingRefundableActionSpend.spendOfficialVisit(
                prospectId,
                ledger.pointsAvailable,
                actions,
                visitDistance,
                active,
                consumption,
                pos.phase,
                offseasonWeek,
            )
            else -> return RecruitingSpendResult(
                action = RecruitingPointAction.Dm,
                pointsCharged = 0,
                pointsRemaining = ledger.pointsAvailable,
                countsAsWeekReservation = false,
                success = false,
            )
        }
        if (result.success && reservation != null) {
            persist(dynasty.dynastyId, ledger.withPointsAvailable(result.pointsRemaining))
            reservationRepository.addReservation(dynasty.dynastyId, reservation)
            val delta = when {
                offer -> RecruitInterestSpendDelta.forPointAction(
                    RecruitingPointAction.ScholarshipOffer,
                    actions,
                    bonusAmount = null,
                )
                officialVisit -> RecruitInterestSpendDelta.forOfficialVisit(actions, visitDistance)
                else -> 0
            }
            recordProspectSpend(dynasty, prospectId, result.pointsCharged, delta)
            return result.copy(interestDeltaApplied = delta)
        }
        return result
    }

    suspend fun onCalendarWeekAdvanced(dynasty: LoadedDynasty, pointsSpentThisWeek: Int = 0) {
        val economy = economyFrom(dynasty.balanceRuleset)
        val ledger = loadOrOpenCycle(dynasty)
        val activeReservations = reservationRepository.loadActive(dynasty.dynastyId)
        val afterRefunds = ledger.copy(
            pointsAvailable = RecruitingReservationRefund.pointsAfterWeekCloseRefunds(
                ledger.pointsAvailable,
                activeReservations,
            ),
        )
        val pos = dynasty.weekCheckpoint.activePosition
        val offseasonWeek = pos.weekIndex.takeIf { pos.phase == CalendarPhase.Offseason }
        val (granted, _) = RecruitingWeekGrant.applyWeekAdvanceGrant(
            economy,
            afterRefunds,
            phase = pos.phase,
            offseasonWeekIndex = offseasonWeek,
            pointsSpentThisWeek = pointsSpentThisWeek,
        )
        var next = granted
        SigningDayPreseasonRecruitingTransition.reopenSeasonOneChasingAtWeekTwo(
            pos,
            dynasty.metadata.seasonIndex,
            next,
        )?.let { next = it }
        next = SigningDayPreseasonRecruitingTransition.ledgerAfterSigningDayAdministrativeWeeks(pos, next)
        if (SigningDayPreseasonRecruitingTransition.enteringPreseason(pos)) {
            next = SigningDayPreseasonRecruitingTransition.ledgerForPreseasonEntry(next)
        }
        reservationRepository.closeWeekReservations(dynasty.dynastyId, ledger.cycleSeasonIndex)
        persist(dynasty.dynastyId, next)
    }

    suspend fun prospectActionGates(
        dynasty: LoadedDynasty,
        prospectId: String,
        prospectHomeState: String,
        school: SchoolRecruitingContext,
    ): RecruitingProspectActionGates {
        val actions = BalanceRecruitingActionsParser.parse(dynasty.balanceRuleset.recruiting)
        val ledger = loadOrOpenCycle(dynasty)
        val consumption = reservationRepository.loadSeasonConsumption(dynasty.dynastyId, ledger.cycleSeasonIndex)
        val active = reservationRepository.loadActive(dynasty.dynastyId)
        val pos = dynasty.weekCheckpoint.activePosition
        return RecruitingHubActionGateEvaluator.forProspect(
            prospectId = prospectId,
            prospectHomeState = prospectHomeState,
            phase = pos.phase,
            offseasonWeekIndex = pos.weekIndex.takeIf { pos.phase == CalendarPhase.Offseason },
            pointsAvailable = ledger.pointsAvailable,
            actions = actions,
            school = school,
            consumption = consumption,
            activeReservations = active,
        )
    }

    fun visitDistanceFor(prospectHomeState: String, school: SchoolRecruitingContext): OfficialVisitDistanceBand =
        OfficialVisitDistanceResolver.resolve(prospectHomeState, school)

    suspend fun openNextCycleAfterSeasonReview(
        dynastyId: String,
        balance: BalanceRuleset,
        nextSeasonIndex: Int,
        postRecapPrestige: Int,
    ): RecruitingCycleLedger {
        val economy = economyFrom(balance)
        val academicsGrade = programAcademicsGrade(dynastyId, nextSeasonIndex)
        val ledger = RecruitingWeekGrant.openCycle(
            economy,
            cycleSeasonIndex = nextSeasonIndex,
            programPrestige = postRecapPrestige,
            programAcademicsGrade = academicsGrade,
        )
        persist(dynastyId, ledger)
        return ledger
    }

    private suspend fun programAcademicsGrade(dynastyId: String, seasonIndex: Int): Int {
        val roster = dynastyRosterRepository.listPlayers(dynastyId)
        val facilities = offseasonWeekNineRepository.loadFacilityGrades(dynastyId, seasonIndex)
        return ProgramAcademicsGradeEngine.snapshot(
            ProgramAcademicsGradeEngine.inputsFromRoster(roster, facilities),
        ).programGrade
    }

    private fun economyFrom(balance: BalanceRuleset): BalanceRecruitingEconomy =
        BalanceRecruitingParser.parse(balance.recruiting)

    private suspend fun persist(dynastyId: String, ledger: RecruitingCycleLedger) {
        context.recruitingEconomyStore.edit { prefs ->
            prefs[ledgerKey(dynastyId)] = encode(ledger)
        }
    }

    private fun ledgerKey(dynastyId: String) = stringPreferencesKey("ledger_$dynastyId")

    suspend fun persistLedger(dynastyId: String, ledger: RecruitingCycleLedger) {
        persist(dynastyId, ledger)
    }

    suspend fun closeCycleAfterHardSign(dynastyId: String) {
        val prefs = context.recruitingEconomyStore.data.first()
        val raw = prefs[ledgerKey(dynastyId)] ?: return
        val ledger = decode(raw).copy(cycleClosed = true, pointsAvailable = 0)
        persist(dynastyId, ledger)
        reservationRepository.saveActive(dynastyId, emptyList())
    }

    private fun encode(ledger: RecruitingCycleLedger): String =
        "${ledger.cycleSeasonIndex}|${ledger.programPrestige}|${ledger.weeklyPoolBase}|" +
            "${ledger.pointsAvailable}|${ledger.unspentAtLastWeekEnd}|${ledger.cycleClosed}|" +
            "${ledger.programAcademicsGrade ?: ""}|${ledger.academicsBudgetMultiplier}"

    private fun decode(raw: String): RecruitingCycleLedger {
        val p = raw.split("|")
        return when (p.size) {
            5 -> RecruitingCycleLedger(
                cycleSeasonIndex = p[0].toInt(),
                programPrestige = p[1].toInt(),
                weeklyPoolBase = p[2].toInt(),
                pointsAvailable = p[3].toInt(),
                unspentAtLastWeekEnd = p[4].toInt(),
                cycleClosed = false,
            )
            6 -> RecruitingCycleLedger(
                cycleSeasonIndex = p[0].toInt(),
                programPrestige = p[1].toInt(),
                weeklyPoolBase = p[2].toInt(),
                pointsAvailable = p[3].toInt(),
                unspentAtLastWeekEnd = p[4].toInt(),
                cycleClosed = p[5].toBooleanStrict(),
            )
            8 -> RecruitingCycleLedger(
                cycleSeasonIndex = p[0].toInt(),
                programPrestige = p[1].toInt(),
                weeklyPoolBase = p[2].toInt(),
                pointsAvailable = p[3].toInt(),
                unspentAtLastWeekEnd = p[4].toInt(),
                cycleClosed = p[5].toBooleanStrict(),
                programAcademicsGrade = p[6].takeIf { it.isNotBlank() }?.toInt(),
                academicsBudgetMultiplier = p[7].toDouble(),
            )
            else -> error("Invalid ledger encoding")
        }
    }

    companion object {
        const val DEFAULT_PROGRAM_PRESTIGE = 40
    }
}

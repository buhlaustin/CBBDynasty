package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.novux.dynasty.cbb.core.recruiting.RecruitingActionBlockReason
import dev.novux.dynasty.cbb.core.recruiting.RecruitingProspectActionGates
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.balance.BalanceNilParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingActionsParser
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.nil.NilBudgetDrillDown
import dev.novux.dynasty.cbb.core.nil.NilBudgetLedger
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.portal.PortalCycleState
import dev.novux.dynasty.cbb.core.portal.PortalEntrant
import dev.novux.dynasty.cbb.core.portal.PortalRecruitProspectMapper
import dev.novux.dynasty.cbb.core.portal.PortalRecruitingHub
import dev.novux.dynasty.cbb.core.portal.PortalStarBands
import dev.novux.dynasty.cbb.core.recruiting.HsJucoBoard
import dev.novux.dynasty.cbb.core.recruiting.HsJucoRecruitBoard
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubFocusPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubDefaults
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTabPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTestHooks
import dev.novux.dynasty.cbb.core.recruiting.RecruitingWeekReservation
import dev.novux.dynasty.cbb.core.signingday.SigningDaySoftCommitInputs
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.core.recruiting.RecruitingPointAction
import dev.novux.dynasty.cbb.core.recruiting.RecruitingSpendFailure
import dev.novux.dynasty.cbb.core.recruiting.RecruitingRefundableAction
import dev.novux.dynasty.cbb.core.recruiting.RecruitScoutLetterEngine
import dev.novux.dynasty.cbb.core.recruiting.RecommendedProspectRow
import dev.novux.dynasty.cbb.core.recruiting.RecommendedRecruitList
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.recruiting.SignLikelihoodBadge
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository
import dev.novux.dynasty.cbb.data.nil.NilBudgetRepository
import dev.novux.dynasty.cbb.core.signingday.NationalRankRow
import dev.novux.dynasty.cbb.core.signingday.NationalRecruitingRankings
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.data.portal.PortalCycleRepository
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromise
import dev.novux.dynasty.cbb.data.recruiting.RecruitOfferPromiseRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecruitingHubScreen(
    loaded: LoadedDynasty,
    recruitingEconomyRepository: RecruitingEconomyRepository,
    recruitOfferPromiseRepository: RecruitOfferPromiseRepository,
    nilBudgetRepository: NilBudgetRepository,
    nilAnnualPoolRepository: NilAnnualPoolRepository,
    weekNineRepository: OffseasonWeekNineRepository? = null,
    incomingClassSeedRepository: IncomingClassSeedRepository,
    portalCycleRepository: PortalCycleRepository,
    initialTab: RecruitingHubTab = RecruitingHubDefaults.defaultTab,
    initialFocusProspectId: String? = null,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    var tabIndex by remember(initialTab) {
        mutableIntStateOf(RecruitingHubDefaults.tabs.indexOf(initialTab))
    }
    var focusedProspectId by remember(initialFocusProspectId) { mutableStateOf(initialFocusProspectId) }
    var ledger by remember { mutableStateOf<RecruitingCycleLedger?>(null) }
    var nilLedger by remember { mutableStateOf<NilBudgetLedger?>(null) }
    var nilDrillDown by remember { mutableStateOf<NilBudgetDrillDown?>(null) }
    var nilOffers by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var nilBoostUsd by remember { mutableIntStateOf(0) }
    var nilBasePoolUsd by remember { mutableStateOf(0L) }
    var facilityGrades by remember {
        mutableStateOf(dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityGrades())
    }
    var gateRefresh by remember { mutableIntStateOf(0) }
    var offerPromises by remember { mutableStateOf<Map<String, RecruitOfferPromise>>(emptyMap()) }
    var promisePickerTarget by remember {
        mutableStateOf<Pair<String, String>?>(null)
    }
    var bonusPickerTarget by remember {
        mutableStateOf<BonusPointsPickerTarget?>(null)
    }
    var watchlistIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var lostProspectIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var offerReservations by remember { mutableStateOf<List<RecruitingWeekReservation>>(emptyList()) }
    var softCommitInputs by remember { mutableStateOf<SigningDaySoftCommitInputs?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dynastyApp = context.requireDynastyApplication()
    val insufficientPointsMessage = stringResource(R.string.recruiting_insufficient_points)
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val nilRules = remember(loaded.balanceRuleset) {
        BalanceNilParser.parse(loaded.balanceRuleset.nil)
    }
    val recruitingActions = remember(loaded.balanceRuleset) {
        BalanceRecruitingActionsParser.parse(loaded.balanceRuleset.recruiting)
    }

    val submitScholarshipOffer: (String, CoachPromiseType) -> Unit = { prospectId, promiseType ->
        scope.launch {
            val stayTopN = if (promiseType == CoachPromiseType.StayPledgeTopN) {
                dev.novux.dynasty.cbb.core.player.loyalty.CoachPromise.DEFAULT_STAY_TOP_N
            } else {
                null
            }
            recruitOfferPromiseRepository.save(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
                RecruitOfferPromise(
                    prospectId = prospectId,
                    promiseType = promiseType,
                    stayTopN = stayTopN,
                ),
            )
            recruitingEconomyRepository.spendRefundableAction(
                loaded,
                prospectId = prospectId,
                offer = true,
                scout = false,
                officialVisit = false,
            )
            offerPromises = recruitOfferPromiseRepository.loadAll(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
            )
            ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
            gateRefresh++
            promisePickerTarget = null
        }
    }

    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex, gateRefresh) {
        offerPromises = recruitOfferPromiseRepository.loadAll(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        val loadedLedger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
        ledger = loadedLedger
        nilBoostUsd = weekNineRepository?.nilPoolBoostUsd(loaded.dynastyId, loaded.metadata.seasonIndex) ?: 0
        nilBasePoolUsd = nilAnnualPoolRepository.cycleBasePoolUsd(loaded)
        val poolUsd = nilBasePoolUsd + nilBoostUsd
        nilOffers = nilBudgetRepository.loadOffers(loaded.dynastyId, loaded.metadata.seasonIndex)
        nilLedger = nilBudgetRepository.loadLedger(loaded.dynastyId, loaded.metadata.seasonIndex, poolUsd)
        facilityGrades = weekNineRepository?.loadFacilityGrades(loaded.dynastyId, loaded.metadata.seasonIndex)
            ?: dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityGrades()
    }


    val recommendedConfig = remember(loaded.balanceRuleset) {
        BalanceRecruitingRecommendedParser.parse(loaded.balanceRuleset.recruiting)
    }
    var schoolContext by remember { mutableStateOf<SchoolRecruitingContext?>(null) }
    var interestSpendByProspect by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var userProgramPrestige by remember {
        mutableIntStateOf(RecruitingEconomyRepository.DEFAULT_PROGRAM_PRESTIGE)
    }
    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex, gateRefresh) {
        val season = loaded.metadata.seasonIndex
        watchlistIds = dynastyApp.recruitingWatchlistRepository.load(loaded.dynastyId, season)
        lostProspectIds = dynastyApp.recruitingLostProspectsRepository.load(loaded.dynastyId, season)
        offerReservations = dynastyApp.recruitingReservationRepository.loadActive(loaded.dynastyId)
        softCommitInputs = dynastyApp.signingDayRepository.softCommitInputsFor(loaded)
    }

    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex, nilBoostUsd, facilityGrades, gateRefresh) {
        userProgramPrestige = recruitingEconomyRepository.resolveUserProgramPrestige(loaded)
        schoolContext = nilAnnualPoolRepository.userSchoolRecruitingContext(
            loaded,
            nilBoostUsd,
            facilityGrades,
        )
        interestSpendByProspect = recruitingEconomyRepository.loadInterestSpendDeltaByProspect(loaded)
    }
    val offerSnapshot = nilOffers.mapValues { it.value.toInt() }
    val resolvedSchoolContext = schoolContext ?: SchoolRecruitingContext(
        programPrestige = userProgramPrestige,
        schoolHomeState = "OH",
        nilPoolAnnualUsd = (nilBasePoolUsd + nilBoostUsd).toInt(),
    )
    var recruitingPool by remember { mutableStateOf<List<RecruitProspect>?>(null) }
    var hsJucoBoard by remember { mutableStateOf<HsJucoBoard?>(null) }
    var hsTop60 by remember { mutableStateOf<List<NationalRankRow>>(emptyList()) }
    var hsPoolReady by remember { mutableStateOf(false) }
    var portalEntrants by remember { mutableStateOf<List<PortalEntrant>>(emptyList()) }
    val calendarPosition = loaded.weekCheckpoint.activePosition
    val portalWindowOpen = PortalRecruitingHub.isRecruitingWindowOpen(
        calendarPosition.phase,
        calendarPosition.weekIndex.takeIf { calendarPosition.phase == CalendarPhase.Offseason },
    )

    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex) {
        hsPoolReady = false
        val pool = incomingClassSeedRepository.ensurePool(loaded.dynastyId, loaded.metadata.seasonIndex)
        recruitingPool = pool
        hsJucoBoard = HsJucoRecruitBoard.partition(pool)
        hsTop60 = NationalRecruitingRankings.top60FromPool(pool)
        hsPoolReady = true
    }

    val fullPool = recruitingPool.orEmpty()

    LaunchedEffect(initialFocusProspectId) {
        if (RecruitingHubFocusPolicy.isValidFocusProspectId(initialFocusProspectId)) {
            tabIndex = RecruitingHubDefaults.tabs.indexOf(RecruitingHubFocusPolicy.portalDeepLinkTab)
            focusedProspectId = initialFocusProspectId
        }
    }
    val toggleBoard: (String) -> Unit = { prospectId ->
        scope.launch {
            val season = loaded.metadata.seasonIndex
            watchlistIds = if (prospectId in watchlistIds) {
                dynastyApp.recruitingWatchlistRepository.remove(loaded.dynastyId, season, prospectId)
            } else {
                dynastyApp.recruitingWatchlistRepository.add(loaded.dynastyId, season, prospectId)
            }
        }
    }

    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex, portalWindowOpen) {
        if (!portalWindowOpen) {
            portalEntrants = emptyList()
            return@LaunchedEffect
        }
        val state: PortalCycleState = portalCycleRepository.loadOrSimulate(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        portalEntrants = if (state.top40Locked) {
            state.entrants
        } else {
            state.onOffseasonWeekStarted(calendarPosition.weekIndex.coerceAtLeast(2)).entrants
        }
    }

    val portalRecommendedRows = remember(
        portalEntrants,
        portalWindowOpen,
        schoolContext,
        recommendedConfig,
        offerSnapshot,
        nilRules,
        interestSpendByProspect,
        offerPromises,
        gateRefresh,
    ) {
        if (!portalWindowOpen) return@remember emptyList()
        val ctx = schoolContext ?: return@remember emptyList()
        val entrants = if (portalEntrants.any { it.stars == null }) {
            PortalStarBands.applyToEntrants(portalEntrants)
        } else {
            portalEntrants
        }
        if (entrants.isEmpty()) return@remember emptyList()
        val prospects = entrants.map { entrant ->
            PortalRecruitProspectMapper.toRecruitProspect(entrant, entrant.priorProgramId)
        }
        RecommendedRecruitList.build(
            prospects,
            ctx,
            recommendedConfig,
            nilOffersByProspect = offerSnapshot,
            nilRules = nilRules,
            spendDeltaByProspect = interestSpendByProspect,
            offerPromisesByProspect = offerPromises,
        )
    }

    val recommendedRows = remember(
        recruitingPool,
        schoolContext,
        recommendedConfig,
        offerSnapshot,
        nilRules,
        interestSpendByProspect,
        offerPromises,
        gateRefresh,
    ) {
        if (RecruitingHubTestHooks.forceEmptyRecommendedPool) return@remember emptyList()
        val pool = recruitingPool ?: return@remember emptyList()
        val ctx = schoolContext ?: return@remember emptyList()
        RecommendedRecruitList.build(
            pool,
            ctx,
            recommendedConfig,
            nilOffersByProspect = offerSnapshot,
            nilRules = nilRules,
            spendDeltaByProspect = interestSpendByProspect,
            offerPromisesByProspect = offerPromises,
        )
    }

    val activePosition = loaded.weekCheckpoint.activePosition
    val hubOffseasonWeek = activePosition.weekIndex.takeIf { activePosition.phase == CalendarPhase.Offseason }
    val ledgerCycleClosed = ledger?.cycleClosed == true
    val hubNavigable = ledger != null &&
        RecruitingCyclePolicy.hubNavigable(
            activePosition.phase,
            hubOffseasonWeek,
            ledgerCycleClosed,
        )
    when {
        ledger == null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return
        }
        !hubNavigable -> {
            RecruitingCycleClosedBody(onBack = onBack)
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.recruiting_hub_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (activePosition.phase == CalendarPhase.Preseason) {
            Text(
                text = stringResource(R.string.recruiting_hub_preseason_setup_banner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.recruiting_hub_cycle_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ledger?.let { row ->
            Text(
                text = stringResource(
                    R.string.recruiting_points_pool,
                    row.pointsAvailable,
                    row.weeklyPoolBase,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.recruiting_carryover_cadence_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            row.programAcademicsGrade?.let { grade ->
                Text(
                    text = stringResource(
                        R.string.recruiting_academics_multiplier,
                        grade,
                        row.academicsBudgetMultiplier,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        nilLedger?.let { nilRow ->
            Column(
                modifier = Modifier.clickable {
                    scope.launch {
                        val poolUsd = nilBasePoolUsd + nilBoostUsd
                        val namesById = fullPool.associate { it.prospectId to it.displayName }
                        nilDrillDown = nilBudgetRepository.loadDrillDown(
                            loaded.dynastyId,
                            loaded.metadata.seasonIndex,
                            poolUsd,
                        ) { id -> namesById[id] ?: id }
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        R.string.recruiting_nil_budget,
                        currency.format(nilRow.nilRemainingUsd),
                        currency.format(nilRow.nilPoolAnnualUsd),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.recruiting_nil_breakdown_line,
                        currency.format(nilRow.nilCommittedUsd),
                        currency.format(nilRow.nilReservedUsd),
                        currency.format(nilRow.nilRemainingUsd),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.recruiting_nil_budget_tap_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        nilDrillDown?.let { drill ->
            RecruitingNilBudgetDrillDownSheet(
                drillDown = drill,
                currency = currency,
                onDismiss = { nilDrillDown = null },
            )
        }
        if (nilBoostUsd > 0) {
            Text(
                text = stringResource(R.string.recruiting_nil_boost_line, currency.format(nilBoostUsd)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        focusedProspectId?.let { focusId ->
            if (RecruitingHubFocusPolicy.isValidFocusProspectId(focusId)) {
                val focusLabel = fullPool.find { it.prospectId == focusId }?.displayName ?: focusId
                Text(
                    text = stringResource(R.string.recruiting_hub_focus_from_news, focusLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        ScrollableTabRow(selectedTabIndex = tabIndex) {
            RecruitingHubDefaults.tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(recruitingHubTabLabel(tab)) },
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            when (RecruitingHubDefaults.tabs[tabIndex]) {
                RecruitingHubTab.Recommended -> RecommendedTabBody(
                    loaded = loaded,
                    rows = recommendedRows,
                    schoolContext = resolvedSchoolContext,
                    recruitingEconomyRepository = recruitingEconomyRepository,
                    nilRules = nilRules,
                    nilOffers = nilOffers,
                    scholarshipOfferPointCost = recruitingActions.scholarshipOffer.cost,
                    gateRefresh = gateRefresh,
                    offerPromises = offerPromises,
                    poolLoaded = hsPoolReady,
                    onBrowseHsJuco = {
                        tabIndex = RecruitingHubDefaults.tabs.indexOf(RecruitingHubTab.AllHsJuco)
                    },
                    onSpend = {
                        scope.launch {
                            recruitingEconomyRepository.spendRefundableAction(
                                loaded,
                                prospectId = it.prospectId,
                                offer = false,
                                scout = it.scout,
                                officialVisit = it.officialVisit,
                                visitDistance = recruitingEconomyRepository.visitDistanceFor(
                                    it.prospectHomeState,
                                    resolvedSchoolContext,
                                ),
                            )
                            ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
                            gateRefresh++
                        }
                    },
                    onRequestScholarshipOffer = { prospectId, displayName ->
                        promisePickerTarget = prospectId to displayName
                    },
                    onRequestBonusPoints = { prospectId, displayName, maxPoints ->
                        bonusPickerTarget = BonusPointsPickerTarget(prospectId, displayName, maxPoints)
                    },
                    onAssignNil = { prospectId, nilAskUsd, offerUsd ->
                        scope.launch {
                            val poolUsd = nilBasePoolUsd + nilBoostUsd
                            nilBudgetRepository.assignOffer(
                                loaded.dynastyId,
                                loaded.metadata.seasonIndex,
                                prospectId,
                                nilAskUsd,
                                offerUsd,
                                nilRules,
                                poolUsd,
                            )
                            nilOffers = nilBudgetRepository.loadOffers(loaded.dynastyId, loaded.metadata.seasonIndex)
                            nilLedger = nilBudgetRepository.loadLedger(
                                loaded.dynastyId,
                                loaded.metadata.seasonIndex,
                                poolUsd,
                            )
                            gateRefresh++
                        }
                    },
                    onPointAction = { prospectId, action, bonusAmount ->
                        scope.launch {
                            val result = recruitingEconomyRepository.spendPointAction(
                                loaded,
                                prospectId,
                                action,
                                bonusAmount,
                            )
                            if (!result.success && result.failure == RecruitingSpendFailure.InsufficientPoints) {
                                Toast.makeText(context, insufficientPointsMessage, Toast.LENGTH_SHORT).show()
                            }
                            ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
                            gateRefresh++
                        }
                    },
                    onRevokeOffer = { prospectId ->
                        scope.launch {
                            recruitOfferPromiseRepository.remove(
                                loaded.dynastyId,
                                loaded.metadata.seasonIndex,
                                prospectId,
                            )
                            recruitingEconomyRepository.revokeRefundableReservation(
                                loaded,
                                prospectId,
                                RecruitingRefundableAction.ScholarshipOffer,
                            )
                            lostProspectIds = dynastyApp.recruitingLostProspectsRepository.record(
                                loaded.dynastyId,
                                loaded.metadata.seasonIndex,
                                prospectId,
                            )
                            offerReservations = dynastyApp.recruitingReservationRepository.loadActive(loaded.dynastyId)
                            offerPromises = recruitOfferPromiseRepository.loadAll(
                                loaded.dynastyId,
                                loaded.metadata.seasonIndex,
                            )
                            ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
                            gateRefresh++
                        }
                    },
                )
                RecruitingHubTab.AllHsJuco -> HsJucoTabBody(
                    board = hsJucoBoard,
                    top60 = hsTop60,
                    poolLoaded = hsPoolReady,
                )
                RecruitingHubTab.HighSchool -> RecruitPoolListTabBody(
                    prospects = RecruitingHubTabPolicy.filterPool(fullPool, RecruitingHubTab.HighSchool),
                    emptyText = stringResource(R.string.recruiting_pool_tab_empty),
                    watchlistIds = watchlistIds,
                    onToggleBoard = toggleBoard,
                    modifier = Modifier.weight(1f),
                )
                RecruitingHubTab.JuniorCollege -> RecruitPoolListTabBody(
                    prospects = RecruitingHubTabPolicy.filterPool(fullPool, RecruitingHubTab.JuniorCollege),
                    emptyText = stringResource(R.string.recruiting_pool_tab_empty),
                    watchlistIds = watchlistIds,
                    onToggleBoard = toggleBoard,
                    modifier = Modifier.weight(1f),
                )
                RecruitingHubTab.BoardTargets -> RecruitPoolListTabBody(
                    prospects = RecruitingHubTabPolicy.boardTargets(fullPool, watchlistIds),
                    emptyText = stringResource(R.string.recruiting_pool_tab_empty),
                    watchlistIds = watchlistIds,
                    onToggleBoard = toggleBoard,
                    modifier = Modifier.weight(1f),
                )
                RecruitingHubTab.Committed -> {
                    val committed = softCommitInputs?.let { inputs ->
                        RecruitingHubTabPolicy.committed(fullPool, offerReservations, inputs)
                    } ?: emptyList()
                    RecruitPoolListTabBody(
                        prospects = committed,
                        emptyText = stringResource(R.string.recruiting_pool_tab_empty),
                        watchlistIds = watchlistIds,
                        onToggleBoard = toggleBoard,
                        modifier = Modifier.weight(1f),
                    )
                }
                RecruitingHubTab.Lost -> RecruitPoolListTabBody(
                    prospects = RecruitingHubTabPolicy.lost(fullPool, lostProspectIds),
                    emptyText = stringResource(R.string.recruiting_pool_tab_empty),
                    watchlistIds = watchlistIds,
                    onToggleBoard = toggleBoard,
                    modifier = Modifier.weight(1f),
                )
                RecruitingHubTab.TransferPortal -> PortalRecruitingTabBody(
                    windowOpen = portalWindowOpen,
                    modifier = Modifier.weight(1f),
                    top40Content = {
                        PortalTop40Section(
                            entrants = portalEntrants,
                            highlightEntrantId = focusedProspectId,
                        )
                    },
                    recommendedContent = {
                        if (portalRecommendedRows.isEmpty()) {
                            Text(
                                stringResource(R.string.recruiting_portal_empty),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else {
                            Text(
                                stringResource(R.string.recruiting_portal_recommended_heading),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            RecommendedTabBody(
                                loaded = loaded,
                                rows = portalRecommendedRows,
                                schoolContext = resolvedSchoolContext,
                                recruitingEconomyRepository = recruitingEconomyRepository,
                                nilRules = nilRules,
                                nilOffers = nilOffers,
                                scholarshipOfferPointCost = recruitingActions.scholarshipOffer.cost,
                                gateRefresh = gateRefresh,
                                offerPromises = offerPromises,
                                modifier = Modifier.weight(1f),
                                onSpend = {
                                    scope.launch {
                                        recruitingEconomyRepository.spendRefundableAction(
                                            loaded,
                                            prospectId = it.prospectId,
                                            offer = false,
                                            scout = it.scout,
                                            officialVisit = it.officialVisit,
                                            visitDistance = recruitingEconomyRepository.visitDistanceFor(
                                                it.prospectHomeState,
                                                resolvedSchoolContext,
                                            ),
                                        )
                                        ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
                                        gateRefresh++
                                    }
                                },
                                onRequestScholarshipOffer = { prospectId, displayName ->
                                    promisePickerTarget = prospectId to displayName
                                },
                                onRequestBonusPoints = { prospectId, displayName, maxPoints ->
                                    bonusPickerTarget = BonusPointsPickerTarget(prospectId, displayName, maxPoints)
                                },
                                onAssignNil = { prospectId, nilAskUsd, offerUsd ->
                                    scope.launch {
                                        val poolUsd = nilBasePoolUsd + nilBoostUsd
                                        nilBudgetRepository.assignOffer(
                                            loaded.dynastyId,
                                            loaded.metadata.seasonIndex,
                                            prospectId,
                                            nilAskUsd,
                                            offerUsd,
                                            nilRules,
                                            poolUsd,
                                        )
                                        nilOffers = nilBudgetRepository.loadOffers(loaded.dynastyId, loaded.metadata.seasonIndex)
                                        nilLedger = nilBudgetRepository.loadLedger(
                                            loaded.dynastyId,
                                            loaded.metadata.seasonIndex,
                                            poolUsd,
                                        )
                                        gateRefresh++
                                    }
                                },
                                onPointAction = { prospectId, action, bonusAmount ->
                                    scope.launch {
                                        val result = recruitingEconomyRepository.spendPointAction(
                                            loaded,
                                            prospectId,
                                            action,
                                            bonusAmount,
                                        )
                                        if (!result.success && result.failure == RecruitingSpendFailure.InsufficientPoints) {
                                            Toast.makeText(context, insufficientPointsMessage, Toast.LENGTH_SHORT).show()
                                        }
                                        ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
                                        gateRefresh++
                                    }
                                },
                                onRevokeOffer = { prospectId ->
                                    scope.launch {
                                        recruitOfferPromiseRepository.remove(
                                            loaded.dynastyId,
                                            loaded.metadata.seasonIndex,
                                            prospectId,
                                        )
                                        recruitingEconomyRepository.revokeRefundableReservation(
                                            loaded,
                                            prospectId,
                                            RecruitingRefundableAction.ScholarshipOffer,
                                        )
                                        lostProspectIds = dynastyApp.recruitingLostProspectsRepository.record(
                                            loaded.dynastyId,
                                            loaded.metadata.seasonIndex,
                                            prospectId,
                                        )
                                        offerReservations = dynastyApp.recruitingReservationRepository.loadActive(
                                            loaded.dynastyId,
                                        )
                                        offerPromises = recruitOfferPromiseRepository.loadAll(
                                            loaded.dynastyId,
                                            loaded.metadata.seasonIndex,
                                        )
                                        ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
                                        gateRefresh++
                                    }
                                },
                            )
                        }
                    },
                )
            }
        }
        promisePickerTarget?.let { (prospectId, displayName) ->
            RecruitPromisePickerSheet(
                prospectDisplayName = displayName,
                onDismiss = { promisePickerTarget = null },
                onConfirm = { promiseType -> submitScholarshipOffer(prospectId, promiseType) },
            )
        }
        bonusPickerTarget?.let { target ->
            RecruitBonusPointsPickerSheet(
                prospectDisplayName = target.displayName,
                pointsAvailable = target.pointsAvailable,
                onDismiss = { bonusPickerTarget = null },
                onConfirm = { amount ->
                    scope.launch {
                        val result = recruitingEconomyRepository.spendPointAction(
                            loaded,
                            target.prospectId,
                            RecruitingPointAction.BonusPoints,
                            amount,
                        )
                        if (!result.success) {
                            when (result.failure) {
                                RecruitingSpendFailure.InsufficientPoints ->
                                    Toast.makeText(context, insufficientPointsMessage, Toast.LENGTH_SHORT).show()
                                RecruitingSpendFailure.InvalidBonusAmount ->
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.recruiting_bonus_invalid_amount),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                else -> Unit
                            }
                        }
                        ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
                        gateRefresh++
                        bonusPickerTarget = null
                    }
                },
            )
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_back))
        }
    }
}

@Composable
private fun PlaceholderBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
    )
}

private data class ProspectSpendRequest(
    val prospectId: String,
    val prospectHomeState: String,
    val scout: Boolean,
    val officialVisit: Boolean,
)

private data class BonusPointsPickerTarget(
    val prospectId: String,
    val displayName: String,
    val pointsAvailable: Int,
)

@Composable
private fun RecommendedTabBody(
    loaded: LoadedDynasty,
    rows: List<RecommendedProspectRow>,
    schoolContext: SchoolRecruitingContext,
    recruitingEconomyRepository: RecruitingEconomyRepository,
    nilRules: dev.novux.dynasty.cbb.core.balance.BalanceNilRules,
    nilOffers: Map<String, Long>,
    scholarshipOfferPointCost: Int,
    gateRefresh: Int,
    offerPromises: Map<String, RecruitOfferPromise>,
    poolLoaded: Boolean = true,
    onBrowseHsJuco: () -> Unit = {},
    modifier: Modifier = Modifier,
    onSpend: (ProspectSpendRequest) -> Unit,
    onRequestScholarshipOffer: (prospectId: String, displayName: String) -> Unit,
    onRequestBonusPoints: (prospectId: String, displayName: String, maxPoints: Int) -> Unit,
    onAssignNil: (prospectId: String, nilAskUsd: Int, offerUsd: Long) -> Unit,
    onPointAction: (prospectId: String, RecruitingPointAction, Int?) -> Unit,
    onRevokeOffer: (String) -> Unit,
) {
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    when {
        !poolLoaded -> {
            Text(
                text = stringResource(R.string.recruiting_hs_juco_loading),
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier.fillMaxSize(),
            )
        }
        rows.isEmpty() -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.recruiting_recommended_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.recruiting_recommended_empty_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onBrowseHsJuco, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.recruiting_recommended_empty_cta))
                }
            }
        }
        else -> {
    val likelyCount = rows.count { it.badge == SignLikelihoodBadge.Likely || it.badge == SignLikelihoodBadge.Possible }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.recruiting_recommended_summary, likelyCount, rows.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.recruiting_recommended_elite_hide_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(R.string.recruiting_influence_board_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(
                    R.string.recruiting_nil_recommended_assign_hint,
                    nilRules.maxOfferMultiplierOfAsk,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(R.string.recruiting_official_visit_window_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        items(rows, key = { it.prospect.prospectId }) { row ->
            var gates by remember(row.prospect.prospectId, gateRefresh) {
                mutableStateOf<RecruitingProspectActionGates?>(null)
            }
            LaunchedEffect(row.prospect.prospectId, gateRefresh, loaded.dynastyId) {
                gates = recruitingEconomyRepository.prospectActionGates(
                    loaded,
                    row.prospect.prospectId,
                    row.prospect.homeState,
                    schoolContext,
                )
            }
            RecommendedProspectRow(
                row = row,
                gates = gates,
                currency = currency,
                nilRules = nilRules,
                nilOfferUsd = nilOffers[row.prospect.prospectId] ?: 0L,
                scholarshipOfferPointCost = scholarshipOfferPointCost,
                scholarshipOfferPromiseLabel = offerPromises[row.prospect.prospectId]?.displayLabel(),
                onScout = {
                    onSpend(
                        ProspectSpendRequest(
                            row.prospect.prospectId,
                            row.prospect.homeState,
                            scout = true,
                            officialVisit = false,
                        ),
                    )
                },
                onOfficialVisit = {
                    onSpend(
                        ProspectSpendRequest(
                            row.prospect.prospectId,
                            row.prospect.homeState,
                            scout = false,
                            officialVisit = true,
                        ),
                    )
                },
                onScholarshipOffer = {
                    onRequestScholarshipOffer(row.prospect.prospectId, row.prospect.displayName)
                },
                onMeetNilAsk = {
                    onAssignNil(row.prospect.prospectId, row.prospect.nilAskUsd, row.prospect.nilAskUsd.toLong())
                },
                onClearNil = { onAssignNil(row.prospect.prospectId, row.prospect.nilAskUsd, 0L) },
                onAssignPartialNil = { offerUsd ->
                    onAssignNil(row.prospect.prospectId, row.prospect.nilAskUsd, offerUsd)
                },
                onDm = {
                    gates?.dm?.takeIf { it.allowed }?.let {
                        onPointAction(row.prospect.prospectId, RecruitingPointAction.Dm, null)
                    }
                },
                onPhoneCall = {
                    gates?.phoneCall?.takeIf { it.allowed }?.let {
                        onPointAction(row.prospect.prospectId, RecruitingPointAction.PhoneCall, null)
                    }
                },
                onSendTheHouse = {
                    gates?.sendTheHouse?.takeIf { it.allowed }?.let {
                        onPointAction(row.prospect.prospectId, RecruitingPointAction.SendTheHouse, null)
                    }
                },
                onRequestBonusPoints = {
                    gates?.bonusPoints?.takeIf { it.allowed }?.let { gate ->
                        onRequestBonusPoints(
                            row.prospect.prospectId,
                            row.prospect.displayName,
                            gate.pointCost,
                        )
                    }
                },
                onRevokeOffer = { onRevokeOffer(row.prospect.prospectId) },
            )
        }
    }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendedProspectRow(
    row: RecommendedProspectRow,
    gates: RecruitingProspectActionGates?,
    currency: NumberFormat,
    nilRules: dev.novux.dynasty.cbb.core.balance.BalanceNilRules,
    nilOfferUsd: Long,
    scholarshipOfferPointCost: Int,
    scholarshipOfferPromiseLabel: String? = null,
    onScout: () -> Unit,
    onOfficialVisit: () -> Unit,
    onScholarshipOffer: () -> Unit,
    onMeetNilAsk: () -> Unit,
    onClearNil: () -> Unit,
    onAssignPartialNil: (Long) -> Unit,
    onDm: () -> Unit,
    onPhoneCall: () -> Unit,
    onSendTheHouse: () -> Unit,
    onRequestBonusPoints: () -> Unit,
    onRevokeOffer: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.prospect.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            Text(
                text = stringResource(
                    R.string.recruiting_row_meta,
                    row.prospect.stars,
                    row.prospect.position.label,
                    row.prospect.homeState,
                    row.prospect.scoutLetterOverall,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (row.prospect.topPriorities.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.prospect.topPriorities.forEach { priority ->
                        AssistChip(
                            onClick = {},
                            label = { Text(priority.chipLabel) },
                        )
                    }
                }
            }
            row.influenceBoard.userRank?.let { rank ->
                Text(
                    text = stringResource(
                        R.string.recruiting_influence_row,
                        rank,
                        row.influenceBoard.closeness,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            RecruitInterestMeterRow(
                baselineInterest = row.baselineInterest,
                liveInterest = row.liveInterest,
            )
        }
        SignLikelihoodChip(row.badge)
            Text(
                text = stringResource(R.string.recruiting_sign_likelihood_baseline_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RecruitScoutLetterSheet(letters = RecruitScoutLetterEngine.fromProspect(row.prospect))
        Text(
            text = stringResource(R.string.recruiting_nil_ask_line, currency.format(row.prospect.nilAskUsd)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RecruitNilAssignCapLine(nilAskUsd = row.prospect.nilAskUsd, nilRules = nilRules)
        RecruitNilPartialOfferControls(
            nilAskUsd = row.prospect.nilAskUsd,
            nilRules = nilRules,
            currentOfferUsd = nilOfferUsd,
            currency = currency,
            onApplyPartialOffer = onAssignPartialNil,
        )
        if (nilOfferUsd > 0L) {
            Text(
                text = stringResource(R.string.recruiting_nil_offer_line, currency.format(nilOfferUsd)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onMeetNilAsk, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.recruiting_nil_assign_meet_ask))
            }
            OutlinedButton(onClick = onClearNil, enabled = nilOfferUsd > 0L, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.recruiting_nil_assign_clear))
            }
        }
        gates?.let { g ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onDm, enabled = g.dm.allowed, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.recruiting_action_dm, g.dm.pointCost))
                }
                OutlinedButton(onClick = onPhoneCall, enabled = g.phoneCall.allowed, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.recruiting_action_phone, g.phoneCall.pointCost))
                }
            }
            OutlinedButton(
                onClick = onSendTheHouse,
                enabled = g.sendTheHouse.allowed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.recruiting_action_send_house, g.sendTheHouse.pointCost))
            }
            if (g.hasActiveScholarshipOffer) {
                scholarshipOfferPromiseLabel?.let { promiseLabel ->
                    Text(
                        text = stringResource(R.string.recruiting_promise_offer_line, promiseLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedButton(onClick = onRevokeOffer, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.recruiting_action_revoke_offer))
                }
            } else {
                OutlinedButton(
                    onClick = onScholarshipOffer,
                    enabled = g.scholarshipOffer.allowed,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.recruiting_action_scholarship_offer, g.scholarshipOffer.pointCost))
                }
            }
            OutlinedButton(
                onClick = onRequestBonusPoints,
                enabled = g.bonusPoints.allowed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        R.string.recruiting_action_bonus_points,
                        g.bonusPoints.pointCost,
                    ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onScout,
                    enabled = g.scoutTheirGame.allowed,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.recruiting_action_scout_game, g.scoutTheirGame.pointCost))
                }
                OutlinedButton(
                    onClick = onOfficialVisit,
                    enabled = g.officialVisit.allowed,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        stringResource(
                            R.string.recruiting_action_official_visit,
                            g.officialVisit.pointCost,
                        ),
                    )
                }
            }
            val hint = blockReasonHint(g)
            if (hint != null) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun blockReasonHint(gates: RecruitingProspectActionGates): String? {
    val reason = listOf(
        gates.dm,
        gates.phoneCall,
        gates.sendTheHouse,
        gates.bonusPoints,
        gates.scholarshipOffer,
        gates.scoutTheirGame,
        gates.officialVisit,
    ).firstNotNullOfOrNull { it.blockReason }
    return when (reason) {
        RecruitingActionBlockReason.WindowClosed -> stringResource(R.string.recruiting_window_closed)
        RecruitingActionBlockReason.AlreadyUsedThisSeason -> stringResource(R.string.recruiting_used_this_season)
        RecruitingActionBlockReason.ReservationActiveThisWeek ->
            stringResource(R.string.recruiting_reservation_active)
        RecruitingActionBlockReason.InsufficientPoints -> stringResource(R.string.recruiting_insufficient_points)
        null -> null
    }
}

@Composable
private fun SignLikelihoodChip(badge: SignLikelihoodBadge) {
    val label = when (badge) {
        SignLikelihoodBadge.Likely -> stringResource(R.string.recruiting_badge_likely)
        SignLikelihoodBadge.Possible -> stringResource(R.string.recruiting_badge_possible)
        SignLikelihoodBadge.Reach -> stringResource(R.string.recruiting_badge_reach)
        SignLikelihoodBadge.LongShot -> stringResource(R.string.recruiting_badge_long_shot)
    }
    val colors = when (badge) {
        SignLikelihoodBadge.Likely -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
        SignLikelihoodBadge.Possible -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
        SignLikelihoodBadge.Reach -> AssistChipDefaults.assistChipColors()
        SignLikelihoodBadge.LongShot -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
    AssistChip(onClick = {}, label = { Text(label) }, colors = colors)
}

@Composable
private fun recruitingHubTabLabel(tab: RecruitingHubTab): String = when (tab) {
    RecruitingHubTab.Recommended -> stringResource(R.string.recruiting_tab_recommended)
    RecruitingHubTab.AllHsJuco -> stringResource(R.string.recruiting_tab_all_hs_juco)
    RecruitingHubTab.HighSchool -> stringResource(R.string.recruiting_tab_high_school)
    RecruitingHubTab.JuniorCollege -> stringResource(R.string.recruiting_tab_junior_college)
    RecruitingHubTab.TransferPortal -> stringResource(R.string.recruiting_tab_portal)
    RecruitingHubTab.BoardTargets -> stringResource(R.string.recruiting_tab_board)
    RecruitingHubTab.Committed -> stringResource(R.string.recruiting_tab_committed)
    RecruitingHubTab.Lost -> stringResource(R.string.recruiting_tab_lost)
}

package dev.novux.dynasty.cbb.ui.offseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.core.calendar.CalendarWeekLabel
import dev.novux.dynasty.cbb.core.calendar.OffseasonWeekHeading
import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear
import dev.novux.dynasty.cbb.core.dashboard.OffseasonAdvanceOptionalTaskAggregator
import dev.novux.dynasty.cbb.core.dashboard.OffseasonDashboardContext
import dev.novux.dynasty.cbb.core.dashboard.DashboardAdvanceGate
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.dashboard.OffseasonOptionalDashboardTasks
import dev.novux.dynasty.cbb.core.dashboard.PortalPipelineEntrantDashboardPolicy
import dev.novux.dynasty.cbb.core.dashboard.OffseasonPortalOptionalTasksPolicy
import dev.novux.dynasty.cbb.core.dashboard.OffseasonWeekSevenOptionalDashboardTasks
import dev.novux.dynasty.cbb.core.dashboard.OffseasonWeekNineOptionalDashboardTasks
import dev.novux.dynasty.cbb.core.offseason.weeknine.ProgramEconomyState
import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutReport
import dev.novux.dynasty.cbb.core.dashboard.PipelineEntrantSpotlight
import dev.novux.dynasty.cbb.core.dashboard.DashboardTask
import dev.novux.dynasty.cbb.core.dashboard.DashboardTaskDismissal
import dev.novux.dynasty.cbb.core.portal.PortalCyclePolicy
import dev.novux.dynasty.cbb.core.dashboard.OffseasonConferenceOffersRequiredDashboardTasks
import dev.novux.dynasty.cbb.core.dashboard.OffseasonRequiredDashboardTasks
import dev.novux.dynasty.cbb.core.dashboard.OffseasonRosterLockRequiredDashboardTasks
import dev.novux.dynasty.cbb.data.offseason.ConferenceOffersWeekState
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.offseason.weekeight.OffseasonRosterLockConstants
import dev.novux.dynasty.cbb.core.offseason.weekeight.RosterLockPolicy
import dev.novux.dynasty.cbb.core.offseason.weekseven.SummerWorkoutConstants
import dev.novux.dynasty.cbb.core.offseason.weeknine.OffseasonWeekNineConstants
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonConferenceOffersPolicy
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonWeekAdvancePolicy
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonWeekTenConstants
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekEightRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekTenRepository
import dev.novux.dynasty.cbb.core.signingday.SigningDayConstants
import dev.novux.dynasty.cbb.data.offseason.OffseasonSummerWorkoutRepository
import dev.novux.dynasty.cbb.core.signingday.SigningDayProgress
import dev.novux.dynasty.cbb.data.signingday.SigningDayRepository
import dev.novux.dynasty.cbb.ui.dashboard.DashboardRequiredTasksPanel
import dev.novux.dynasty.cbb.ui.dashboard.SigningDayRequiredTasksSkeleton
import dev.novux.dynasty.cbb.core.portal.PortalConstants
import dev.novux.dynasty.cbb.core.portal.PortalEntrant
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekTwoRepository
import dev.novux.dynasty.cbb.data.portal.PortalCycleRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession
import dev.novux.dynasty.cbb.data.save.SaveSlotRepository
import dev.novux.dynasty.cbb.ui.dashboard.DashboardOptionalTasksPanel
import dev.novux.dynasty.cbb.ui.dashboard.DashboardNotificationFeedPanel
import dev.novux.dynasty.cbb.ui.dashboard.CareerBlockNotificationDialog
import dev.novux.dynasty.cbb.core.notification.NotificationFeedItem
import dev.novux.dynasty.cbb.data.notification.DashboardNotificationSnapshotLoader
import dev.novux.dynasty.cbb.data.notification.DashboardNotificationSnapshotSidecarLoader
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellChrome
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellTab
import dev.novux.dynasty.cbb.ui.dynasty.rememberDynastyShellSelectedTab
import dev.novux.dynasty.cbb.ui.hubs.DynastyHubMenuCallbacks
import dev.novux.dynasty.cbb.ui.hubs.buildDynastyHubMenu
import dev.novux.dynasty.cbb.ui.hubs.notifyRecruitingCycleClosed
import dev.novux.dynasty.cbb.data.calendar.UserCalendarRepository
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellCalendarTab
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellNewsTab
import dev.novux.dynasty.cbb.ui.dynasty.rememberShellNewsFeed
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellRosterTab
import dev.novux.dynasty.cbb.ui.offseason.leaving.DraftResultsWeekTwoPanel
import dev.novux.dynasty.cbb.ui.offseason.leaving.PlayersLeavingPanel
import dev.novux.dynasty.cbb.ui.offseason.leaving.WeekOneNilRetentionHost
import dev.novux.dynasty.cbb.ui.offseason.leaving.WeekOneLeavingNavigation
import dev.novux.dynasty.cbb.ui.offseason.leaving.weekOneNavigationFor
import dev.novux.dynasty.cbb.core.advance.SimWeekConfirmation
import dev.novux.dynasty.cbb.ui.advance.SimWeekAdvanceConfirmationUiPerf
import dev.novux.dynasty.cbb.ui.advance.SimWeekAdvanceDialog
import dev.novux.dynasty.cbb.ui.advance.SimWeekAdvanceUiPerf
import dev.novux.dynasty.cbb.ui.advance.applyWeekAdvanceResult
import dev.novux.dynasty.cbb.ui.advance.showWeekAdvanceConfirmationUnavailable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.ui.player.PlayerCardPreviewSheet
import dev.novux.dynasty.cbb.ui.player.PlayerCardScreen
import kotlinx.coroutines.launch


/** Offseason shell — week header and advance ([planning/offseason.md]). */
@Composable
fun OffseasonScreen(
    initial: LoadedDynasty,
    saveSlotRepository: SaveSlotRepository,
    rosterRepository: DynastyRosterRepository,
    recruitingEconomyRepository: RecruitingEconomyRepository,
    portalCycleRepository: PortalCycleRepository,
    signingDayRepository: SigningDayRepository,
    offseasonWeekTwoRepository: OffseasonWeekTwoRepository,
    summerWorkoutRepository: OffseasonSummerWorkoutRepository,
    weekEightRepository: OffseasonWeekEightRepository,
    weekNineRepository: OffseasonWeekNineRepository,
    weekTenRepository: OffseasonWeekTenRepository,
    hubMenuCallbacks: DynastyHubMenuCallbacks,
    onOpenRecruiting: (RecruitingHubTab) -> Unit = {},
    onOpenSigningDay: (DashboardDeepLink) -> Unit = {},
    userCalendarRepository: UserCalendarRepository,
    showNotificationFeed: Boolean = true,
) {
    var loaded by remember(initial) { mutableStateOf(initial) }
    var ledger by remember { mutableStateOf<RecruitingCycleLedger?>(null) }
    var portalTop40Locked by remember { mutableStateOf(false) }
    var portalEntrants by remember { mutableStateOf<List<PortalEntrant>>(emptyList()) }
    var portalPipelineSpotlight by remember { mutableStateOf<PipelineEntrantSpotlight?>(null) }
    var summerWorkoutReport by remember { mutableStateOf<SummerWorkoutReport?>(null) }
    var summerWorkoutSheetOpen by remember { mutableStateOf(false) }
    var weekNineEconomy by remember { mutableStateOf<ProgramEconomyState?>(null) }
    var fundraisingSheetOpen by remember { mutableStateOf(false) }
    var openScholarshipSlots by remember { mutableStateOf(0) }
    var rosterScholarshipCount by remember { mutableStateOf(0) }
    var dismissedTaskKeys by remember { mutableStateOf(setOf<String>()) }
    var pendingAdvanceConfirmation by remember { mutableStateOf<SimWeekConfirmation?>(null) }
    var weekAdvanceInProgress by remember { mutableStateOf(false) }
    var dialogAdvanceInProgress by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var signingProgress by remember { mutableStateOf<SigningDayProgress?>(null) }
    var conferenceOffersResolved by remember { mutableStateOf(true) }
    var conferenceOffersState by remember { mutableStateOf<ConferenceOffersWeekState?>(null) }
    var weekOnePlayers by remember { mutableStateOf<List<RosterPlayer>>(emptyList()) }
    var weekOneLeavingNav by remember { mutableStateOf<WeekOneLeavingNavigation?>(null) }
    var selectedTab by rememberDynastyShellSelectedTab()
    var notificationFeed by remember { mutableStateOf<List<NotificationFeedItem>>(emptyList()) }
    var careerModal by remember { mutableStateOf<NotificationFeedItem?>(null) }
    var fullRoster by remember { mutableStateOf<List<DynastyPlayer>>(emptyList()) }
    var cardPlayer by remember { mutableStateOf<DynastyPlayer?>(null) }
    var fullScreenPlayer by remember { mutableStateOf<DynastyPlayer?>(null) }
    val context = LocalContext.current
    val app = context.requireDynastyApplication()
    val weekOneNilRetentionHost = remember(loaded.dynastyId, loaded.metadata.seasonIndex) {
        WeekOneNilRetentionHost(
            loaded = loaded,
            nilRules = app.balanceRulesetRepository.activeRuleset().nil,
            nilBudgetRepository = app.nilBudgetRepository,
            nilAnnualPoolRepository = app.nilAnnualPoolRepository,
        )
    }
    val playerStatsRepository = app.playerStatsRepository
    val newsFeed = rememberShellNewsFeed(
        dynastyId = loaded.dynastyId,
        seasonIndex = loaded.metadata.seasonIndex,
        weekIndex = loaded.weekCheckpoint.activePosition.weekIndex,
        newsFeedRepository = app.newsFeedRepository,
        rosterRepository = rosterRepository,
    )
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val remove = ActiveDynastySession.addListener {
            ActiveDynastySession.current?.let { session -> loaded = session }
        }
        onDispose(remove)
    }

    val notificationFeedRepository = context.requireDynastyApplication().notificationFeedRepository
    val coachEmploymentRepository = context.requireDynastyApplication().coachEmploymentRepository
    val coachSkillPointRepository = context.requireDynastyApplication().coachSkillPointRepository
    val scheduleRepository = context.requireDynastyApplication().preseasonScheduleRepository

    LaunchedEffect(
        loaded.dynastyId,
        loaded.metadata.seasonIndex,
        loaded.weekCheckpoint,
        showNotificationFeed,
    ) {
        if (showNotificationFeed) {
            val userSchool = app.schoolHubRepository.loadUserSchoolOverview(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
            )
            val standings = app.leagueWeekBatchRepository.loadStandings(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
            )
            val sidecar = DashboardNotificationSnapshotSidecarLoader.load(
                loaded = loaded,
                rosterRepository = rosterRepository,
                disciplineRepository = app.disciplineRepository,
                coachingCarouselRepository = app.coachingCarouselRepository,
                weekNineRepository = weekNineRepository,
                userTeamId = userSchool?.teamId,
                selectionSundayPublished = standings?.selectionSunday != null,
                loadStandingsSelection = { standings?.selectionSunday },
            )
            val snapshot = DashboardNotificationSnapshotLoader.snapshotFor(
                loaded = loaded,
                rosterRepository = rosterRepository,
                coachEmploymentRepository = coachEmploymentRepository,
                coachSkillPointRepository = coachSkillPointRepository,
                scheduleRepository = scheduleRepository,
                signingDayRepository = signingDayRepository,
                sidecar = sidecar,
            )
            notificationFeed = notificationFeedRepository.refreshFromSnapshot(loaded.dynastyId, snapshot)
            careerModal = notificationFeedRepository.pendingCareerModal(loaded.dynastyId)
        } else {
            notificationFeed = emptyList()
            careerModal = null
        }
    }

    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex) {
        dismissedTaskKeys = app.dashboardOptionalDismissRepository.load(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
    }

    val onDismissOptionalTask: (String) -> Unit = { key ->
        dismissedTaskKeys = dismissedTaskKeys + key
        scope.launch {
            dismissedTaskKeys = app.dashboardOptionalDismissRepository.recordDismiss(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
                key,
            )
        }
    }

    LaunchedEffect(loaded.dynastyId, loaded.weekCheckpoint) {
        signingProgress = signingDayRepository.loadProgress(loaded.dynastyId, loaded.metadata.seasonIndex)
        ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
        val portal = portalCycleRepository.loadOrSimulate(loaded.dynastyId, loaded.metadata.seasonIndex)
        portalTop40Locked = portal.top40Locked
        portalEntrants = portal.entrants
        val packIndex = app.packRepository.activeSession().index
        val userTeamId = app.database.dynastyCoachDao().getByDynastyId(loaded.dynastyId)?.userTeamId
        val pipelineStates = buildSet {
            userTeamId?.let { id ->
                packIndex.team(id)?.homeState?.takeIf { it.isNotBlank() }?.let(::add)
            }
        }
        portalPipelineSpotlight = PortalPipelineEntrantDashboardPolicy.spotlight(
            packIndex,
            portalEntrants,
            pipelineStates.ifEmpty { setOf("OH") },
        )
        val roster = rosterRepository.listPlayers(loaded.dynastyId)
        fullRoster = rosterRepository.listDynastyPlayers(loaded.dynastyId)
        val scholarships = RosterLockPolicy.scholarshipCount(roster)
        rosterScholarshipCount = scholarships
        openScholarshipSlots =
            (OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP - scholarships).coerceAtLeast(0)
        if (loaded.weekCheckpoint.activePosition.phase == CalendarPhase.Offseason &&
            loaded.weekCheckpoint.activePosition.weekIndex == OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX
        ) {
            val weekTenState = weekTenRepository.loadState(loaded.dynastyId, loaded.metadata.seasonIndex)
            conferenceOffersState = weekTenState
            conferenceOffersResolved = OffseasonConferenceOffersRequiredDashboardTasks.canAdvanceWeek(
                weekTenState.offers,
                weekTenState.decisions,
                loaded.metadata.seasonIndex,
            )
        } else {
            conferenceOffersState = null
        }
        if (loaded.weekCheckpoint.activePosition.phase == CalendarPhase.Offseason &&
            loaded.weekCheckpoint.activePosition.weekIndex == 1
        ) {
            weekOnePlayers = rosterRepository.prepareWeekOneLeaving(loaded.dynastyId)
        }
        if (loaded.weekCheckpoint.activePosition.phase == CalendarPhase.Offseason &&
            loaded.weekCheckpoint.activePosition.weekIndex == SummerWorkoutConstants.OFFSEASON_WEEK_INDEX
        ) {
            summerWorkoutReport = summerWorkoutRepository.ensureSimulated(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
            )
        } else {
            summerWorkoutReport = null
        }
        if (loaded.weekCheckpoint.activePosition.phase == CalendarPhase.Offseason &&
            loaded.weekCheckpoint.activePosition.weekIndex == OffseasonWeekNineConstants.OFFSEASON_WEEK_INDEX
        ) {
            weekNineEconomy = weekNineRepository.loadEconomy(loaded.dynastyId, loaded.metadata.seasonIndex)
        } else {
            weekNineEconomy = null
        }
    }
    val position = loaded.weekCheckpoint.activePosition
    val inOffseason = position.phase == CalendarPhase.Offseason
    val weekHeader = CalendarWeekLabel.display(position)
    val themeLine = if (inOffseason) {
        OffseasonWeekHeading.themeLine(position.weekIndex)
    } else {
        null
    }

    val schoolName = loaded.summary.schoolDisplayName.orEmpty()
    val offseasonWeekForCycle = position.weekIndex.takeIf { inOffseason }
    val recruitingCycleClosed = RecruitingCyclePolicy.recruitingMenuTreatsCycleClosed(
        position.phase,
        offseasonWeekForCycle,
        ledger,
    )
    val menu = buildDynastyHubMenu(
        position = position,
        callbacks = hubMenuCallbacks,
        recruitingCycleClosed = recruitingCycleClosed,
        onRecruitingCycleClosed = { notifyRecruitingCycleClosed(context) },
    )

    PlayerCardPreviewSheet(
        player = cardPlayer,
        onDismiss = { cardPlayer = null },
        onViewMore = { player ->
            cardPlayer = null
            fullScreenPlayer = player
        },
        allowEdit = false,
    )

    fullScreenPlayer?.let { player ->
        PlayerCardScreen(
            player = player,
            schoolDisplayName = schoolName,
            seasonRecordLabel = "0-0",
            seasonIndex = loaded.metadata.seasonIndex,
            playerStatsRepository = playerStatsRepository,
            allowEdit = false,
            onBack = { fullScreenPlayer = null },
        )
        return
    }

    careerModal?.let { modal ->
        CareerBlockNotificationDialog(
            item = modal,
            onAcknowledge = {
                scope.launch {
                    notificationFeedRepository.acknowledgeCareerModal(loaded.dynastyId, modal.eventKey)
                    careerModal = notificationFeedRepository.pendingCareerModal(loaded.dynastyId)
                }
            },
            onOpenDestination = { hubMenuCallbacks.onExitHome() },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    DynastyShellChrome(
        schoolDisplayName = schoolName,
        phaseWeekLabel = weekHeader,
        headerSubtitle = themeLine,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        menuActions = menu,
    ) { padding ->
        when (selectedTab) {
            DynastyShellTab.Roster -> DynastyShellRosterTab(
                players = fullRoster,
                contentPadding = padding,
                onPlayerClick = { cardPlayer = it },
            )
            DynastyShellTab.Calendar -> DynastyShellCalendarTab(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                activePosition = position,
                calendarRepository = userCalendarRepository,
                contentPadding = padding,
            )
            DynastyShellTab.News -> DynastyShellNewsTab(
                feed = newsFeed,
                contentPadding = padding,
                onOpenNewsHub = hubMenuCallbacks.onOpenNewsHub,
            )
            DynastyShellTab.Dashboard -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
        Text(
            text = stringResource(
                R.string.offseason_shell_season,
                SeasonMembershipYear.label(loaded.metadata.seasonIndex),
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showNotificationFeed) {
            DashboardNotificationFeedPanel(
                feed = notificationFeed,
                onDeepLink = onOpenRecruiting,
                onStrategyOpen = hubMenuCallbacks.onOpenStrategy,
                onCoachCardOpen = hubMenuCallbacks.onOpenSchool,
                onSigningDayOpen = { onOpenSigningDay(DashboardDeepLink.SigningDayRankings) },
                onMarchMayhemOpen = hubMenuCallbacks.onOpenMarchMayhem,
                onFundraisingOpen = { fundraisingSheetOpen = true },
            )
        }
        val offseasonRequiredContext = OffseasonRequiredDashboardTasks.WeekContext(
            weekIndex = position.weekIndex,
            seasonIndex = loaded.metadata.seasonIndex,
            weekOnePlayers = weekOnePlayers,
            signingProgress = signingProgress,
            rosterScholarshipCount = rosterScholarshipCount,
            conferenceOffers = conferenceOffersState?.offers.orEmpty(),
            conferenceDecisions = conferenceOffersState?.decisions.orEmpty(),
        )
        val offseasonRequiredTasks = if (inOffseason) {
            OffseasonRequiredDashboardTasks.build(offseasonRequiredContext)
        } else {
            emptyList()
        }
        if (inOffseason && position.weekIndex == 1) {
            DashboardRequiredTasksPanel(
                tasks = offseasonRequiredTasks,
                onOpenSigningDay = { link ->
                    weekOneNavigationFor(link)?.let { weekOneLeavingNav = it }
                },
            )
            PlayersLeavingPanel(
                dynastyId = loaded.dynastyId,
                rosterRepository = rosterRepository,
                modifier = Modifier.weight(1f),
                onPlayersChanged = { weekOnePlayers = it },
                navigationRequest = weekOneLeavingNav,
                onNavigationRequestConsumed = { weekOneLeavingNav = null },
                nilRetentionHost = weekOneNilRetentionHost,
            )
        } else if (inOffseason && position.weekIndex == 2) {
            DraftResultsWeekTwoPanel(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                weekTwoRepository = offseasonWeekTwoRepository,
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == OffseasonRosterLockConstants.OFFSEASON_WEEK_INDEX) {
            if (offseasonRequiredTasks.isNotEmpty()) {
                DashboardRequiredTasksPanel(
                    tasks = offseasonRequiredTasks,
                    onOpenSigningDay = {},
                )
            }
            RosterCutsWeekEightPanel(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                rosterRepository = rosterRepository,
                weekEightRepository = weekEightRepository,
                onRosterChanged = {
                    scope.launch {
                        val roster = rosterRepository.listPlayers(loaded.dynastyId)
                        val scholarships = RosterLockPolicy.scholarshipCount(roster)
                        rosterScholarshipCount = scholarships
                        openScholarshipSlots =
                            (OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP - scholarships).coerceAtLeast(0)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == OffseasonWeekNineConstants.OFFSEASON_WEEK_INDEX) {
            FundraisingWeekNinePanel(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                weekNineRepository = weekNineRepository,
                economyState = weekNineEconomy,
                onEconomyLoaded = { weekNineEconomy = it },
                onOpenFullHub = { fundraisingSheetOpen = true },
                compactSummary = true,
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX) {
            if (offseasonRequiredTasks.isNotEmpty()) {
                DashboardRequiredTasksPanel(
                    tasks = offseasonRequiredTasks,
                    onOpenSigningDay = {},
                )
            }
            ConferenceOffersWeekTenPanel(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                weekTenRepository = weekTenRepository,
                onDecisionsChanged = {
                    scope.launch {
                        val weekTenState = weekTenRepository.loadState(
                            loaded.dynastyId,
                            loaded.metadata.seasonIndex,
                        )
                        conferenceOffersState = weekTenState
                        conferenceOffersResolved = OffseasonConferenceOffersRequiredDashboardTasks.canAdvanceWeek(
                            weekTenState.offers,
                            weekTenState.decisions,
                            loaded.metadata.seasonIndex,
                        )
                    }
                },
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == SummerWorkoutConstants.OFFSEASON_WEEK_INDEX) {
            SummerWorkoutReportPanel(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                summerWorkoutRepository = summerWorkoutRepository,
                report = summerWorkoutReport,
                onOpenFullReport = { summerWorkoutSheetOpen = true },
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == SigningDayConstants.OFFSEASON_WEEK_INDEX) {
            Text(
                text = stringResource(R.string.offseason_signing_day_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == 3) {
            Text(
                text = stringResource(R.string.offseason_dashboard_week3_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == 4) {
            Text(
                text = stringResource(R.string.offseason_dashboard_week4_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        } else if (inOffseason && position.weekIndex == 5) {
            Text(
                text = stringResource(R.string.offseason_dashboard_week5_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        } else {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.offseason_shell_chrome_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.offseason_ai_recruit_batch_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.offseason_shell_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (inOffseason && position.weekIndex == SigningDayConstants.OFFSEASON_WEEK_INDEX) {
            when (signingProgress) {
                null -> SigningDayRequiredTasksSkeleton()
                else -> DashboardRequiredTasksPanel(
                    tasks = offseasonRequiredTasks,
                    onOpenSigningDay = onOpenSigningDay,
                )
            }
        }

        val taskDismissKey: (DashboardTask) -> String = { task ->
            DashboardTaskDismissal.key(position, task)
        }
        val offseasonWeek = position.weekIndex.takeIf { inOffseason }
        val ledgerCycleClosed = ledger?.cycleClosed == true
        if (RecruitingCyclePolicy.hubNavigable(position.phase, offseasonWeek, ledgerCycleClosed)) {
            OutlinedButton(
                onClick = { onOpenRecruiting(RecruitingHubTab.Recommended) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.recruiting_open_hub))
            }
        } else if (
            inOffseason &&
            position.weekIndex == 1 &&
            RecruitingCyclePolicy.hubAccessible(position.phase, offseasonWeek) &&
            !RecruitingCyclePolicy.hubNavigable(position.phase, offseasonWeek, ledgerCycleClosed)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.offseason_week_one_recruiting_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.offseason_week_one_recruiting_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (
            RecruitingCyclePolicy.hubAccessible(position.phase, offseasonWeek) &&
            recruitingCycleClosed
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.recruiting_administration_only_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.recruiting_cycle_closed_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (inOffseason && position.weekIndex == SummerWorkoutConstants.OFFSEASON_WEEK_INDEX) {
            val weekSevenTasks = OffseasonWeekSevenOptionalDashboardTasks.build(
                offseasonWeekIndex = position.weekIndex,
                reportAvailable = summerWorkoutReport != null,
                coachTrainingGrade = summerWorkoutReport?.coachTrainingGrade,
                playerCount = summerWorkoutReport?.lines?.size,
            )
            if (weekSevenTasks.isNotEmpty()) {
                DashboardOptionalTasksPanel(
                    tasks = weekSevenTasks,
                    dismissedKeys = dismissedTaskKeys,
                    onDismiss = onDismissOptionalTask,
                    taskDismissKey = taskDismissKey,
                    onDeepLink = onOpenRecruiting,
                    onSummerWorkoutReportOpen = { summerWorkoutSheetOpen = true },
                )
            }
        }

        if (inOffseason && position.weekIndex == OffseasonWeekNineConstants.OFFSEASON_WEEK_INDEX) {
            val weekNineTasks = OffseasonWeekNineOptionalDashboardTasks.build(
                offseasonWeekIndex = position.weekIndex,
                economy = weekNineEconomy,
            )
            if (weekNineTasks.isNotEmpty()) {
                DashboardOptionalTasksPanel(
                    tasks = weekNineTasks,
                    dismissedKeys = dismissedTaskKeys,
                    onDismiss = onDismissOptionalTask,
                    taskDismissKey = taskDismissKey,
                    onDeepLink = onOpenRecruiting,
                    onFundraisingWeekNineOpen = { fundraisingSheetOpen = true },
                )
            }
        }

        if (inOffseason && OffseasonPortalOptionalTasksPolicy.inPortalTaskWindow(position.weekIndex)) {
            Text(
                text = stringResource(R.string.dashboard_offseason_portal_tasks_cadence_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val dashboardTasks = buildOffseasonPortalOptionalTasks(
                weekIndex = position.weekIndex,
                ledger = ledger,
                portalTop40Locked = portalTop40Locked,
                openScholarshipSlots = openScholarshipSlots,
                pipelineSpotlight = portalPipelineSpotlight,
            )
            DashboardOptionalTasksPanel(
                tasks = dashboardTasks,
                dismissedKeys = dismissedTaskKeys,
                onDismiss = onDismissOptionalTask,
                taskDismissKey = taskDismissKey,
                onDeepLink = onOpenRecruiting,
                headingRes = R.string.dashboard_offseason_portal_optional_heading,
            )
        } else if (inOffseason && PortalCyclePolicy.portalWindowClosed(position.weekIndex)) {
            Text(
                text = stringResource(R.string.dashboard_offseason_portal_window_closed_heading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.dashboard_offseason_portal_window_closed_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        pendingAdvanceConfirmation?.let { confirmation ->
            SimWeekAdvanceDialog(
                confirmation = confirmation,
                isAdvancing = dialogAdvanceInProgress,
                onConfirm = {
                    scope.launch {
                        dialogAdvanceInProgress = true
                        try {
                            val result = SimWeekAdvanceUiPerf.measure {
                                saveSlotRepository.advanceDynastyWeek(loaded.slotIndex)
                            }
                            applyWeekAdvanceResult(
                                result,
                                snackbarHostState,
                                context,
                            ) { updated ->
                                loaded = updated
                                ActiveDynastySession.set(updated)
                                pendingAdvanceConfirmation = null
                            }
                        } finally {
                            dialogAdvanceInProgress = false
                        }
                    }
                },
                onDismiss = {
                    if (!dialogAdvanceInProgress) {
                        pendingAdvanceConfirmation = null
                    }
                },
            )
        }

        Button(
            onClick = {
                if (weekAdvanceInProgress || dialogAdvanceInProgress) return@Button
                scope.launch {
                    val signing = signingProgress
                        ?: signingDayRepository.loadProgress(loaded.dynastyId, loaded.metadata.seasonIndex)
                    signingProgress = signing
                    val requiredTasks = if (inOffseason) {
                        OffseasonRequiredDashboardTasks.build(
                            OffseasonRequiredDashboardTasks.WeekContext(
                                weekIndex = position.weekIndex,
                                seasonIndex = loaded.metadata.seasonIndex,
                                weekOnePlayers = weekOnePlayers,
                                signingProgress = signing,
                                rosterScholarshipCount = RosterLockPolicy.scholarshipCount(
                                    rosterRepository.listPlayers(loaded.dynastyId),
                                ),
                                conferenceOffers = conferenceOffersState?.offers.orEmpty(),
                                conferenceDecisions = conferenceOffersState?.decisions.orEmpty(),
                            ),
                        )
                    } else {
                        emptyList()
                    }
                    if (DashboardAdvanceGate.blocksWeekAdvance(requiredTasks)) {
                        return@launch
                    }
                    if (inOffseason &&
                        position.weekIndex == SigningDayConstants.OFFSEASON_WEEK_INDEX &&
                        !signing.canAdvanceOffseasonWeek()
                    ) {
                        return@launch
                    }
                    val dashboardTasks = if (inOffseason) {
                        OffseasonAdvanceOptionalTaskAggregator.collect(
                            offseasonWeekIndex = position.weekIndex,
                            ledger = ledger,
                            portalTop40Locked = portalTop40Locked,
                            openScholarshipSlots = openScholarshipSlots,
                            pipelineSpotlight = portalPipelineSpotlight,
                            summerWorkoutReport = summerWorkoutReport,
                            weekNineEconomy = weekNineEconomy,
                        )
                    } else {
                        emptyList()
                    }
                    weekAdvanceInProgress = true
                    try {
                        val dismissKey = { task: DashboardTask ->
                            DashboardTaskDismissal.key(position, task)
                        }
                        val unresolved = OffseasonAdvanceOptionalTaskAggregator.unresolvedCount(
                            dashboardTasks,
                            dismissedTaskKeys,
                            dismissKey,
                        )
                        val confirmation = SimWeekAdvanceConfirmationUiPerf.measure {
                            saveSlotRepository.buildSimWeekAdvanceConfirmation(
                                loaded.slotIndex,
                                unresolvedOptionalTaskCount = unresolved,
                            )
                        }
                        if (confirmation == null) {
                            showWeekAdvanceConfirmationUnavailable(
                                snackbarHostState,
                                context,
                            )
                        } else if (confirmation.needsDialog) {
                            pendingAdvanceConfirmation = confirmation
                        } else {
                            val result = SimWeekAdvanceUiPerf.measure {
                                saveSlotRepository.advanceDynastyWeek(loaded.slotIndex)
                            }
                            applyWeekAdvanceResult(
                                result,
                                snackbarHostState,
                                context,
                            ) { updated ->
                                loaded = updated
                                ActiveDynastySession.set(updated)
                            }
                        }
                    } finally {
                        weekAdvanceInProgress = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !weekAdvanceInProgress &&
                !dialogAdvanceInProgress &&
                inOffseason && when (position.weekIndex) {
                SigningDayConstants.OFFSEASON_WEEK_INDEX ->
                    signingProgress?.canAdvanceOffseasonWeek() == true
                OffseasonRosterLockConstants.OFFSEASON_WEEK_INDEX ->
                    OffseasonRosterLockRequiredDashboardTasks.canAdvanceWeek(rosterScholarshipCount)
                OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX -> conferenceOffersResolved
                1 -> !DashboardAdvanceGate.blocksWeekAdvance(offseasonRequiredTasks)
                else -> true
            },
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (weekAdvanceInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    if (weekAdvanceInProgress) {
                        stringResource(R.string.dynasty_shell_advance_week_in_progress)
                    } else if (inOffseason && OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta(position)) {
                        stringResource(R.string.offseason_advance_to_preseason)
                    } else {
                        stringResource(R.string.offseason_advance_week)
                    },
                )
            }
        }
        if (inOffseason && OffseasonWeekAdvancePolicy.showsAdvanceToPreseasonCta(position)) {
            Text(
                text = stringResource(
                    if (OffseasonConferenceOffersPolicy.skipConferenceOffersWeek(loaded.metadata.seasonIndex)) {
                        R.string.offseason_advance_to_preseason_season_one
                    } else {
                        R.string.offseason_advance_to_preseason_after_conf
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

            }
        }
    }
        summerWorkoutReport?.takeIf { summerWorkoutSheetOpen }?.let { report ->
            SummerWorkoutReportBottomSheet(
                report = report,
                onDismiss = { summerWorkoutSheetOpen = false },
            )
        }

        if (fundraisingSheetOpen) {
            FundraisingWeekNineBottomSheet(
                dynastyId = loaded.dynastyId,
                seasonIndex = loaded.metadata.seasonIndex,
                weekNineRepository = weekNineRepository,
                programPrestige = RecruitingEconomyRepository.DEFAULT_PROGRAM_PRESTIGE,
                onDismiss = { fundraisingSheetOpen = false },
                onEconomyLoaded = { weekNineEconomy = it },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

private fun buildOffseasonPortalOptionalTasks(
    weekIndex: Int,
    ledger: RecruitingCycleLedger?,
    portalTop40Locked: Boolean,
    openScholarshipSlots: Int,
    pipelineSpotlight: PipelineEntrantSpotlight?,
) = OffseasonOptionalDashboardTasks.build(
    OffseasonDashboardContext(
        offseasonWeekIndex = weekIndex,
        recruitingPointsAvailable = ledger?.pointsAvailable ?: 0,
        portalTop40Locked = portalTop40Locked,
        openScholarshipSlots = openScholarshipSlots,
        signingCrunchActive = OffseasonPortalOptionalTasksPolicy.signingCrunchActive(weekIndex),
        pipelineSpotlight = pipelineSpotlight,
    ),
)

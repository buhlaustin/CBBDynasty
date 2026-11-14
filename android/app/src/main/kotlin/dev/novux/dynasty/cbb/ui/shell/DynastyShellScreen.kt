package dev.novux.dynasty.cbb.ui.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.calendar.CalendarWeekLabel
import dev.novux.dynasty.cbb.core.calendar.ConferenceTournamentCalendar
import dev.novux.dynasty.cbb.core.advance.SimWeekConfirmation
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentStatus
import dev.novux.dynasty.cbb.core.dashboard.DashboardAdvanceGate
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingRecommendedParser
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.dashboard.DashboardTaskGate
import dev.novux.dynasty.cbb.core.dashboard.DashboardTask
import dev.novux.dynasty.cbb.core.dashboard.DashboardTaskDismissal
import dev.novux.dynasty.cbb.core.dashboard.InSeasonRecruitingDashboardContext
import dev.novux.dynasty.cbb.core.dashboard.InSeasonRecruitingNudgeResolver
import dev.novux.dynasty.cbb.core.dashboard.RecruitingTop60MovementPolicy
import dev.novux.dynasty.cbb.core.dashboard.InSeasonWeekGames
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.core.recruiting.SchoolRecruitingContext
import dev.novux.dynasty.cbb.core.signingday.NationalRecruitingRankings
import dev.novux.dynasty.cbb.core.notification.NotificationCategoryMutePolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedItem
import dev.novux.dynasty.cbb.data.notification.DashboardNotificationSnapshotLoader
import dev.novux.dynasty.cbb.data.notification.DashboardNotificationSnapshotSidecarLoader
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession
import dev.novux.dynasty.cbb.data.save.SaveSlotRepository
import dev.novux.dynasty.cbb.ui.advance.SimWeekAdvanceConfirmationUiPerf
import dev.novux.dynasty.cbb.ui.advance.SimWeekAdvanceDialog
import dev.novux.dynasty.cbb.ui.advance.SimWeekAdvanceUiPerf
import dev.novux.dynasty.cbb.ui.advance.applyWeekAdvanceResult
import dev.novux.dynasty.cbb.ui.advance.showWeekAdvanceConfirmationUnavailable
import dev.novux.dynasty.cbb.core.schedule.PreseasonSchedulePlan
import dev.novux.dynasty.cbb.core.discipline.PendingDisciplineIncident
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.core.injuries.MedicalRedshirtPolicy
import dev.novux.dynasty.cbb.core.coach.card.AssistantCoachCard
import dev.novux.dynasty.cbb.core.coach.achievements.HeadCoachAchievements
import dev.novux.dynasty.cbb.core.coach.card.CoachCardUserGate
import dev.novux.dynasty.cbb.core.coach.card.HeadCoachCard
import dev.novux.dynasty.cbb.core.coach.security.CoachSecuritySnapshot
import dev.novux.dynasty.cbb.core.coach.staff.SchoolStaffRoster
import dev.novux.dynasty.cbb.core.preseason.FirstPlayableGame
import dev.novux.dynasty.cbb.core.preview.DashboardGamePreviewCadencePolicy
import dev.novux.dynasty.cbb.core.preview.GamePreviewEngine
import dev.novux.dynasty.cbb.core.preview.GamePreviewRequest
import dev.novux.dynasty.cbb.core.preview.GamePreviewVenue
import dev.novux.dynasty.cbb.core.preview.GamePreviewScoutingBuilder
import dev.novux.dynasty.cbb.core.preview.HeadToHeadHistory
import dev.novux.dynasty.cbb.core.preview.HeadToHeadHistoryResolver
import dev.novux.dynasty.cbb.core.preview.TeamPreviewSide
import dev.novux.dynasty.cbb.ui.preview.GamePreviewFromCalendar
import dev.novux.dynasty.cbb.ui.preview.GamePreviewLaunch
import dev.novux.dynasty.cbb.core.preseason.PreseasonSeasonStartPolicy
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.draft.NationalDraftTop60Pool
import dev.novux.dynasty.cbb.core.player.draft.withNationalTop60DraftRank
import dev.novux.dynasty.cbb.core.preseason.SeasonStartFocus
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview
import dev.novux.dynasty.cbb.data.coach.CoachSecurityRepository
import dev.novux.dynasty.cbb.data.coach.CoachSkillPointRepository
import dev.novux.dynasty.cbb.data.coach.SchoolStaffRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import dev.novux.dynasty.cbb.data.chemistry.ChemistryRepository
import dev.novux.dynasty.cbb.data.discipline.DisciplineRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.core.dashboard.DashboardPostseasonSeedResolver
import dev.novux.dynasty.cbb.ui.dashboard.DashboardLastGameCard
import dev.novux.dynasty.cbb.ui.dashboard.DashboardRequiredTasksPanel
import dev.novux.dynasty.cbb.core.dashboard.DashboardLastGameSummary
import dev.novux.dynasty.cbb.core.dashboard.DashboardLastUserGameResolver
import dev.novux.dynasty.cbb.ui.dashboard.DashboardTeamOverviewCard
import dev.novux.dynasty.cbb.data.school.SchoolHubRepository
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.ui.dashboard.CareerBlockNotificationDialog
import dev.novux.dynasty.cbb.ui.dashboard.DashboardNotificationFeedPanel
import dev.novux.dynasty.cbb.ui.dashboard.DashboardOptionalTasksPanel
import dev.novux.dynasty.cbb.data.coach.CoachEmploymentRepository
import dev.novux.dynasty.cbb.data.notification.NotificationFeedRepository
import dev.novux.dynasty.cbb.ui.player.PlayerCardPreviewSheet
import dev.novux.dynasty.cbb.ui.player.PlayerCardScreen
import dev.novux.dynasty.cbb.ui.player.PlayerInjuryRosterRow
import dev.novux.dynasty.cbb.ui.player.PlayerLoyaltyChips
import dev.novux.dynasty.cbb.ui.postgame.PostgameInjuryFlow
import dev.novux.dynasty.cbb.ui.coach.AssistantCoachCardScreen
import dev.novux.dynasty.cbb.ui.coach.CoachPreviewBottomSheet
import dev.novux.dynasty.cbb.ui.discipline.DisciplineDecisionBottomSheet
import dev.novux.dynasty.cbb.ui.discipline.rememberDisciplineDecisionSheetState
import dev.novux.dynasty.cbb.ui.coach.CoachPreviewTarget
import dev.novux.dynasty.cbb.ui.coach.CoachCardRoute
import dev.novux.dynasty.cbb.ui.coach.CoachCardTab
import dev.novux.dynasty.cbb.ui.coach.HeadCoachCardScreen
import dev.novux.dynasty.cbb.ui.coach.SchoolStaffTabPanel
import dev.novux.dynasty.cbb.ui.coach.StaffManageBottomSheet
import dev.novux.dynasty.cbb.ui.coach.StaffManageTarget
import dev.novux.dynasty.cbb.ui.coach.rememberStaffManageSheetState
import dev.novux.dynasty.cbb.data.coach.StaffManageResult
import dev.novux.dynasty.cbb.ui.coach.rememberCoachPreviewSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import dev.novux.dynasty.cbb.ui.school.SchoolHubScreen
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellChrome
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellMenuAction
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellTab
import dev.novux.dynasty.cbb.ui.dynasty.rememberDynastyShellSelectedTab
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.shell.InSeasonShellMenuExtrasPolicy
import dev.novux.dynasty.cbb.ui.hubs.DynastyHubMenuCallbacks
import dev.novux.dynasty.cbb.ui.hubs.buildDynastyHubMenu
import dev.novux.dynasty.cbb.ui.hubs.notifyRecruitingCycleClosed
import dev.novux.dynasty.cbb.data.calendar.UserCalendarRepository
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellCalendarTab
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellNewsTab
import dev.novux.dynasty.cbb.core.news.ShallowNewsFeedSnapshot
import dev.novux.dynasty.cbb.data.news.NewsFeedRepository
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellRosterTab
import kotlinx.coroutines.launch

private sealed class ShellCoachView {
    data object Hub : ShellCoachView()
    data object Staff : ShellCoachView()
    data class School(val overview: SchoolHubOverview) : ShellCoachView()
    data class HeadCoach(
        val card: HeadCoachCard,
        val route: CoachCardRoute.HeadCoach,
    ) : ShellCoachView()

    data class Assistant(
        val card: AssistantCoachCard,
        val route: CoachCardRoute.Assistant,
    ) : ShellCoachView()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynastyShellScreen(
    initial: LoadedDynasty,
    saveSlotRepository: SaveSlotRepository,
    scheduleRepository: PreseasonScheduleRepository,
    staffRepository: SchoolStaffRepository,
    schoolHubRepository: SchoolHubRepository,
    coachSecurityRepository: CoachSecurityRepository,
    coachSkillPointRepository: CoachSkillPointRepository,
    rosterRepository: DynastyRosterRepository,
    disciplineRepository: DisciplineRepository,
    chemistryRepository: ChemistryRepository,
    userCalendarRepository: UserCalendarRepository,
    newsFeedRepository: NewsFeedRepository,
    notificationFeedRepository: NotificationFeedRepository,
    coachEmploymentRepository: CoachEmploymentRepository,
    dynastyAutomationRepository: dev.novux.dynasty.cbb.data.settings.DynastyAutomationRepository,
    showNotificationFeed: Boolean = true,
    mutedNotificationCategories: Set<dev.novux.dynasty.cbb.core.notification.NotificationFeedCategory> = emptySet(),
    hubMenuCallbacks: DynastyHubMenuCallbacks,
    onOpenDiscipline: (PendingDisciplineIncident) -> Unit,
    onOpenGamePreview: () -> Unit = {},
    onOpenGameWatch: () -> Unit = {},
    onOpenSigningDay: (dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink) -> Unit = {},
    onOpenRecruitingTab: (RecruitingHubTab, String?) -> Unit = { _, _ -> },
    onOpenAutomation: () -> Unit = {},
    onOpenJobCenter: () -> Unit = {},
    postgameInjuryRefreshKey: Int = 0,
    disciplineDashboardRefreshKey: Int = 0,
) {
    var loaded by remember(initial) { mutableStateOf(initial) }
    var pendingAdvanceConfirmation by remember { mutableStateOf<SimWeekConfirmation?>(null) }
    var weekAdvanceInProgress by remember { mutableStateOf(false) }
    var dialogAdvanceInProgress by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var focus by remember { mutableStateOf<SeasonStartFocus?>(null) }
    var staffRoster by remember { mutableStateOf<SchoolStaffRoster?>(null) }
    var coachSecurity by remember { mutableStateOf<CoachSecuritySnapshot?>(null) }
    var userSchool by remember { mutableStateOf<SchoolHubOverview?>(null) }
    var dashboardPostseasonSeed by remember { mutableStateOf<String?>(null) }
    var selectionSundayPublished by remember { mutableStateOf(false) }
    var coachView by remember { mutableStateOf<ShellCoachView>(ShellCoachView.Hub) }
    var headCoachTabWire by rememberSaveable { mutableStateOf(CoachCardTab.Overview.wire) }
    val headCoachCardRoute = CoachCardRoute.HeadCoach(
        dynastyId = loaded.dynastyId,
        selectedTabWire = headCoachTabWire,
    )
    var assistantCoachRoutes by remember { mutableStateOf(mapOf<String, CoachCardRoute.Assistant>()) }
    var rosterPreview by remember { mutableStateOf<List<DynastyPlayer>>(emptyList()) }
    var cardPlayer by remember { mutableStateOf<DynastyPlayer?>(null) }
    var fullScreenPlayer by remember { mutableStateOf<DynastyPlayer?>(null) }
    var rosterPlayers by remember { mutableStateOf<List<RosterPlayer>>(emptyList()) }
    var postgameEvents by remember { mutableStateOf<List<GameSimInjuryEvent>>(emptyList()) }
    var dismissedOptionalTaskKeys by remember { mutableStateOf(setOf<String>()) }
    var disciplinePending by remember { mutableStateOf<PendingDisciplineIncident?>(null) }
    var disciplineSheetTarget by remember { mutableStateOf<PendingDisciplineIncident?>(null) }
    val disciplineSheetState = rememberDisciplineDecisionSheetState()
    var teamChemistry by remember { mutableStateOf<Int?>(null) }
    var schedulePlan by remember { mutableStateOf<PreseasonSchedulePlan?>(null) }
    var exhibitionCount by remember { mutableStateOf(0) }
    var selectedTab by rememberDynastyShellSelectedTab()
    var fullRoster by remember { mutableStateOf<List<DynastyPlayer>>(emptyList()) }
    var newsFeed by remember { mutableStateOf(ShallowNewsFeedSnapshot.Empty) }
    var coachSkillPointBank by remember { mutableIntStateOf(0) }
    var coachSkillState by remember {
        mutableStateOf(dev.novux.dynasty.cbb.core.coach.skill.CoachSkillPointState())
    }
    var notificationFeed by remember { mutableStateOf<List<NotificationFeedItem>>(emptyList()) }
    var lastGameSummary by remember { mutableStateOf<DashboardLastGameSummary?>(null) }
    var careerModal by remember { mutableStateOf<NotificationFeedItem?>(null) }
    var coachPreviewTarget by remember { mutableStateOf<CoachPreviewTarget?>(null) }
    var coachEmploymentStatus by remember { mutableStateOf(CoachEmploymentStatus.Employed) }
    var coachAchievements by remember { mutableStateOf<HeadCoachAchievements?>(null) }
    var coachDeepTabs by remember {
        mutableStateOf<dev.novux.dynasty.cbb.core.coach.card.deep.CoachCardDeepTabsSnapshot?>(null)
    }
    var headCoachExtensionOffer by remember {
        mutableStateOf<dev.novux.dynasty.cbb.core.coach.contract.HeadCoachExtensionOffer?>(null)
    }
    var recruitingLedger by remember { mutableStateOf<RecruitingCycleLedger?>(null) }
    var recruitingDashContext by remember { mutableStateOf<InSeasonRecruitingDashboardContext?>(null) }
    val coachPreviewSheetState = rememberCoachPreviewSheetState()
    var staffManageTarget by remember { mutableStateOf<StaffManageTarget?>(null) }
    val staffManageSheetState = rememberStaffManageSheetState()
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val remove = ActiveDynastySession.addListener {
            ActiveDynastySession.current?.let { session -> loaded = session }
        }
        onDispose(remove)
    }

    val refreshStaffRoster: () -> Unit = {
        scope.launch {
            val record = staffRoster?.headCoach?.seasonRecordLabel ?: "0-0"
            staffRoster = staffRepository.loadStaffRoster(loaded.dynastyId, record)
        }
    }

    LaunchedEffect(staffManageTarget) {
        if (staffManageTarget != null) {
            staffManageSheetState.show()
        }
    }
    val recruitingEconomyRepository = context.requireDynastyApplication().recruitingEconomyRepository
    val medicalRules = remember { BalanceMedicalRedshirtRules() }
    val position = loaded.weekCheckpoint.activePosition
    val dashboardTasks = DashboardTaskGate.inSeason(
        position = position,
        plan = schedulePlan,
        exhibitionCount = exhibitionCount,
        weekSimStaged = loaded.weekCheckpoint.provisionalGames.isNotEmpty(),
        disciplinePending = disciplinePending,
        rosterPlayers = rosterPlayers,
        medicalRules = medicalRules,
        coachSkillPointBank = coachSkillPointBank,
        recruiting = recruitingDashContext,
    )
    val optionalTaskDismissKey: (DashboardTask) -> String = { task ->
        DashboardTaskDismissal.key(position, task)
    }

    LaunchedEffect(loaded.dynastyId) {
        coachEmploymentStatus = coachEmploymentRepository.loadState(loaded.dynastyId).status
    }

    // disciplinePendingLoadEffect — [DisciplineDashboardFirstPaint.SHELL_DEDICATED_EFFECT_MARKER]
    LaunchedEffect(
        loaded.dynastyId,
        loaded.metadata.seasonIndex,
        loaded.weekCheckpoint,
        postgameInjuryRefreshKey,
        disciplineDashboardRefreshKey,
    ) {
        disciplinePending = disciplineRepository.loadState(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        ).pending
    }

    val incomingClassSeedRepository = context.requireDynastyApplication().incomingClassSeedRepository
    // recruitingLedgerDashContextLoadEffect — [RecruitingDashboardFirstPaint.SHELL_LEDGER_CONTEXT_EFFECT_MARKER]
    LaunchedEffect(
        loaded.dynastyId,
        loaded.metadata.seasonIndex,
        loaded.weekCheckpoint,
        postgameInjuryRefreshKey,
        loaded.balanceRuleset,
        userSchool,
    ) {
        val ledger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
        recruitingLedger = ledger
        val active = loaded.weekCheckpoint.activePosition
        val offseasonWeek = active.weekIndex.takeIf { active.phase == CalendarPhase.Offseason }
        val hubOpen = RecruitingCyclePolicy.hubNavigable(active.phase, offseasonWeek, ledger.cycleClosed)
        val recruitingNudges = if (hubOpen) {
            val dynastyApp = context.requireDynastyApplication()
            val recommended = BalanceRecruitingRecommendedParser.parse(loaded.balanceRuleset.recruiting)
            val pool = incomingClassSeedRepository.ensurePool(loaded.dynastyId, loaded.metadata.seasonIndex)
            val spendDelta = recruitingEconomyRepository.loadInterestSpendDeltaByProspect(loaded)
            val prestige = recruitingEconomyRepository.resolveUserProgramPrestige(loaded)
            val homeState = "OH"
            val school = SchoolRecruitingContext(
                programPrestige = prestige,
                schoolHomeState = homeState,
                nilPoolAnnualUsd = 0,
            )
            val top60 = NationalRecruitingRankings.top60FromPool(pool)
            val watchlistIds = dynastyApp.recruitingWatchlistRepository.load(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
            )
            val previousTop60Ranks = dynastyApp.recruitingWatchlistTop60RankRepository.load(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
            )
            val currentRankById = top60.associate { it.prospect.prospectId to it.rank }
            val offerProspects = dynastyApp.recruitingReservationRepository.loadActive(loaded.dynastyId)
                .filter { it.action == dev.novux.dynasty.cbb.core.recruiting.RecruitingRefundableAction.ScholarshipOffer }
                .map { it.prospectId }
                .toSet()
            val nudges = InSeasonRecruitingNudgeResolver.resolve(
                pool = pool,
                watchlistIds = watchlistIds,
                school = school,
                userProgramId = "user-${loaded.dynastyId}",
                recommended = recommended,
                spendDeltaByProspect = spendDelta,
                top60 = top60,
                scholarshipOfferProspectIds = offerProspects,
                previousTop60Ranks = previousTop60Ranks,
            )
            dynastyApp.recruitingWatchlistTop60RankRepository.save(
                loaded.dynastyId,
                loaded.metadata.seasonIndex,
                RecruitingTop60MovementPolicy.snapshotAfterObserve(watchlistIds, currentRankById),
            )
            nudges
        } else {
            emptyList()
        }
        recruitingDashContext = InSeasonRecruitingDashboardContext(
            phase = active.phase,
            offseasonWeekIndex = offseasonWeek,
            pointsAvailable = ledger.pointsAvailable,
            cycleClosed = RecruitingCyclePolicy.effectiveCycleClosed(
                active.phase,
                offseasonWeek,
                ledger.cycleClosed,
            ),
            recruitingNudges = recruitingNudges,
        )
    }

    // shellDashboardHydrationEffect — [DisciplineDashboardFirstPaint.SHELL_HYDRATION_EFFECT_MARKER] ([MEGA-FRAME-104])
    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex, loaded.weekCheckpoint, showNotificationFeed) {
        disciplinePending = disciplineRepository.loadState(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        ).pending
        teamChemistry = chemistryRepository.loadState(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        ).teamChemistry
        exhibitionCount = scheduleRepository.loadExhibitionCount(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        schedulePlan = scheduleRepository.loadPlan(loaded.dynastyId, loaded.metadata.seasonIndex)
        focus = PreseasonSeasonStartPolicy.resolve(
            loaded.metadata.seasonIndex,
            exhibitionCount,
            schedulePlan,
        )
        staffRoster = staffRepository.loadStaffRoster(loaded.dynastyId)
        coachAchievements = context.requireDynastyApplication().coachAchievementsRepository.snapshot(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        userSchool = schoolHubRepository.loadUserSchoolOverview(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        val app = context.requireDynastyApplication()
        val standings = app.leagueWeekBatchRepository.loadStandings(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        selectionSundayPublished = standings?.selectionSunday != null
        dashboardPostseasonSeed = userSchool?.let { school ->
            DashboardPostseasonSeedResolver.label(school.teamId, standings?.selectionSunday)
        }
        val contractState = app.headCoachUserContractRepository.load(loaded.dynastyId)
        coachDeepTabs = app.coachCardDeepTabsRepository.loadDeepTabs(
            dynastyId = loaded.dynastyId,
            seasonIndex = loaded.metadata.seasonIndex,
            schoolDisplayName = userSchool?.displayName
                ?: staffRoster?.headCoach?.schoolDisplayName
                ?: "",
            currentRecordLabel = staffRoster?.headCoach?.seasonRecordLabel ?: "0-0",
        )
        coachSecurity = coachSecurityRepository.loadForUserDynasty(loaded.dynastyId)
        headCoachExtensionOffer = staffRoster?.headCoach?.let { hc ->
            coachSecurity?.let { security ->
                dev.novux.dynasty.cbb.core.coach.contract.HeadCoachExtensionOfferEngine.incoming(
                    card = hc,
                    security = security,
                    dismissedForSeasonIndex = contractState.declinedExtensionSeasonIndex,
                    currentSeasonIndex = loaded.metadata.seasonIndex,
                )
            }
        }
        rosterPlayers = rosterRepository.listPlayers(loaded.dynastyId)
        fullRoster = rosterRepository.listDynastyPlayers(loaded.dynastyId)
        rosterPreview = fullRoster.take(6)
        postgameEvents = rosterRepository.peekPostgameInjuries(loaded.dynastyId)
        newsFeed = newsFeedRepository.shallowFeedSnapshot(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
            loaded.weekCheckpoint.activePosition.weekIndex,
            rosterPlayers,
        )
        val archetype = staffRoster?.headCoach?.archetype
            ?: dev.novux.dynasty.cbb.core.coach.CoachArchetype.Balanced
        val skillState = coachSkillPointRepository.ensureSundayGrant(
            loaded.dynastyId,
            loaded.weekCheckpoint.activePosition,
            archetype,
        )
        coachSkillState = skillState
        coachSkillPointBank = skillState.unspentBank
        val app = context.requireDynastyApplication()
        val savedLast = app.dashboardLastGameRepository.load(loaded.dynastyId, loaded.metadata.seasonIndex)
        val standingsForLast = app.leagueWeekBatchRepository.loadStandings(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
        val userTeamId = userSchool?.teamId
        val displaySchool = loaded.summary.schoolDisplayName.orEmpty().ifBlank { "Your program" }
        val teamName: (String) -> String = { teamId ->
            app.packRepository.teamDisplay(teamId)?.displayName ?: teamId
        }
        lastGameSummary = DashboardLastUserGameResolver.fromSnapshot(savedLast)
            ?: userTeamId?.let { teamId ->
                DashboardLastUserGameResolver.fromStandings(
                    userTeamId = teamId,
                    userTeamDisplayName = displaySchool,
                    standings = standingsForLast,
                    teamDisplayName = teamName,
                )
            }
        if (showNotificationFeed) {
            val archetype = staffRoster?.headCoach?.archetype
                ?: dev.novux.dynasty.cbb.core.coach.CoachArchetype.Balanced
            val dynastyApp = context.requireDynastyApplication()
            val sidecar = DashboardNotificationSnapshotSidecarLoader.load(
                loaded = loaded,
                rosterRepository = rosterRepository,
                disciplineRepository = disciplineRepository,
                coachingCarouselRepository = dynastyApp.coachingCarouselRepository,
                weekNineRepository = dynastyApp.offseasonWeekNineRepository,
                userTeamId = userSchool?.teamId,
                selectionSundayPublished = selectionSundayPublished,
                loadStandingsSelection = {
                    dynastyApp.leagueWeekBatchRepository.loadStandings(
                        loaded.dynastyId,
                        loaded.metadata.seasonIndex,
                    )?.selectionSunday
                },
                medicalRules = medicalRules,
            )
            val snapshot = DashboardNotificationSnapshotLoader.snapshotFor(
                loaded = loaded,
                rosterRepository = rosterRepository,
                coachEmploymentRepository = coachEmploymentRepository,
                coachSkillPointRepository = coachSkillPointRepository,
                scheduleRepository = scheduleRepository,
                signingDayRepository = dynastyApp.signingDayRepository,
                headCoachArchetype = archetype,
                sidecar = sidecar,
            )
            notificationFeed = notificationFeedRepository.refreshFromSnapshot(loaded.dynastyId, snapshot)
            careerModal = notificationFeedRepository.pendingCareerModal(loaded.dynastyId)
        } else {
            notificationFeed = emptyList()
            careerModal = null
        }
    }

    val nationalTop60RankByPlayerId = remember(
        fullRoster,
        loaded.dynastyId,
        loaded.metadata.seasonIndex,
    ) {
        NationalDraftTop60Pool.rankIndexForRoster(
            fullRoster,
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
    }

    PlayerCardPreviewSheet(
        player = cardPlayer?.withNationalTop60DraftRank(nationalTop60RankByPlayerId),
        onDismiss = { cardPlayer = null },
        onViewMore = { player ->
            cardPlayer = null
            fullScreenPlayer = player.withNationalTop60DraftRank(nationalTop60RankByPlayerId)
        },
        nationalTop60RankByPlayerId = nationalTop60RankByPlayerId,
    )

    fullScreenPlayer?.let { player ->
        PlayerCardScreen(
            player = player,
            schoolDisplayName = loaded.summary.schoolDisplayName.orEmpty(),
            seasonRecordLabel = staffRoster?.headCoach?.seasonRecordLabel ?: "0-0",
            seasonIndex = loaded.metadata.seasonIndex,
            playerStatsRepository = context.requireDynastyApplication().playerStatsRepository,
            allowEdit = true,
            schoolHubOverview = userSchool,
            onOpenSchoolHub = userSchool?.let { overview ->
                {
                    fullScreenPlayer = null
                    coachView = ShellCoachView.School(overview)
                }
            },
            onBack = { fullScreenPlayer = null },
            nationalTop60RankByPlayerId = nationalTop60RankByPlayerId,
        )
        return
    }
    PostgameInjuryFlow(
        events = postgameEvents,
        players = rosterPlayers,
        rsWeekIndex = loaded.weekCheckpoint.activePosition.weekIndex,
        medicalRules = medicalRules,
        countsMedicalRedshirt = loaded.weekCheckpoint.activePosition.phase != CalendarPhase.Exhibition,
        onDismiss = {
            scope.launch {
                if (postgameEvents.isNotEmpty()) {
                    val names = rosterPlayers.associate { it.playerId to it.displayName }
                    newsFeedRepository.appendUserInjuries(
                        loaded.dynastyId,
                        loaded.metadata.seasonIndex,
                        loaded.weekCheckpoint.activePosition.weekIndex,
                        names,
                        postgameEvents,
                    )
                    newsFeed = newsFeedRepository.shallowFeedSnapshot(
                        loaded.dynastyId,
                        loaded.metadata.seasonIndex,
                        loaded.weekCheckpoint.activePosition.weekIndex,
                        rosterPlayers,
                    )
                }
            }
            rosterRepository.clearPostgameInjuries(loaded.dynastyId)
            postgameEvents = emptyList()
        },
        onApplyMedicalRedshirt = { playerId ->
            scope.launch {
                rosterPlayers = rosterRepository.applyMedicalRedshirt(loaded.dynastyId, playerId)
                rosterRepository.clearPostgameInjuries(loaded.dynastyId)
                postgameEvents = emptyList()
                fullRoster = rosterRepository.listDynastyPlayers(loaded.dynastyId)
                rosterPreview = fullRoster.take(6)
            }
        },
    )

    val openHeadCoachFull: (HeadCoachCard) -> Unit = { card ->
        coachView = ShellCoachView.HeadCoach(
            card,
            headCoachCardRoute.withTab(CoachCardTab.Overview),
        )
    }
    val assistantRouteFor: (AssistantCoachCard) -> CoachCardRoute.Assistant = { card ->
        assistantCoachRoutes[card.staffId]
            ?: CoachCardRoute.Assistant(loaded.dynastyId, card.staffId)
    }
    val openAssistantFull: (AssistantCoachCard) -> Unit = { card ->
        coachView = ShellCoachView.Assistant(
            card,
            assistantRouteFor(card).withTab(CoachCardTab.Overview),
        )
    }
    val openHeadCoachPreview: (HeadCoachCard) -> Unit = { card ->
        coachPreviewTarget = CoachPreviewTarget.HeadCoach(card)
    }
    val openAssistantPreview: (AssistantCoachCard) -> Unit = { card ->
        coachPreviewTarget = CoachPreviewTarget.Assistant(card)
    }

    LaunchedEffect(coachPreviewTarget) {
        if (coachPreviewTarget != null) {
            coachPreviewSheetState.show()
        }
    }

    val refreshDisciplineAndRoster: suspend () -> Unit = {
        disciplinePending = disciplineRepository.loadState(loaded.dynastyId, loaded.metadata.seasonIndex).pending
        rosterPlayers = rosterRepository.listPlayers(loaded.dynastyId)
        fullRoster = rosterRepository.listDynastyPlayers(loaded.dynastyId)
    }

    LaunchedEffect(disciplineSheetTarget) {
        if (disciplineSheetTarget != null) {
            disciplineSheetState.show()
        }
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
            onOpenDestination = {
                when (modal.kind) {
                    dev.novux.dynasty.cbb.core.notification.NotificationEventKind.CareerFired ->
                        hubMenuCallbacks.onExitHome()
                    else -> staffRoster?.headCoach?.let { openHeadCoachPreview(it) }
                }
            },
        )
    }

    val previewCoachSecurity = coachPreviewTarget?.let { target ->
        when (target) {
            is CoachPreviewTarget.HeadCoach ->
                coachSecurity.takeIf { CoachCardUserGate.isUserHeadCoach(target.card, staffRoster) }
            else -> null
        }
    }
    CoachPreviewBottomSheet(
        target = coachPreviewTarget,
        userCoachSecurity = previewCoachSecurity,
        sheetState = coachPreviewSheetState,
        onDismiss = { coachPreviewTarget = null },
        onViewMore = { target ->
            when (target) {
                is CoachPreviewTarget.HeadCoach -> openHeadCoachFull(target.card)
                is CoachPreviewTarget.Assistant -> openAssistantFull(target.card)
            }
        },
    )

    DisciplineDecisionBottomSheet(
        incident = disciplineSheetTarget,
        dynastyId = loaded.dynastyId,
        seasonIndex = loaded.metadata.seasonIndex,
        disciplineRepository = disciplineRepository,
        sheetState = disciplineSheetState,
        onDismiss = { disciplineSheetTarget = null },
        onResolved = {
            scope.launch {
                refreshDisciplineAndRoster()
                disciplineSheetTarget = null
            }
        },
    )

  StaffManageBottomSheet(
        target = staffManageTarget,
        sheetState = staffManageSheetState,
        onDismiss = { staffManageTarget = null },
        onFire = { target ->
            scope.launch {
                when (staffRepository.fireStaff(loaded.dynastyId, target.staffId)) {
                    StaffManageResult.Success -> {
                        snackbarHostState.showSnackbar(context.getString(R.string.staff_manage_done))
                        refreshStaffRoster()
                    }
                    is StaffManageResult.Blocked -> {
                        val msg = if (target.isAssistant) {
                            context.getString(R.string.staff_manage_blocked_min_assistants)
                        } else {
                            context.getString(R.string.staff_manage_done)
                        }
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        },
        onHireFromPool = { target ->
            scope.launch {
                val record = staffRoster?.headCoach?.seasonRecordLabel ?: "0-0"
                when (staffRepository.hireFromPool(loaded.dynastyId, target.staffId, record)) {
                    StaffManageResult.Success -> {
                        snackbarHostState.showSnackbar(context.getString(R.string.staff_manage_done))
                        refreshStaffRoster()
                    }
                    is StaffManageResult.Blocked -> Unit
                }
            }
        },
        onPoach = { target ->
            scope.launch {
                val record = staffRoster?.headCoach?.seasonRecordLabel ?: "0-0"
                when (staffRepository.poachAssistant(loaded.dynastyId, target.staffId, record)) {
                    StaffManageResult.Success -> {
                        snackbarHostState.showSnackbar(context.getString(R.string.staff_manage_done))
                        refreshStaffRoster()
                    }
                    is StaffManageResult.Blocked -> Unit
                }
            }
        },
    )

    when (val view = coachView) {
        is ShellCoachView.School -> {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedButton(
                    onClick = { coachView = ShellCoachView.Hub },
                    modifier = Modifier.padding(16.dp),
                ) { Text(stringResource(R.string.dynasty_shell_back)) }
                SchoolHubScreen(
                    overview = view.overview,
                    teamChemistry = if (view.overview.isUserSchool) teamChemistry else null,
                    staffSection = if (view.overview.isUserSchool && staffRoster != null) {
                        {
                            SchoolStaffTabPanel(
                                roster = staffRoster!!,
                                adTrust = coachSecurity,
                                programPrestige = view.overview.programPrestige,
                                onHeadCoachClick = { openHeadCoachPreview(staffRoster!!.headCoach) },
                                onAssistantClick = { id ->
                                    staffRoster!!.assistants.firstOrNull { it.staffId == id }?.let {
                                        openAssistantPreview(it)
                                    }
                                },
                                userCanManage = true,
                                onManageStaff = { staffManageTarget = it },
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            return
        }
        is ShellCoachView.HeadCoach -> {
            val isUserHeadCoach = CoachCardUserGate.isUserHeadCoach(view.card, staffRoster)
            Column(modifier = Modifier.fillMaxSize()) {
                HeadCoachCardScreen(
                    card = view.card,
                    userCoachSecurity = coachSecurity,
                    isUserCoach = isUserHeadCoach,
                    onBack = { coachView = ShellCoachView.Staff },
                    allowEdit = isUserHeadCoach && coachSkillState != null,
                    onEdit = {},
                    skillPointBank = coachSkillPointBank,
                    skillState = coachSkillState,
                    programPrestige = userSchool?.programPrestige ?: 50,
                    seasonIndex = loaded.metadata.seasonIndex,
                    selectedTab = view.route.selectedTab,
                    onSelectedTabChange = { tab ->
                        headCoachTabWire = tab.wire
                        coachView = ShellCoachView.HeadCoach(
                            view.card,
                            headCoachCardRoute.withTab(tab),
                        )
                    },
                    onOpenStaffTab = { coachView = ShellCoachView.Staff },
                    employmentStatus = coachEmploymentStatus,
                    onVoluntaryResign = {
                        scope.launch {
                            coachEmploymentRepository.enterUnemployed(
                                loaded.dynastyId,
                                loaded.metadata.seasonIndex,
                            )
                            coachEmploymentStatus = CoachEmploymentStatus.Unemployed
                            onOpenJobCenter()
                        }
                    },
                    onOpenJobCenter = onOpenJobCenter,
                    schoolHubOverview = userSchool,
                    onOpenSchoolHub = userSchool?.let { overview ->
                        { coachView = ShellCoachView.School(overview) }
                    },
                    coachAchievements = coachAchievements,
                    coachDeepTabs = coachDeepTabs,
                    displayRatings = dev.novux.dynasty.cbb.core.coach.skill.HeadCoachSkillRatings.effective(
                        view.card.ratings,
                        coachSkillState,
                    ),
                    onSpendSkill = { branch ->
                        scope.launch {
                            val next = coachSkillPointRepository.spendOnBranch(loaded.dynastyId, branch)
                            coachSkillState = next
                            coachSkillPointBank = next.unspentBank
                        }
                    },
                    onAutoSpendChanged = { enabled ->
                        scope.launch {
                            val next = coachSkillPointRepository.setAutoSpend(
                                loaded.dynastyId,
                                enabled,
                                view.card.archetype,
                            )
                            val auto = dynastyAutomationRepository.load(loaded.dynastyId)
                            dynastyAutomationRepository.save(
                                loaded.dynastyId,
                                auto.copy(coachAutoSpend = enabled),
                            )
                            coachSkillState = next
                            coachSkillPointBank = next.unspentBank
                        }
                    },
                    extensionOffer = headCoachExtensionOffer,
                    onAcceptExtension = {
                        scope.launch {
                            val offer = headCoachExtensionOffer ?: return@launch
                            val appContext = context.requireDynastyApplication()
                            appContext.headCoachUserContractRepository.acceptExtension(loaded.dynastyId, offer)
                            headCoachExtensionOffer = null
                            val record = staffRoster?.headCoach?.seasonRecordLabel ?: "0-0"
                            staffRoster = staffRepository.loadStaffRoster(loaded.dynastyId, record)
                            staffRoster?.headCoach?.let { updated ->
                                coachView = ShellCoachView.HeadCoach(
                                    updated,
                                    headCoachCardRoute.withTab(view.route.selectedTab),
                                )
                            }
                        }
                    },
                    onDeclineExtension = {
                        scope.launch {
                            context.requireDynastyApplication().headCoachUserContractRepository.declineExtension(
                                loaded.dynastyId,
                                loaded.metadata.seasonIndex,
                            )
                            headCoachExtensionOffer = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            return
        }
        is ShellCoachView.Assistant -> {
            Column(modifier = Modifier.fillMaxSize()) {
                AssistantCoachCardScreen(
                    card = view.card,
                    onBack = { coachView = ShellCoachView.Staff },
                    seasonIndex = loaded.metadata.seasonIndex,
                    coachDeepTabs = if (view.card.hasHeadCoachCareer) coachDeepTabs else null,
                    selectedTab = view.route.selectedTab,
                    onSelectedTabChange = { tab ->
                        val nextRoute = view.route.withTab(tab)
                        assistantCoachRoutes = assistantCoachRoutes + (view.card.staffId to nextRoute)
                        coachView = ShellCoachView.Assistant(view.card, nextRoute)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            return
        }
        ShellCoachView.Staff -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
            ) {
                OutlinedButton(onClick = { coachView = ShellCoachView.Hub }) {
                    Text(stringResource(R.string.dynasty_shell_back))
                }
                staffRoster?.let { roster ->
                    SchoolStaffTabPanel(
                        roster = roster,
                        adTrust = coachSecurity,
                        programPrestige = userSchool?.programPrestige,
                        onHeadCoachClick = { openHeadCoachPreview(roster.headCoach) },
                        onAssistantClick = { id ->
                            roster.assistants.firstOrNull { it.staffId == id }?.let {
                                openAssistantPreview(it)
                            }
                        },
                        userCanManage = true,
                        onManageStaff = { staffManageTarget = it },
                    )
                }
            }
            return
        }
        ShellCoachView.Hub -> Unit
    }

    val schoolName = loaded.summary.schoolDisplayName.orEmpty()
    val leagueMaxConfWeeks = remember(loaded.dynastyId) {
        ConferenceTournamentCalendar.leagueMaxConfTourneyWeeks(
            context.requireDynastyApplication().packRepository.activeSession().index.source,
        )
    }
    val phaseLabel = CalendarWeekLabel.display(position, leagueMaxConfWeeks)
    val shellOffseasonWeek = position.weekIndex.takeIf { position.phase == CalendarPhase.Offseason }
    val recruitingCycleClosed = RecruitingCyclePolicy.recruitingMenuTreatsCycleClosed(
        position.phase,
        shellOffseasonWeek,
        recruitingLedger,
    )
    val shellMenu = buildDynastyHubMenu(
        position = position,
        callbacks = hubMenuCallbacks.copy(
            onOpenSchool = { userSchool?.let { coachView = ShellCoachView.School(it) } },
        ),
        selectionSundayPublished = selectionSundayPublished,
        recruitingCycleClosed = recruitingCycleClosed,
        onRecruitingCycleClosed = { notifyRecruitingCycleClosed(context) },
        leadingExtras = if (InSeasonShellMenuExtrasPolicy.includeStaffAndAutomation(position.phase)) {
            listOf(
                DynastyShellMenuAction(R.string.staff_open_tab) { coachView = ShellCoachView.Staff },
                DynastyShellMenuAction(R.string.shell_menu_automation, onOpenAutomation),
            )
        } else {
            emptyList()
        },
    )

    Box(modifier = Modifier.fillMaxSize()) {
    DynastyShellChrome(
        schoolDisplayName = schoolName,
        phaseWeekLabel = phaseLabel,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        menuActions = shellMenu,
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
                rosterPlayers = fullRoster.map { it.toRosterPlayer() },
                userTeamDisplayName = userSchool?.displayName.orEmpty(),
                onOpenGamePreview = { game ->
                    scope.launch {
                        val school = userSchool ?: return@launch
                        GamePreviewFromCalendar.launch(
                            context = context,
                            dynastyId = loaded.dynastyId,
                            seasonIndex = loaded.metadata.seasonIndex,
                            userSchool = school,
                            game = game,
                        )
                        onOpenGamePreview()
                    }
                },
                onReplayStagedGame = {
                    scope.launch {
                        val app = context.requireDynastyApplication()
                        if (app.userCalendarGameWatchBootstrap.configure(loaded)) {
                            onOpenGameWatch()
                        }
                    }
                },
            )
            DynastyShellTab.News -> DynastyShellNewsTab(
                feed = newsFeed,
                contentPadding = padding,
                onOpenNewsHub = hubMenuCallbacks.onOpenNewsHub,
                onOpenNewsHubPortal = hubMenuCallbacks.onOpenNewsHubPortal,
            )
            DynastyShellTab.Dashboard -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                userSchool?.let { overview ->
                    DashboardTeamOverviewCard(
                        overview = overview,
                        postseasonSeedLabel = dashboardPostseasonSeed,
                        onOpenRankings = hubMenuCallbacks.onOpenRankings,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_team_overview_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                lastGameSummary?.let { summary ->
                    DashboardLastGameCard(
                        summary = summary,
                        rosterPlayers = rosterPlayers,
                        onOpenTeamStats = hubMenuCallbacks.onOpenStatistics,
                        onOpenPlayerStats = hubMenuCallbacks.onOpenStatistics,
                    )
                }
                Text(
                    text = stringResource(R.string.dynasty_shell_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.dynasty_shell_roster_tab_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showNotificationFeed) {
                    val visibleNotificationFeed = remember(notificationFeed, mutedNotificationCategories) {
                        NotificationCategoryMutePolicy.filterVisible(
                            notificationFeed,
                            mutedNotificationCategories,
                        )
                    }
                    DashboardNotificationFeedPanel(
                        feed = visibleNotificationFeed,
                        onDeepLink = onOpenRecruitingTab,
                        onStrategyOpen = hubMenuCallbacks.onOpenStrategy,
                        onCoachCardOpen = { staffRoster?.headCoach?.let { openHeadCoachPreview(it) } },
                        onSigningDayOpen = onOpenSigningDay,
                        onGamePreviewOpen = {
                            val school = userSchool
                            if (school != null) {
                                scope.launch {
                                    ShellWeekGamePreviewLauncher.launchCurrentWeek(
                                        context = context,
                                        loaded = loaded,
                                        userSchool = school,
                                        schedulePlan = schedulePlan,
                                        exhibitionCount = exhibitionCount,
                                        onOpenGamePreview = onOpenGamePreview,
                                    )
                                }
                            }
                        },
                        onDisciplineOpen = {
                            disciplinePending?.let { disciplineSheetTarget = it }
                        },
                        onMarchMayhemOpen = hubMenuCallbacks.onOpenMarchMayhem,
                        onRankingsOpen = hubMenuCallbacks.onOpenRankings,
                        onHistoryOpen = hubMenuCallbacks.onOpenHistory,
                    )
                }
                DashboardRequiredTasksPanel(
                    tasks = dashboardTasks.required,
                    onOpenSigningDay = onOpenSigningDay,
                    onOpenDiscipline = { disciplinePending?.let { disciplineSheetTarget = it } },
                )
                DashboardOptionalTasksPanel(
                    tasks = dashboardTasks.optional,
                    dismissedKeys = dismissedOptionalTaskKeys,
                    onDismiss = { dismissedOptionalTaskKeys = dismissedOptionalTaskKeys + it },
                    taskDismissKey = optionalTaskDismissKey,
                    onDeepLink = onOpenRecruitingTab,
                    onStrategyOpen = hubMenuCallbacks.onOpenStrategy,
                    onCoachCardOpen = { staffRoster?.headCoach?.let { openHeadCoachPreview(it) } },
                    onReplayStagedGame = {
                        scope.launch {
                            val app = context.requireDynastyApplication()
                            if (app.userCalendarGameWatchBootstrap.configure(loaded)) {
                                onOpenGameWatch()
                            }
                        }
                    },
                )
                focus?.firstGame?.let { game ->
                    NextGameCard(
                        game = game,
                        seasonIndex = loaded.metadata.seasonIndex,
                        onViewOpponentSchool = {
                            val opponentId = game.opponentTeamId
                            if (!opponentId.startsWith("event-")) {
                                scope.launch {
                                    schoolHubRepository.loadOpponentOverview(
                                        loaded.dynastyId,
                                        opponentId,
                                        loaded.metadata.seasonIndex,
                                    )?.let { coachView = ShellCoachView.School(it) }
                                }
                            }
                        },
                        onOpenPreview = {
                            scope.launch {
                                val user = userSchool ?: return@launch
                                val oppOverview = if (!game.opponentTeamId.startsWith("event-")) {
                                    schoolHubRepository.loadOpponentOverview(
                                        loaded.dynastyId,
                                        game.opponentTeamId,
                                        loaded.metadata.seasonIndex,
                                    )
                                } else {
                                    null
                                }
                                val opponent = oppOverview?.let { TeamPreviewSide.fromOverview(it) }
                                    ?: TeamPreviewSide.fromPrestige(
                                        game.opponentTeamId,
                                        game.opponentDisplayName,
                                        58,
                                    )
                                val venue = if (game.homeGame) {
                                    GamePreviewVenue.Home
                                } else {
                                    GamePreviewVenue.Away
                                }
                                val app = context.requireDynastyApplication()
                                val userTeamId = user.teamId
                                val seasonIndex = loaded.metadata.seasonIndex
                                val seasonResultsByIndex = buildMap {
                                    for (season in 1..seasonIndex) {
                                        val st = app.leagueWeekBatchRepository.loadStandings(
                                            loaded.dynastyId,
                                            season,
                                        ) ?: continue
                                        if (st.seasonResults.isNotEmpty()) {
                                            put(season, st.seasonResults)
                                        }
                                    }
                                }
                                val meetings = HeadToHeadHistoryResolver.collectDynastyMeetings(
                                    userTeamId = userTeamId,
                                    opponentTeamId = game.opponentTeamId,
                                    seasonResultsByIndex = seasonResultsByIndex,
                                )
                                val currentStandings = app.leagueWeekBatchRepository.loadStandings(
                                    loaded.dynastyId,
                                    seasonIndex,
                                )
                                val userScouting = GamePreviewScoutingBuilder.fromOverview(user, currentStandings)
                                val opponentScouting = oppOverview?.let {
                                    GamePreviewScoutingBuilder.fromOverview(it, currentStandings)
                                } ?: GamePreviewScoutingBuilder.fromPrestige(
                                    game.opponentTeamId,
                                    game.opponentDisplayName,
                                    58,
                                )
                                val preview = GamePreviewEngine.predict(
                                    GamePreviewRequest(
                                        user = TeamPreviewSide.fromOverview(user),
                                        opponent = opponent,
                                        venue = venue,
                                        headToHead = HeadToHeadHistory.summarize(meetings),
                                    ),
                                ).copy(
                                    userScouting = userScouting,
                                    opponentScouting = opponentScouting,
                                )
                                val loc = if (game.homeGame) {
                                    context.getString(R.string.dynasty_shell_home)
                                } else {
                                    context.getString(R.string.dynasty_shell_away)
                                }
                                GamePreviewLaunch.matchupLabel = context.getString(
                                    R.string.dynasty_shell_matchup,
                                    game.opponentDisplayName,
                                    loc,
                                )
                                GamePreviewLaunch.userTeamName = user.displayName
                                GamePreviewLaunch.opponentName = game.opponentDisplayName
                                GamePreviewLaunch.venue = venue
                                GamePreviewLaunch.venuePhrase = if (game.homeGame) {
                                    context.getString(R.string.game_preview_venue_phrase_home)
                                } else {
                                    context.getString(R.string.game_preview_venue_phrase_away)
                                }
                                GamePreviewLaunch.result = preview
                                onOpenGamePreview()
                            }
                        },
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
                                        dismissedOptionalTaskKeys = emptySet()
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
                            if (DashboardAdvanceGate.blocksWeekAdvance(dashboardTasks.required)) {
                                return@launch
                            }
                            weekAdvanceInProgress = true
                            try {
                                val unresolved = dashboardTasks.optional.count {
                                    optionalTaskDismissKey(it) !in dismissedOptionalTaskKeys
                                }
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
                                        dismissedOptionalTaskKeys = emptySet()
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
                        !DashboardAdvanceGate.blocksWeekAdvance(dashboardTasks.required),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (weekAdvanceInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                        Text(
                            stringResource(
                                if (weekAdvanceInProgress) {
                                    R.string.dynasty_shell_advance_week_in_progress
                                } else {
                                    R.string.dynasty_shell_advance_week
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun NextGameCard(
    game: FirstPlayableGame,
    seasonIndex: Int,
    onViewOpponentSchool: () -> Unit,
    onOpenPreview: () -> Unit,
) {
    val locationLabel = if (game.homeGame) {
        stringResource(R.string.dynasty_shell_home)
    } else {
        stringResource(R.string.dynasty_shell_away)
    }
    val dateLabel = game.gameDateLabel(seasonIndex)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.dynasty_shell_next_game),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.dynasty_shell_matchup,
                    game.opponentDisplayName,
                    locationLabel,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (dateLabel.isNotBlank()) {
                Text(
                    text = stringResource(R.string.dynasty_shell_next_game_date, dateLabel, locationLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = stringResource(R.string.dynasty_shell_game_type, game.kind.name),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onViewOpponentSchool) {
                Text(stringResource(R.string.school_hub_view_opponent))
            }
            OutlinedButton(onClick = onOpenPreview, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.dynasty_shell_game_preview))
            }
            if (DashboardGamePreviewCadencePolicy.showPreviewHint(game)) {
                Text(
                    text = stringResource(R.string.dynasty_shell_game_preview_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

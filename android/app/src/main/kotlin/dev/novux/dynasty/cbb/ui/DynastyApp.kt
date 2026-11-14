package dev.novux.dynasty.cbb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.awards.YearlyAwardsSeasonArchive
import dev.novux.dynasty.cbb.core.balance.injuryTuning
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.settings.AppSettings
import dev.novux.dynasty.cbb.core.shell.LaunchResumeSkipStudioPolicy
import dev.novux.dynasty.cbb.core.advance.UserLeagueGameIds
import dev.novux.dynasty.cbb.core.dashboard.InSeasonWeekGames
import dev.novux.dynasty.cbb.core.strategy.StrategyEditLockPolicy
import dev.novux.dynasty.cbb.core.calendar.checkpoint.ProvisionalGameMark
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult
import dev.novux.dynasty.cbb.data.save.SaveSlotRepository
import dev.novux.dynasty.cbb.ui.shell.SaveLoadUnavailableDialog
import dev.novux.dynasty.cbb.data.settings.AppSettingsRepository
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.ui.navigation.AppRoute
import dev.novux.dynasty.cbb.ui.shell.CreditsScreen
import dev.novux.dynasty.cbb.ui.shell.HomeMenuScreen
import dev.novux.dynasty.cbb.ui.shell.DynastyLoadProgressOverlay
import dev.novux.dynasty.cbb.ui.shell.LoadGameScreen
import dev.novux.dynasty.cbb.ui.shell.DynastyAutomationScreen
import dev.novux.dynasty.cbb.ui.pack.IdentityPackImportScreen
import dev.novux.dynasty.cbb.ui.shell.SettingsScreen
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.novux.dynasty.cbb.ui.navigation.DynastyLaunch
import dev.novux.dynasty.cbb.ui.navigation.navigateToRecruitingHub
import dev.novux.dynasty.cbb.ui.navigation.popBackToInShellDashboard
import dev.novux.dynasty.cbb.ui.strategy.StrategyScreen
import dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch
import dev.novux.dynasty.cbb.ui.sim.GameWatchScreen
import dev.novux.dynasty.cbb.ui.preview.GamePreviewLaunch
import dev.novux.dynasty.cbb.ui.preview.GamePreviewScreen
import dev.novux.dynasty.cbb.ui.discipline.DisciplineDecisionScreen
import dev.novux.dynasty.cbb.ui.newgame.NewGameWizardScreen
import dev.novux.dynasty.cbb.ui.offseason.CoachingCarouselScreen
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubDefaults
import dev.novux.dynasty.cbb.core.news.NewsHubPortalAddToBoardPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import dev.novux.dynasty.cbb.ui.hubs.DynastyHubMenuCallbacks
import dev.novux.dynasty.cbb.ui.hubs.HubScaffoldScreen
import dev.novux.dynasty.cbb.ui.hubs.ConferenceTournamentHubRouteScreen
import dev.novux.dynasty.cbb.ui.hubs.MarchMayhemHubRouteScreen
import dev.novux.dynasty.cbb.ui.hubs.RankingsHubRouteScreen
import dev.novux.dynasty.cbb.ui.hubs.SchoolHubRouteScreen
import dev.novux.dynasty.cbb.ui.hubs.StatisticsHubRouteScreen
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.ui.recruiting.RecruitingHubScreen
import dev.novux.dynasty.cbb.ui.dynasty.DynastyCareerHost
import dev.novux.dynasty.cbb.ui.signingday.SigningDayInitialTab
import dev.novux.dynasty.cbb.ui.signingday.SigningDayScreen
import dev.novux.dynasty.cbb.ui.awards.AwardsHubScreen
import dev.novux.dynasty.cbb.ui.awards.ConferenceOption
import dev.novux.dynasty.cbb.ui.news.NewsHubScreen
import dev.novux.dynasty.cbb.ui.seasonreview.SeasonReviewScreen
import dev.novux.dynasty.cbb.core.shell.SplashBootstrapPhase
import dev.novux.dynasty.cbb.core.shell.SplashReadyGatePolicy
import dev.novux.dynasty.cbb.ui.shell.SplashScreen
import dev.novux.dynasty.cbb.ui.shell.StudioScreen
import dev.novux.dynasty.cbb.ui.theme.DynastyTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun DynastyApp(
    settingsRepository: AppSettingsRepository =
        LocalContext.current.requireDynastyApplication().appSettingsRepository,
    saveSlotRepository: SaveSlotRepository =
        LocalContext.current.requireDynastyApplication().saveSlotRepository,
    coachingCarouselRepository: dev.novux.dynasty.cbb.data.carousel.CoachingCarouselRepository =
        LocalContext.current.requireDynastyApplication().coachingCarouselRepository,
    recruitingEconomyRepository: dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository =
        LocalContext.current.requireDynastyApplication().recruitingEconomyRepository,
) {
    DynastyTheme {
        var launchSettings by remember { mutableStateOf<AppSettings?>(null) }
        val appSettings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
        val scope = rememberCoroutineScope()

        LaunchedEffect(settingsRepository) {
            launchSettings = settingsRepository.currentSettings()
        }

        if (launchSettings == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            )
        } else {
            var studioBeatConsumed by rememberSaveable {
                mutableStateOf(launchSettings!!.skipStudioIntro)
            }
            val startRoute = if (
                LaunchResumeSkipStudioPolicy.shouldBypassStudioBeat(
                    skipStudioIntroSetting = launchSettings!!.skipStudioIntro,
                    studioBeatConsumed = studioBeatConsumed,
                )
            ) {
                AppRoute.Splash.route
            } else {
                AppRoute.Studio.route
            }
            val navController = rememberNavController()
            val continueSlot by saveSlotRepository.observeContinueSlot()
                .collectAsState(initial = null)
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            var continueLoadFailedSlot by remember { mutableStateOf<Int?>(null) }
            var continueLoading by remember { mutableStateOf(false) }
            var splashPhase by remember { mutableStateOf(SplashBootstrapPhase.Loading) }
            var splashLoadAttempt by remember { mutableIntStateOf(0) }
            val dynastyApp = context.requireDynastyApplication()
            LaunchedEffect(splashLoadAttempt) {
                splashPhase = SplashBootstrapPhase.Loading
                try {
                    val session = withContext(Dispatchers.IO) {
                        saveSlotRepository.ensureInitialized()
                        dynastyApp.packRepository.ensureEmbeddedLoaded()
                    }
                    val ready = SplashReadyGatePolicy.isReady(
                        packTeamCount = session.index.allTeamDisplays().size,
                        saveSlotsInitialized = true,
                    )
                    splashPhase = if (ready) {
                        SplashBootstrapPhase.Ready
                    } else {
                        SplashBootstrapPhase.Error
                    }
                } catch (_: Exception) {
                    splashPhase = SplashBootstrapPhase.Error
                }
            }
            var recruitingLaunchTab by remember {
                mutableStateOf(RecruitingHubDefaults.defaultTab)
            }
            var recruitingFocusProspectId by remember { mutableStateOf<String?>(null) }
            var newsHubLaunchTab by remember {
                mutableStateOf(dev.novux.dynasty.cbb.ui.news.NewsHubTab.Stories)
            }
            var signingDayInitialTab by remember { mutableStateOf(SigningDayInitialTab.Rankings) }
            var disciplineIncident by remember {
                mutableStateOf<dev.novux.dynasty.cbb.core.discipline.PendingDisciplineIncident?>(null)
            }
            var postgameInjuryRefreshKey by remember { mutableIntStateOf(0) }
            var disciplineDashboardRefreshKey by remember { mutableIntStateOf(0) }
            var watchPostgameEvents by remember {
                mutableStateOf<List<dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent>>(emptyList())
            }
            var watchPostgameRoster by remember {
                mutableStateOf<List<dev.novux.dynasty.cbb.core.roster.RosterPlayer>>(emptyList())
            }
            val portalCycleRepository = LocalContext.current.requireDynastyApplication().portalCycleRepository
            val signingDayRepository = LocalContext.current.requireDynastyApplication().signingDayRepository
            val offseasonWeekTwoRepository =
                LocalContext.current.requireDynastyApplication().offseasonWeekTwoRepository
            val summerWorkoutRepository =
                LocalContext.current.requireDynastyApplication().summerWorkoutRepository
            val offseasonWeekEightRepository =
                LocalContext.current.requireDynastyApplication().offseasonWeekEightRepository
            val offseasonWeekNineRepository =
                LocalContext.current.requireDynastyApplication().offseasonWeekNineRepository
            val offseasonWeekTenRepository =
                LocalContext.current.requireDynastyApplication().offseasonWeekTenRepository
            val preseasonProgressRepository =
                LocalContext.current.requireDynastyApplication().preseasonProgressRepository
            val incomingClassSeedRepository =
                LocalContext.current.requireDynastyApplication().incomingClassSeedRepository
            val tournamentInvitesRepository =
                LocalContext.current.requireDynastyApplication().tournamentInvitesRepository
            val preseasonScheduleRepository =
                LocalContext.current.requireDynastyApplication().preseasonScheduleRepository
            val preseasonReportRepository =
                LocalContext.current.requireDynastyApplication().preseasonReportRepository
            val preseasonSeasonStartRepository =
                LocalContext.current.requireDynastyApplication().preseasonSeasonStartRepository
            val schoolStaffRepository =
                LocalContext.current.requireDynastyApplication().schoolStaffRepository
            val schoolHubRepository =
                LocalContext.current.requireDynastyApplication().schoolHubRepository
            val coachSecurityRepository =
                LocalContext.current.requireDynastyApplication().coachSecurityRepository
            val coachEmploymentRepository =
                LocalContext.current.requireDynastyApplication().coachEmploymentRepository

            val hubMenuCallbacks = DynastyHubMenuCallbacks(
                onOpenSchool = { navController.navigate(AppRoute.SchoolHub.route) },
                onOpenRecruiting = {
                    recruitingFocusProspectId = null
                    recruitingLaunchTab = RecruitingHubTab.Recommended
                    navController.navigateToRecruitingHub()
                },
                onOpenStrategy = { navController.navigate(AppRoute.Strategy.route) },
                onOpenStatistics = { navController.navigate(AppRoute.StatisticsHub.route) },
                onOpenRankings = { navController.navigate(AppRoute.RankingsHub.route) },
                onOpenNewsHub = {
                    newsHubLaunchTab = dev.novux.dynasty.cbb.ui.news.NewsHubTab.Stories
                    navController.navigate(AppRoute.NewsHub.route)
                },
                onOpenNewsHubPortal = {
                    newsHubLaunchTab = dev.novux.dynasty.cbb.ui.news.NewsHubTab.Portal
                    navController.navigate(AppRoute.NewsHub.route)
                },
                onOpenHistory = { navController.navigate(AppRoute.HistoryHub.route) },
                onOpenAwards = { navController.navigate(AppRoute.AwardsHub.route) },
                onOpenMarchMayhem = { navController.navigate(AppRoute.MarchMayhem.route) },
                onOpenConferenceTournament = { navController.navigate(AppRoute.ConferenceTournamentHub.route) },
                onExitHome = {
                    navController.popBackStack(AppRoute.Home.route, inclusive = false)
                },
            )

            suspend fun onDynastyReady(loaded: LoadedDynasty) {
                ActiveDynastySession.set(loaded)
                val app = context.requireDynastyApplication()
                val signedYear = withContext(Dispatchers.IO) {
                    app.packDynastyIdentityRepository.loadMembershipYearAtSign(loaded.dynastyId)
                }
                val activeYear = app.packRepository.activeSession().index.membershipYear
                if (
                    dev.novux.dynasty.cbb.core.pack.PackSwapMembershipWarnPolicy.isMismatch(
                        signedYear,
                        activeYear,
                    )
                ) {
                    snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.pack_import_membership_year_warning,
                            signedYear,
                            activeYear,
                        ),
                    )
                }
                val carousel = withContext(Dispatchers.IO) {
                    coachingCarouselRepository.loadOrCreate(
                        loaded.dynastyId,
                        loaded.metadata.seasonIndex,
                    )
                }
                val employment = withContext(Dispatchers.IO) {
                    coachEmploymentRepository.refreshOffersIfNeeded(
                        loaded.dynastyId,
                        loaded.metadata.seasonIndex,
                    )
                }
                DynastyLaunch.navigateForLoaded(navController, loaded, carousel, employment)
            }

            @Composable
            fun DynastyCareerRoute() {
                DynastyCareerHost(
                    saveSlotRepository = saveSlotRepository,
                    recruitingEconomyRepository = recruitingEconomyRepository,
                    portalCycleRepository = portalCycleRepository,
                    signingDayRepository = signingDayRepository,
                    offseasonWeekTwoRepository = offseasonWeekTwoRepository,
                    summerWorkoutRepository = summerWorkoutRepository,
                    offseasonWeekEightRepository = offseasonWeekEightRepository,
                    offseasonWeekNineRepository = offseasonWeekNineRepository,
                    offseasonWeekTenRepository = offseasonWeekTenRepository,
                    preseasonProgressRepository = preseasonProgressRepository,
                    incomingClassSeedRepository = incomingClassSeedRepository,
                    tournamentInvitesRepository = tournamentInvitesRepository,
                    preseasonScheduleRepository = preseasonScheduleRepository,
                    preseasonReportRepository = preseasonReportRepository,
                    preseasonSeasonStartRepository = preseasonSeasonStartRepository,
                    schoolStaffRepository = schoolStaffRepository,
                    schoolHubRepository = schoolHubRepository,
                    coachSecurityRepository = coachSecurityRepository,
                    coachEmploymentRepository = coachEmploymentRepository,
                    hubMenuCallbacks = hubMenuCallbacks,
                    postgameInjuryRefreshKey = postgameInjuryRefreshKey,
                    disciplineDashboardRefreshKey = disciplineDashboardRefreshKey,
                    showNotificationFeed = appSettings.showDashboardNotificationFeed,
                    onOpenRecruitingTab = { tab ->
                        recruitingLaunchTab = tab
                        navController.navigateToRecruitingHub()
                    },
                    onOpenSigningDayFromOffseason = { link ->
                        signingDayInitialTab = when (link) {
                            DashboardDeepLink.SigningDayRankings -> SigningDayInitialTab.Rankings
                            DashboardDeepLink.SigningDayPositions -> SigningDayInitialTab.Positions
                            else -> SigningDayInitialTab.Rankings
                        }
                        navController.navigate(AppRoute.SigningDay.route)
                    },
                    onOpenSigningDayFromShell = { navController.navigate(AppRoute.SigningDay.route) },
                    onOpenAutomation = { navController.navigate(AppRoute.DynastyAutomation.route) },
                    onOpenDiscipline = { incident ->
                        disciplineIncident = incident
                        navController.navigate(AppRoute.Discipline.route)
                    },
                    onOpenGamePreview = { navController.navigate(AppRoute.GamePreview.route) },
                    onOpenGameWatch = { navController.navigate(AppRoute.GameWatch.route) },
                    onOpenJobCenter = {
                        scope.launch {
                            val active = ActiveDynastySession.current ?: return@launch
                            coachEmploymentRepository.refreshOffersIfNeeded(
                                active.dynastyId,
                                active.metadata.seasonIndex,
                            )
                            navController.navigate(AppRoute.DynastyCareer.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onExitToHome = {
                        navController.popBackStack(AppRoute.Home.route, inclusive = false)
                    },
                    onAfterJobOfferAccepted = { reloaded ->
                        scope.launch { onDynastyReady(reloaded) }
                    },
                )
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding ->
            NavHost(
                navController = navController,
                startDestination = startRoute,
                modifier = Modifier.padding(padding),
            ) {
                composable(AppRoute.Studio.route) {
                    StudioScreen(
                        onFinished = {
                            studioBeatConsumed = true
                            navController.navigate(AppRoute.Splash.route)
                        },
                    )
                }
                composable(AppRoute.Splash.route) {
                    SplashScreen(
                        bootstrapPhase = splashPhase,
                        onTapToPlay = {
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        },
                        onRetryLoad = { splashLoadAttempt++ },
                    )
                }
                composable(AppRoute.Home.route) {
                    Box(modifier = Modifier.fillMaxSize()) {
                    HomeMenuScreen(
                        continueSlot = continueSlot,
                        continueLoading = continueLoading,
                        onContinue = {
                            val slot = continueSlot ?: return@HomeMenuScreen
                            scope.launch {
                                continueLoading = true
                                try {
                                    when (val result = saveSlotRepository.loadSlot(slot.slotIndex)) {
                                        is DynastyLoadResult.Success -> onDynastyReady(result.loaded)
                                        is DynastyLoadResult.Unreadable ->
                                            continueLoadFailedSlot = slot.slotIndex
                                        is DynastyLoadResult.WeekAdvanceBlocked -> Unit
                                        DynastyLoadResult.SlotEmpty -> Unit
                                    }
                                } finally {
                                    continueLoading = false
                                }
                            }
                        },
                        onNew = { navController.navigate(AppRoute.NewGame.route) },
                        onLoad = { navController.navigate(AppRoute.Load.route) },
                        onSettings = { navController.navigate(AppRoute.Settings.route) },
                        onCredits = { navController.navigate(AppRoute.Credits.route) },
                    )
                    if (continueLoading) {
                        DynastyLoadProgressOverlay()
                    }
                    }
                }
                composable(AppRoute.Load.route) {
                    LoadGameScreen(
                        saveSlotRepository = saveSlotRepository,
                        onBack = { navController.popBackStack() },
                        onDynastyLoaded = { loaded ->
                            scope.launch {
                                onDynastyReady(loaded)
                                navController.popBackStack()
                            }
                        },
                    )
                }
                composable(AppRoute.SeasonReview.route) {
                    val session = ActiveDynastySession.current
                    if (session != null) {
                        SeasonReviewScreen(
                            loaded = session,
                            saveSlotRepository = saveSlotRepository,
                            seasonReviewRepository = LocalContext.current.requireDynastyApplication().seasonReviewRepository,
                            seasonReviewProgressRepository =
                                LocalContext.current.requireDynastyApplication().seasonReviewProgressRepository,
                            hubMenuCallbacks = hubMenuCallbacks,
                            recruitingEconomyRepository =
                                LocalContext.current.requireDynastyApplication().recruitingEconomyRepository,
                            onReturnedHome = {
                                navController.popBackStack(AppRoute.Home.route, inclusive = false)
                            },
                            onEnteredOffseason = { updated ->
                                scope.launch {
                                    val security = coachSecurityRepository.loadForUserDynasty(updated.dynastyId)
                                    if (
                                        dev.novux.dynasty.cbb.core.coach.employment.CoachFiringEngine
                                            .shouldEnterUnemployed(
                                                security?.jobSecurity?.score ?: 0,
                                                updated.metadata.seasonIndex,
                                                updated.dynastyId,
                                            )
                                    ) {
                                        coachEmploymentRepository.enterUnemployed(
                                            updated.dynastyId,
                                            updated.metadata.seasonIndex,
                                        )
                                    }
                                    onDynastyReady(updated)
                                }
                            },
                            userCalendarRepository = LocalContext.current.requireDynastyApplication().userCalendarRepository,
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.CoachingCarousel.route) {
                    val session = ActiveDynastySession.current
                    if (session != null) {
                        CoachingCarouselScreen(
                            loaded = session,
                            repository = coachingCarouselRepository,
                            rosterRepository = LocalContext.current.requireDynastyApplication().dynastyRosterRepository,
                            userCalendarRepository = LocalContext.current.requireDynastyApplication().userCalendarRepository,
                            hubMenuCallbacks = hubMenuCallbacks,
                            onComplete = {
                                navController.navigate(AppRoute.DynastyCareer.route) {
                                    popUpTo(AppRoute.Home.route) { inclusive = false }
                                }
                            },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.JobCenter.route) {
                    val session = ActiveDynastySession.current
                    if (session != null) {
                        LaunchedEffect(Unit) {
                            navController.navigate(AppRoute.DynastyCareer.route) {
                                launchSingleTop = true
                                popUpTo(AppRoute.JobCenter.route) { inclusive = true }
                            }
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.DynastyCareer.route) { DynastyCareerRoute() }
                composable(AppRoute.Preseason.route) { DynastyCareerRoute() }
                composable(AppRoute.DynastyShell.route) { DynastyCareerRoute() }
                composable(AppRoute.Discipline.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    val incident = disciplineIncident
                    if (session != null && incident != null) {
                        DisciplineDecisionScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            incident = incident,
                            disciplineRepository = app.disciplineRepository,
                            onResolved = {
                                disciplineIncident = null
                                disciplineDashboardRefreshKey++
                                navController.popBackToInShellDashboard()
                            },
                            onBack = { navController.popBackStack() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.Strategy.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    var teamChem by remember { mutableStateOf<Int?>(null) }
                    var fatigueTuning by remember {
                        mutableStateOf(dev.novux.dynasty.cbb.core.balance.BalanceFatigue(0.35, 25, 15, 20, 35))
                    }
                    if (session != null) {
                        var prepEditable by remember { mutableStateOf(true) }
                        LaunchedEffect(session.dynastyId, session.metadata.seasonIndex, session.weekCheckpoint) {
                            teamChem = app.chemistryRepository.loadState(
                                session.dynastyId,
                                session.metadata.seasonIndex,
                            ).teamChemistry
                            fatigueTuning = app.balanceRulesetRepository.activeRuleset().fatigue
                            val plan = app.preseasonScheduleRepository.loadPlan(
                                session.dynastyId,
                                session.metadata.seasonIndex,
                            )
                            val exhibitionCount = app.preseasonScheduleRepository.loadExhibitionCount(
                                session.dynastyId,
                                session.metadata.seasonIndex,
                            )
                            val hasScheduled = InSeasonWeekGames.forPosition(
                                session.weekCheckpoint.activePosition,
                                plan,
                                exhibitionCount,
                            ).isNotEmpty()
                            prepEditable = StrategyEditLockPolicy.prepEditable(
                                checkpoint = session.weekCheckpoint,
                                hasScheduledUserGame = hasScheduled,
                                phase = session.weekCheckpoint.activePosition.phase,
                            )
                        }
                        StrategyScreen(
                            dynastyId = session.dynastyId,
                            rosterRepository = app.dynastyRosterRepository,
                            strategyPrepRepository = app.dynastyStrategyPrepRepository,
                            teamChemistry = teamChem,
                            fatigueTuning = fatigueTuning,
                            prepEditable = prepEditable,
                            homeTeamLabel = session.summary.schoolDisplayName.orEmpty().ifBlank { "Home" },
                            awayTeamLabel = stringResource(R.string.game_watch_opponent_scrimmage),
                            onWatchScrimmage = { navController.navigate(AppRoute.GameWatch.route) },
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.GamePreview.route) {
                    val preview = GamePreviewLaunch.result
                    val app = LocalContext.current.requireDynastyApplication()
                    val scope = rememberCoroutineScope()
                    if (preview != null) {
                        GamePreviewScreen(
                            matchupLabel = GamePreviewLaunch.matchupLabel,
                            userTeamName = GamePreviewLaunch.userTeamName,
                            opponentName = GamePreviewLaunch.opponentName,
                            venuePhrase = GamePreviewLaunch.venuePhrase,
                            venue = GamePreviewLaunch.venue,
                            preview = preview,
                            onOpenStrategy = { navController.navigate(AppRoute.Strategy.route) },
                            onPlayGame = {
                                scope.launch {
                                    val session = ActiveDynastySession.current ?: return@launch
                                    if (app.userCalendarGameWatchBootstrap.configure(session)) {
                                        navController.navigate(AppRoute.GameWatch.route)
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() },
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
                composable(AppRoute.GameWatch.route) {
                    val app = LocalContext.current.requireDynastyApplication()
                    val scope = rememberCoroutineScope()
                    LaunchedEffect(Unit) {
                        watchPostgameEvents = emptyList()
                        watchPostgameRoster = emptyList()
                    }
                    var tuning by remember {
                        mutableStateOf<dev.novux.dynasty.cbb.core.sim.tuning.SimTuning?>(null)
                    }
                    var medicalRules by remember {
                        mutableStateOf(dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules())
                    }
                    LaunchedEffect(Unit) {
                        medicalRules = app.balanceRulesetRepository.activeRuleset().injuryTuning().medicalRedshirt
                    }
                    LaunchedEffect(Unit) {
                        tuning = app.simTuningRepository.activeTuning()
                    }
                    LaunchedEffect(Unit) {
                        val session = ActiveDynastySession.current ?: return@LaunchedEffect
                        if (GameWatchLaunch.injuryRoll == null || GameWatchLaunch.isScrimmage) {
                            val ruleset = app.balanceRulesetRepository.activeRuleset()
                            val injuryTuning = ruleset.injuryTuning()
                            val bank = dev.novux.dynasty.cbb.core.injuries.InjuryOccurrenceEngine
                                .bankFromBalance(ruleset.injuries.bank)
                            GameWatchLaunch.injuryRoll = dev.novux.dynasty.cbb.core.sim.GameSimInjuryRollInput(
                                phase = session.metadata.phase,
                                bank = bank,
                                occurrence = injuryTuning.occurrence,
                                medicalStaffGrade = 70,
                                homeRosterPlayers = GameWatchLaunch.homeRosterPlayers,
                                awayRosterPlayers = null,
                            )
                        }
                    }
                    val ready = tuning
                    val session = ActiveDynastySession.current
                    val rsWeek = session?.weekCheckpoint?.activePosition?.weekIndex ?: 0
                    if (ready != null) {
                        GameWatchScreen(
                            tuning = ready,
                            homeName = GameWatchLaunch.homeName,
                            awayName = GameWatchLaunch.awayName,
                            homeGameplan = GameWatchLaunch.homeGameplan,
                            homeOffensiveScheme = GameWatchLaunch.homeOffensiveScheme,
                            homeDefensiveScheme = GameWatchLaunch.homeDefensiveScheme,
                            homeRosterPlayers = GameWatchLaunch.homeRosterPlayers,
                            homeStarters = GameWatchLaunch.homeStarters,
                            homeSimRoster = GameWatchLaunch.homeSimRoster,
                            injuryRoll = GameWatchLaunch.injuryRoll,
                            userTeamIsHome = GameWatchLaunch.userTeamIsHome,
                            homeVenue = GameWatchLaunch.homeVenue,
                            userOffensiveRating = GameWatchLaunch.userOffensiveRating,
                            userDefensiveRating = GameWatchLaunch.userDefensiveRating,
                            awayOffensiveRating = GameWatchLaunch.awayOffensiveRating,
                            awayDefensiveRating = GameWatchLaunch.awayDefensiveRating,
                            seed = GameWatchLaunch.simSeed ?: 17L,
                            postgameInjuryEvents = watchPostgameEvents,
                            postgameInjuryRoster = watchPostgameRoster,
                            rsWeekIndex = rsWeek,
                            medicalRules = medicalRules,
                            onDismissPostgameInjuries = {
                                scope.launch {
                                    val active = ActiveDynastySession.current
                                    if (active != null && !GameWatchLaunch.isScrimmage && watchPostgameEvents.isNotEmpty()) {
                                        val ruleset = app.balanceRulesetRepository.activeRuleset()
                                        val minOvr = ruleset.injuryTuning().occurrence.headlineOverallMin
                                        val newsworthy = dev.novux.dynasty.cbb.core.news.UserInjuryNewsGate
                                            .newsworthy(
                                                watchPostgameEvents,
                                                watchPostgameRoster,
                                                minOvr,
                                            )
                                        if (newsworthy.isNotEmpty()) {
                                            val names = watchPostgameRoster.associate {
                                                it.playerId to it.displayName
                                            }
                                            app.newsFeedRepository.appendUserInjuries(
                                                active.dynastyId,
                                                active.metadata.seasonIndex,
                                                active.weekCheckpoint.activePosition.weekIndex,
                                                names,
                                                newsworthy,
                                            )
                                        }
                                    }
                                    active?.let { app.dynastyRosterRepository.clearPostgameInjuries(it.dynastyId) }
                                    watchPostgameEvents = emptyList()
                                    postgameInjuryRefreshKey++
                                }
                            },
                            onApplyMedicalRedshirt = { playerId ->
                                scope.launch {
                                    val active = ActiveDynastySession.current ?: return@launch
                                    watchPostgameRoster = app.dynastyRosterRepository.applyMedicalRedshirt(
                                        active.dynastyId,
                                        playerId,
                                    )
                                    app.dynastyRosterRepository.clearPostgameInjuries(active.dynastyId)
                                    watchPostgameEvents = emptyList()
                                    postgameInjuryRefreshKey++
                                }
                            },
                            onHomeHalftimeAdjustments = { adj ->
                                GameWatchLaunch.homeGameplan = adj.gameplan
                                GameWatchLaunch.homeOffensiveScheme = adj.offensiveScheme
                                GameWatchLaunch.homeDefensiveScheme = adj.defensiveScheme
                                GameWatchLaunch.homeStarters = adj.starters
                            },
                            onSimComplete = { result ->
                                scope.launch {
                                    val active = ActiveDynastySession.current ?: return@launch
                                    if (GameWatchLaunch.isScrimmage) {
                                        app.scrimmageGameRecordRepository.save(
                                            active.dynastyId,
                                            GameWatchLaunch.awayName,
                                            result,
                                        )
                                        GameWatchLaunch.isScrimmage = false
                                        return@launch
                                    }
                                    val position = active.weekCheckpoint.activePosition
                                    val gameId = GameWatchLaunch.activeGameId
                                        ?: UserLeagueGameIds.forPosition(position)
                                    val userHome = GameWatchLaunch.userTeamIsHome
                                    val priorGen = active.weekCheckpoint.provisionalGames[gameId]?.simGeneration
                                    val mark = ProvisionalGameMark.fromGameSim(
                                        gameId = gameId,
                                        result = result,
                                        userTeamIsHome = userHome,
                                        previousGeneration = priorGen ?: 0,
                                    )
                                    val staged = app.weekCheckpointRepository.stageSim(
                                        active.dynastyId,
                                        active.weekCheckpoint,
                                        mark,
                                    )
                                    ActiveDynastySession.set(active.copy(weekCheckpoint = staged))
                                    val events = result.userTeamInjuries(userTeamIsHome = userHome)
                                    GameWatchLaunch.activeGameId = null
                                    if (events.isEmpty()) return@launch
                                    val week = position.weekIndex
                                    val before = app.dynastyRosterRepository.listPlayers(active.dynastyId)
                                    val rsGames = before.firstOrNull { it.playerId == events.first().playerId }
                                        ?.rsGamesPlayedSeason ?: 0
                                    val roster = app.dynastyRosterRepository.applyGameInjuries(
                                        active.dynastyId,
                                        events,
                                        dev.novux.dynasty.cbb.core.injuries.InjuryApplyContext(
                                            rsWeekIndex = week,
                                            rsGameIndex = week.coerceAtLeast(1),
                                            rsGamesPlayed = rsGames,
                                        ),
                                    )
                                    watchPostgameEvents = events
                                    watchPostgameRoster = roster
                                    postgameInjuryRefreshKey++
                                }
                            },
                            onBack = {
                                if (watchPostgameEvents.isNotEmpty()) {
                                    session?.let {
                                        app.dynastyRosterRepository.clearPostgameInjuries(it.dynastyId)
                                    }
                                    watchPostgameEvents = emptyList()
                                    postgameInjuryRefreshKey++
                                }
                                navController.popBackStack()
                            },
                        )
                    }
                }
                composable(AppRoute.Offseason.route) { DynastyCareerRoute() }
                composable(AppRoute.NewGame.route) {
                    NewGameWizardScreen(
                        saveSlotRepository = saveSlotRepository,
                        packRepository = LocalContext.current.requireDynastyApplication().packRepository,
                        balanceRulesetRepository =
                            LocalContext.current.requireDynastyApplication().balanceRulesetRepository,
                        onExitWizard = { navController.popBackStack() },
                        onDynastyCreated = {
                            scope.launch {
                                ActiveDynastySession.current?.let { onDynastyReady(it) }
                                navController.popBackStack()
                            }
                        },
                        onNoEmptySlot = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.new_no_empty_slot),
                                )
                            }
                        },
                    )
                }
                composable(AppRoute.Credits.route) {
                    CreditsScreen(onBack = { navController.popBackStack() })
                }
                composable(AppRoute.Recruiting.route) {
                    val active = ActiveDynastySession.current
                    if (active != null) {
                        RecruitingHubScreen(
                            loaded = active,
                            recruitingEconomyRepository = recruitingEconomyRepository,
                            recruitOfferPromiseRepository = LocalContext.current.requireDynastyApplication().recruitOfferPromiseRepository,
                            nilBudgetRepository = LocalContext.current.requireDynastyApplication().nilBudgetRepository,
                            nilAnnualPoolRepository = LocalContext.current.requireDynastyApplication().nilAnnualPoolRepository,
                            weekNineRepository = offseasonWeekNineRepository,
                            incomingClassSeedRepository = incomingClassSeedRepository,
                            portalCycleRepository = portalCycleRepository,
                            initialTab = recruitingLaunchTab,
                            initialFocusProspectId = recruitingFocusProspectId,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackToInShellDashboard()
                        }
                    }
                }
                composable(AppRoute.SigningDay.route) {
                    val active = ActiveDynastySession.current
                    if (active != null) {
                        SigningDayScreen(
                            loaded = active,
                            signingDayRepository = signingDayRepository,
                            recruitingEconomyRepository = recruitingEconomyRepository,
                            nilAnnualPoolRepository = LocalContext.current.requireDynastyApplication().nilAnnualPoolRepository,
                            weekNineRepository = offseasonWeekNineRepository,
                            incomingClassSeedRepository = incomingClassSeedRepository,
                            recruitOfferPromiseRepository = LocalContext.current.requireDynastyApplication()
                                .recruitOfferPromiseRepository,
                            initialTab = signingDayInitialTab,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    }
                }
                composable(AppRoute.Settings.route) {
                    SettingsScreen(
                        settingsRepository = settingsRepository,
                        onBack = { navController.popBackStack() },
                        onOpenPackImport = { navController.navigate(AppRoute.PackImport.route) },
                    )
                }
                composable(AppRoute.PackImport.route) {
                    val app = context.requireDynastyApplication()
                    var dynastyMembershipYear by remember { mutableStateOf<String?>(null) }
                    val session = ActiveDynastySession.current
                    LaunchedEffect(session?.dynastyId) {
                        val dynastyId = session?.dynastyId
                        dynastyMembershipYear = if (dynastyId != null) {
                            app.packDynastyIdentityRepository.loadMembershipYearAtSign(dynastyId)
                        } else {
                            null
                        }
                    }
                    IdentityPackImportScreen(
                        packRepository = app.packRepository,
                        dynastyMembershipYear = dynastyMembershipYear,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(AppRoute.DynastyAutomation.route) {
                    val active = ActiveDynastySession.current
                    if (active != null) {
                        val app = LocalContext.current.requireDynastyApplication()
                        DynastyAutomationScreen(
                            loaded = active,
                            automationRepository = app.dynastyAutomationRepository,
                            coachSkillPointRepository = app.coachSkillPointRepository,
                            saveSlotRepository = saveSlotRepository,
                            onBack = { navController.popBackStack() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.NewsHub.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    if (session != null) {
                        NewsHubScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            position = session.weekCheckpoint.activePosition,
                            newsFeedRepository = app.newsFeedRepository,
                            incomingClassSeedRepository = incomingClassSeedRepository,
                            portalCycleRepository = portalCycleRepository,
                            offseasonWeekTwoRepository = offseasonWeekTwoRepository,
                            packRepository = app.packRepository,
                            onBack = { navController.popBackToInShellDashboard() },
                            onOpenRecruitingPortal = {
                                recruitingFocusProspectId = null
                                recruitingLaunchTab = NewsHubPortalAddToBoardPolicy.recruitingHubTab
                                navController.navigateToRecruitingHub()
                            },
                            onOpenPriorProgram = {
                                recruitingFocusProspectId = null
                                recruitingLaunchTab = NewsHubPortalAddToBoardPolicy.recruitingHubTab
                                navController.navigateToRecruitingHub()
                            },
                            onOpenPortalEntrantFocus = { entrantId ->
                                recruitingFocusProspectId = entrantId
                                recruitingLaunchTab = dev.novux.dynasty.cbb.core.recruiting.RecruitingHubFocusPolicy.portalDeepLinkTab
                                navController.navigateToRecruitingHub()
                            },
                            initialTab = newsHubLaunchTab,
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.SchoolHub.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    if (session != null) {
                        SchoolHubRouteScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            schoolHubRepository = schoolHubRepository,
                            chemistryRepository = app.chemistryRepository,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.StatisticsHub.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    if (session != null) {
                        StatisticsHubRouteScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            repository = app.statisticsHubRepository,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.RankingsHub.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    if (session != null) {
                        RankingsHubRouteScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            rankingsHubRepository = app.rankingsHubRepository,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.HistoryHub.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    if (session != null) {
                        dev.novux.dynasty.cbb.ui.history.HistoryHubRouteScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            historyRepository = app.historyRepository,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.ConferenceTournamentHub.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    if (session != null) {
                        ConferenceTournamentHubRouteScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            repository = app.conferenceTournamentHubRepository,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.MarchMayhem.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    if (session != null) {
                        MarchMayhemHubRouteScreen(
                            dynastyId = session.dynastyId,
                            seasonIndex = session.metadata.seasonIndex,
                            repository = app.marchMayhemHubRepository,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
                composable(AppRoute.AwardsHub.route) {
                    val session = ActiveDynastySession.current
                    val app = LocalContext.current.requireDynastyApplication()
                    var userConferenceId by remember { mutableStateOf("") }
                    var userConferenceName by remember { mutableStateOf("") }
                    var conferences by remember { mutableStateOf<List<ConferenceOption>>(emptyList()) }
                    var awardsArchive by remember { mutableStateOf<YearlyAwardsSeasonArchive?>(null) }
                    if (session != null) {
                        LaunchedEffect(session.dynastyId, session.metadata.seasonIndex) {
                            val index = app.packRepository.activeSession().index
                            conferences = index.allConferenceDisplays().map {
                                ConferenceOption(it.conferenceId, it.displayName)
                            }
                            val overview = app.schoolHubRepository.loadUserSchoolOverview(
                                session.dynastyId,
                                session.metadata.seasonIndex,
                            )
                            userConferenceName = overview?.conferenceName.orEmpty()
                            userConferenceId = overview?.let { o ->
                                index.team(o.teamId)?.conferenceId
                            }.orEmpty().ifBlank { conferences.firstOrNull()?.id.orEmpty() }
                            if (session.weekCheckpoint.activePosition.phase == CalendarPhase.SeasonReview) {
                                app.yearlyAwardsRepository.wrapSeasonIfNeeded(
                                    session.dynastyId,
                                    session.metadata.seasonIndex,
                                )
                            }
                            awardsArchive = app.yearlyAwardsRepository.loadArchive(
                                session.dynastyId,
                                session.metadata.seasonIndex,
                            )
                        }
                        AwardsHubScreen(
                            seasonIndex = session.metadata.seasonIndex,
                            position = session.weekCheckpoint.activePosition,
                            userConferenceId = userConferenceId,
                            userConferenceName = userConferenceName,
                            conferences = conferences,
                            currentSeasonArchive = awardsArchive,
                            onBack = { navController.popBackToInShellDashboard() },
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(AppRoute.Home.route, inclusive = false)
                        }
                    }
                }
            }

            continueLoadFailedSlot?.let { slotIndex ->
                SaveLoadUnavailableDialog(
                    slotDisplayNumber = slotIndex + 1,
                    onDelete = {
                        scope.launch {
                            saveSlotRepository.deleteSlot(slotIndex)
                            if (ActiveDynastySession.current?.slotIndex == slotIndex) {
                                ActiveDynastySession.clear()
                            }
                            continueLoadFailedSlot = null
                        }
                    },
                    onDismiss = { continueLoadFailedSlot = null },
                )
            }
            }
        }
    }
}

package dev.novux.dynasty.cbb.ui.dynasty

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentState
import dev.novux.dynasty.cbb.core.coach.employment.CoachEmploymentStatus
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.discipline.PendingDisciplineIncident
import dev.novux.dynasty.cbb.core.navigation.DynastyCareerPhaseResolver
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCategory
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import androidx.compose.runtime.LaunchedEffect
import dev.novux.dynasty.cbb.data.coach.CoachEmploymentRepository
import dev.novux.dynasty.cbb.data.coach.CoachSecurityRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekEightRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekNineRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekTenRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekTwoRepository
import dev.novux.dynasty.cbb.data.offseason.PortalCycleRepository
import dev.novux.dynasty.cbb.data.offseason.SigningDayRepository
import dev.novux.dynasty.cbb.data.offseason.SummerWorkoutRepository
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonProgressRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonReportRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonSeasonStartRepository
import dev.novux.dynasty.cbb.data.preseason.TournamentInvitesRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession
import dev.novux.dynasty.cbb.data.save.SaveSlotRepository
import dev.novux.dynasty.cbb.data.coach.SchoolStaffRepository
import dev.novux.dynasty.cbb.data.school.SchoolHubRepository
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.ui.hubs.DynastyHubMenuCallbacks
import dev.novux.dynasty.cbb.ui.offseason.OffseasonScreen
import dev.novux.dynasty.cbb.ui.preseason.PreseasonScreen
import dev.novux.dynasty.cbb.ui.coach.UnemployedCoachCareerScreen
import dev.novux.dynasty.cbb.ui.dynasty.rememberShellNewsFeed
import dev.novux.dynasty.cbb.ui.shell.DynastyShellScreen
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/** Single NavHost destination; body swaps by [CalendarPhase] only ([MEGA-FRAME-005]). */
@Composable
fun DynastyCareerHost(
    saveSlotRepository: SaveSlotRepository,
    recruitingEconomyRepository: RecruitingEconomyRepository,
    portalCycleRepository: PortalCycleRepository,
    signingDayRepository: SigningDayRepository,
    offseasonWeekTwoRepository: OffseasonWeekTwoRepository,
    summerWorkoutRepository: SummerWorkoutRepository,
    offseasonWeekEightRepository: OffseasonWeekEightRepository,
    offseasonWeekNineRepository: OffseasonWeekNineRepository,
    offseasonWeekTenRepository: OffseasonWeekTenRepository,
    preseasonProgressRepository: PreseasonProgressRepository,
    incomingClassSeedRepository: IncomingClassSeedRepository,
    tournamentInvitesRepository: TournamentInvitesRepository,
    preseasonScheduleRepository: PreseasonScheduleRepository,
    preseasonReportRepository: PreseasonReportRepository,
    preseasonSeasonStartRepository: PreseasonSeasonStartRepository,
    schoolStaffRepository: SchoolStaffRepository,
    schoolHubRepository: SchoolHubRepository,
    coachSecurityRepository: CoachSecurityRepository,
    coachEmploymentRepository: CoachEmploymentRepository,
    hubMenuCallbacks: DynastyHubMenuCallbacks,
    postgameInjuryRefreshKey: Int,
    disciplineDashboardRefreshKey: Int,
    showNotificationFeed: Boolean,
    mutedNotificationCategories: Set<NotificationFeedCategory> = emptySet(),
    onOpenRecruitingTab: (RecruitingHubTab, String?) -> Unit,
    onOpenSigningDayFromOffseason: (DashboardDeepLink) -> Unit,
    onOpenSigningDayFromShell: () -> Unit,
    onOpenAutomation: () -> Unit,
    onOpenDiscipline: (PendingDisciplineIncident) -> Unit,
    onOpenGamePreview: () -> Unit,
    onOpenGameWatch: () -> Unit,
    onOpenJobCenter: () -> Unit,
    onExitToHome: () -> Unit,
    onAfterJobOfferAccepted: suspend (LoadedDynasty) -> Unit = {},
) {
    var session by remember { mutableStateOf(ActiveDynastySession.current) }
    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        val remove = ActiveDynastySession.addListener { session = ActiveDynastySession.current }
        onDispose(remove)
    }
    val loaded = session
    if (loaded == null) {
        LaunchedEffect(Unit) { onExitToHome() }
        return
    }
    val app = LocalContext.current.requireDynastyApplication()
    var employment by remember { mutableStateOf<CoachEmploymentState?>(null) }
    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex) {
        employment = coachEmploymentRepository.refreshOffersIfNeeded(
            loaded.dynastyId,
            loaded.metadata.seasonIndex,
        )
    }
    val newsFeed = rememberShellNewsFeed(
        dynastyId = loaded.dynastyId,
        seasonIndex = loaded.metadata.seasonIndex,
        weekIndex = loaded.weekCheckpoint.activePosition.weekIndex,
        newsFeedRepository = app.newsFeedRepository,
        rosterRepository = app.dynastyRosterRepository,
    )
    if (employment?.status == CoachEmploymentStatus.Unemployed) {
        UnemployedCoachCareerScreen(
            loaded = loaded,
            offers = employment?.pendingOffers.orEmpty(),
            newsFeed = newsFeed,
            onAccept = { offer ->
                scope.launch {
                    coachEmploymentRepository.acceptOffer(loaded.dynastyId, offer)
                    when (val result = saveSlotRepository.loadSlot(loaded.slotIndex)) {
                        is DynastyLoadResult.Success -> onAfterJobOfferAccepted(result.loaded)
                        else -> Unit
                    }
                }
            },
            onWait = onExitToHome,
            onExitHome = onExitToHome,
        )
        return
    }
    when (
        DynastyCareerPhaseResolver.careerBody(loaded.weekCheckpoint.activePosition.phase)
    ) {
        DynastyCareerPhaseResolver.CareerBody.Offseason -> {
            OffseasonScreen(
                initial = loaded,
                saveSlotRepository = saveSlotRepository,
                rosterRepository = app.dynastyRosterRepository,
                recruitingEconomyRepository = recruitingEconomyRepository,
                portalCycleRepository = portalCycleRepository,
                signingDayRepository = signingDayRepository,
                offseasonWeekTwoRepository = offseasonWeekTwoRepository,
                summerWorkoutRepository = summerWorkoutRepository,
                weekEightRepository = offseasonWeekEightRepository,
                weekNineRepository = offseasonWeekNineRepository,
                weekTenRepository = offseasonWeekTenRepository,
                hubMenuCallbacks = hubMenuCallbacks,
                onOpenRecruiting = onOpenRecruitingTab,
                onOpenSigningDay = onOpenSigningDayFromOffseason,
                userCalendarRepository = app.userCalendarRepository,
                showNotificationFeed = showNotificationFeed,
                mutedNotificationCategories = mutedNotificationCategories,
            )
        }
        DynastyCareerPhaseResolver.CareerBody.Preseason -> {
            PreseasonScreen(
                initial = loaded,
                saveSlotRepository = saveSlotRepository,
                progressRepository = preseasonProgressRepository,
                incomingClassSeedRepository = incomingClassSeedRepository,
                tournamentInvitesRepository = tournamentInvitesRepository,
                scheduleRepository = preseasonScheduleRepository,
                preseasonReportRepository = preseasonReportRepository,
                seasonStartRepository = preseasonSeasonStartRepository,
                rosterRepository = app.dynastyRosterRepository,
                userCalendarRepository = app.userCalendarRepository,
                onSeasonStarted = {},
                hubMenuCallbacks = hubMenuCallbacks,
                showNotificationFeed = showNotificationFeed,
                mutedNotificationCategories = mutedNotificationCategories,
                onOpenRecruitingTab = onOpenRecruitingTab,
                onOpenSigningDay = onOpenSigningDayFromOffseason,
            )
        }
        DynastyCareerPhaseResolver.CareerBody.InSeason -> {
            DynastyShellScreen(
                initial = loaded,
                saveSlotRepository = saveSlotRepository,
                scheduleRepository = preseasonScheduleRepository,
                staffRepository = schoolStaffRepository,
                schoolHubRepository = schoolHubRepository,
                coachSecurityRepository = coachSecurityRepository,
                rosterRepository = app.dynastyRosterRepository,
                disciplineRepository = app.disciplineRepository,
                chemistryRepository = app.chemistryRepository,
                userCalendarRepository = app.userCalendarRepository,
                newsFeedRepository = app.newsFeedRepository,
                coachSkillPointRepository = app.coachSkillPointRepository,
                notificationFeedRepository = app.notificationFeedRepository,
                coachEmploymentRepository = coachEmploymentRepository,
                dynastyAutomationRepository = app.dynastyAutomationRepository,
                showNotificationFeed = showNotificationFeed,
                hubMenuCallbacks = hubMenuCallbacks,
                onOpenSigningDay = onOpenSigningDayFromOffseason,
                mutedNotificationCategories = mutedNotificationCategories,
                onOpenRecruitingTab = onOpenRecruitingTab,
                onOpenAutomation = onOpenAutomation,
                onOpenDiscipline = onOpenDiscipline,
                onOpenGamePreview = onOpenGamePreview,
                onOpenGameWatch = onOpenGameWatch,
                onOpenJobCenter = onOpenJobCenter,
                postgameInjuryRefreshKey = postgameInjuryRefreshKey,
                disciplineDashboardRefreshKey = disciplineDashboardRefreshKey,
            )
        }
        null -> Unit
    }
}

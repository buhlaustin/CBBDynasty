package dev.novux.dynasty.cbb.ui.seasonreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.animateScrollTo
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.novux.dynasty.cbb.core.calendar.CalendarWeekLabel
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCycleLedger
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewMenuPolicy
import dev.novux.dynasty.cbb.core.calendar.OffseasonEntryKind
import dev.novux.dynasty.cbb.core.calendar.OffseasonEntryRouter
import dev.novux.dynasty.cbb.core.calendar.SeasonReviewGate
import dev.novux.dynasty.cbb.core.dashboard.SeasonReviewRequiredDashboardTasks
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewProgress
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewSnapshot
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewSurfacePolicy
import dev.novux.dynasty.cbb.data.calendar.UserCalendarRepository
import dev.novux.dynasty.cbb.data.save.ActiveDynastySession
import dev.novux.dynasty.cbb.data.save.SaveSlotRepository
import dev.novux.dynasty.cbb.data.seasonreview.SeasonReviewProgressRepository
import dev.novux.dynasty.cbb.data.recruiting.RecruitingEconomyRepository
import dev.novux.dynasty.cbb.data.seasonreview.SeasonReviewRepository
import dev.novux.dynasty.cbb.ui.dashboard.DashboardRequiredTasksPanel
import dev.novux.dynasty.cbb.ui.hubs.DynastyHubMenuCallbacks
import dev.novux.dynasty.cbb.ui.hubs.buildDynastyHubMenu
import dev.novux.dynasty.cbb.ui.hubs.notifyRecruitingCycleClosed
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellCalendarTab
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellChrome
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellMenuAction
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellNewsTab
import dev.novux.dynasty.cbb.ui.dynasty.rememberShellNewsFeed
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellTab
import kotlinx.coroutines.launch

/** End of Season gate — [planning/season-review.md]. */
@Composable
fun SeasonReviewScreen(
    loaded: LoadedDynasty,
    saveSlotRepository: SaveSlotRepository,
    seasonReviewRepository: SeasonReviewRepository,
    seasonReviewProgressRepository: SeasonReviewProgressRepository,
    hubMenuCallbacks: DynastyHubMenuCallbacks,
    recruitingEconomyRepository: RecruitingEconomyRepository,
    onReturnedHome: () -> Unit,
    onEnteredOffseason: (LoadedDynasty) -> Unit = { onReturnedHome() },
    userCalendarRepository: UserCalendarRepository,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val app = context.requireDynastyApplication()
    val newsFeed = rememberShellNewsFeed(
        dynastyId = loaded.dynastyId,
        seasonIndex = loaded.metadata.seasonIndex,
        weekIndex = loaded.weekCheckpoint.activePosition.weekIndex,
        newsFeedRepository = app.newsFeedRepository,
        rosterRepository = app.dynastyRosterRepository,
    )
    val weekHeader = CalendarWeekLabel.display(loaded.weekCheckpoint.activePosition)
    var selectedTab by remember { mutableStateOf(DynastyShellTab.Dashboard) }
    val schoolName = loaded.summary.schoolDisplayName.orEmpty()
    val position = loaded.weekCheckpoint.activePosition
    var recruitingLedger by remember { mutableStateOf<RecruitingCycleLedger?>(null) }
    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex) {
        recruitingLedger = recruitingEconomyRepository.loadOrOpenCycle(loaded)
    }
    val offseasonWeek = position.weekIndex.takeIf { position.phase == CalendarPhase.Offseason }
    val recruitingCycleClosed = RecruitingCyclePolicy.recruitingMenuTreatsCycleClosed(
        position.phase,
        offseasonWeek,
        recruitingLedger,
    )
    val menu = if (SeasonReviewMenuPolicy.usesFullDynastyHubMenu(position)) {
        buildDynastyHubMenu(
            position = position,
            callbacks = hubMenuCallbacks.copy(onExitHome = onReturnedHome),
            recruitingCycleClosed = recruitingCycleClosed,
            onRecruitingCycleClosed = { notifyRecruitingCycleClosed(context) },
        )
    } else {
        listOf(DynastyShellMenuAction(R.string.shell_menu_exit_home, onReturnedHome))
    }
    var snapshot by remember { mutableStateOf<SeasonReviewSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var reviewProgress by remember { mutableStateOf(SeasonReviewProgress()) }
    val recapScrollState = rememberScrollState()
    var scrollToRecap by remember { mutableStateOf(false) }
    val requiredTasks = SeasonReviewRequiredDashboardTasks.build(reviewProgress)
    val canEnterOffseason =
        SeasonReviewGate.canEnterOffseason(loaded.weekCheckpoint, reviewProgress.acknowledged)

    LaunchedEffect(loaded.dynastyId, loaded.metadata.seasonIndex) {
        loading = true
        reviewProgress = seasonReviewProgressRepository.load(loaded.dynastyId, loaded.metadata.seasonIndex)
        snapshot = seasonReviewRepository.loadSnapshot(loaded)
        loading = false
    }

    LaunchedEffect(scrollToRecap) {
        if (scrollToRecap) {
            recapScrollState.animateScrollTo(0)
            scrollToRecap = false
        }
    }

    DynastyShellChrome(
        schoolDisplayName = schoolName,
        phaseWeekLabel = weekHeader,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        menuActions = menu,
        headerSubtitle = stringResource(R.string.season_review_shell_header_hint),
    ) { padding ->
        when (selectedTab) {
            DynastyShellTab.Calendar -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.season_review_calendar_banner),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                )
                DynastyShellCalendarTab(
                    dynastyId = loaded.dynastyId,
                    seasonIndex = loaded.metadata.seasonIndex,
                    activePosition = loaded.weekCheckpoint.activePosition,
                    calendarRepository = userCalendarRepository,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                )
            }
            DynastyShellTab.News -> DynastyShellNewsTab(feed = newsFeed, contentPadding = padding)
            DynastyShellTab.Roster,
            DynastyShellTab.Dashboard,
            -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = weekHeader,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.season_review_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                )
                DashboardRequiredTasksPanel(
                    tasks = requiredTasks,
                    onOpenSigningDay = {},
                    onOpenSeasonReviewRecap = { scrollToRecap = true },
                )
                when {
                    loading -> {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator()
                            Text(
                                stringResource(R.string.season_review_loading),
                                modifier = Modifier.padding(top = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    SeasonReviewSurfacePolicy.hasRecap(snapshot) -> {
                        SeasonReviewScrollContent(
                            snapshot = snapshot!!,
                            modifier = Modifier.weight(1f, fill = false),
                            scrollState = recapScrollState,
                            showAcknowledgeAction = !reviewProgress.acknowledged,
                            onAcknowledge = {
                                scope.launch {
                                    reviewProgress = seasonReviewProgressRepository.acknowledge(
                                        loaded.dynastyId,
                                        loaded.metadata.seasonIndex,
                                    )
                                }
                            },
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.season_review_cadence_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                val previewEntry = OffseasonEntryRouter.entryAfterSeasonReview(loaded.metadata.seasonIndex + 1)
                Text(
                    text = when (previewEntry) {
                        OffseasonEntryKind.WeekOnePlayersLeaving ->
                            stringResource(R.string.season_review_offseason_week1)
                        OffseasonEntryKind.CoachingCarouselFirst ->
                            stringResource(R.string.season_review_carousel_next)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = {
                        scope.launch {
                            if (!canEnterOffseason) return@launch
                            when (
                                val result = saveSlotRepository.completeSeasonReviewEnterOffseason(loaded.slotIndex)
                            ) {
                                is DynastyLoadResult.Success -> {
                                    ActiveDynastySession.set(result.loaded)
                                    onEnteredOffseason(result.loaded)
                                }
                                else -> onReturnedHome()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canEnterOffseason,
                ) {
                    Text(stringResource(R.string.season_review_enter_offseason))
                }
            }
        }
    }
}

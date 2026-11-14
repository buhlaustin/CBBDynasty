package dev.novux.dynasty.cbb.ui.offseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.carousel.CarouselStatus
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselEngine
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselState
import dev.novux.dynasty.cbb.core.dashboard.CarouselDashboardTasks
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.carousel.CoachingCarouselPolicy
import dev.novux.dynasty.cbb.core.qa.CoachingCarouselShellPath
import dev.novux.dynasty.cbb.core.recruiting.RecruitingCyclePolicy
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.carousel.CoachingCarouselRepository
import dev.novux.dynasty.cbb.data.calendar.UserCalendarRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.requireDynastyApplication
import dev.novux.dynasty.cbb.ui.dashboard.DashboardRequiredTasksPanel
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellCalendarTab
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellChrome
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellNewsTab
import dev.novux.dynasty.cbb.ui.dynasty.rememberShellNewsFeed
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellRosterTab
import dev.novux.dynasty.cbb.ui.dynasty.DynastyShellTab
import dev.novux.dynasty.cbb.ui.hubs.DynastyHubMenuCallbacks
import dev.novux.dynasty.cbb.ui.hubs.buildDynastyHubMenu
import dev.novux.dynasty.cbb.ui.player.PlayerCardPreviewSheet
import dev.novux.dynasty.cbb.ui.player.PlayerCardScreen
import kotlinx.coroutines.launch

/**
 * Coaching Carousel inside dynasty shell chrome ([MEGA-FRAME-007], [MEGA-GDD-026], [planning/offseason.md]).
 */
@Composable
fun CoachingCarouselScreen(
    loaded: LoadedDynasty,
    repository: CoachingCarouselRepository,
    rosterRepository: DynastyRosterRepository,
    userCalendarRepository: UserCalendarRepository,
    hubMenuCallbacks: DynastyHubMenuCallbacks,
    onComplete: () -> Unit,
) {
    @Suppress("unused")
    val qaShell = CoachingCarouselShellPath.MEGA_ID
    @Suppress("unused")
    val qaFlow = CoachingCarouselPolicy.MEGA_ID
    var carousel by remember { mutableStateOf<CoachingCarouselState?>(null) }
    var selectedTab by remember { mutableStateOf(DynastyShellTab.Dashboard) }
    var fullRoster by remember { mutableStateOf<List<DynastyPlayer>>(emptyList()) }
    var cardPlayer by remember { mutableStateOf<DynastyPlayer?>(null) }
    var fullScreenPlayer by remember { mutableStateOf<DynastyPlayer?>(null) }
    val context = LocalContext.current
    val app = context.requireDynastyApplication()
    val playerStatsRepository = app.playerStatsRepository
    val newsFeed = rememberShellNewsFeed(
        dynastyId = loaded.dynastyId,
        seasonIndex = seasonIndex,
        weekIndex = position.weekIndex,
        newsFeedRepository = app.newsFeedRepository,
        rosterRepository = rosterRepository,
    )
    val scope = rememberCoroutineScope()
    val seasonIndex = loaded.metadata.seasonIndex
    val schoolName = loaded.summary.schoolDisplayName.orEmpty()
    val position = loaded.weekCheckpoint.activePosition

    LaunchedEffect(loaded.dynastyId, seasonIndex) {
        carousel = repository.loadOrCreate(loaded.dynastyId, seasonIndex)
        fullRoster = rosterRepository.listDynastyPlayers(loaded.dynastyId)
    }

    val state = carousel ?: return

    val menu = buildDynastyHubMenu(
        position = position,
        callbacks = hubMenuCallbacks,
        recruitingCycleClosed = RecruitingCyclePolicy.recruitingMenuTreatsCycleClosed(
            CalendarPhase.Offseason,
            weekIndex = null,
            ledger = null,
        ),
        onRecruitingCycleClosed = {},
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
            seasonIndex = seasonIndex,
            playerStatsRepository = playerStatsRepository,
            allowEdit = false,
            onBack = { fullScreenPlayer = null },
        )
        return
    }

    DynastyShellChrome(
        schoolDisplayName = schoolName,
        phaseWeekLabel = stringResource(R.string.carousel_shell_phase_label),
        headerSubtitle = stringResource(R.string.carousel_subtitle),
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
                seasonIndex = seasonIndex,
                activePosition = position,
                calendarRepository = userCalendarRepository,
                contentPadding = padding,
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
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DashboardRequiredTasksPanel(
                    tasks = CarouselDashboardTasks.build(state),
                    onOpenSigningDay = { _: DashboardDeepLink -> },
                )
                CoachingCarouselDashboardContent(
                    loaded = loaded,
                    state = state,
                    repository = repository,
                    onStateChange = { carousel = it },
                    onComplete = onComplete,
                    scope = scope,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}

@Composable
private fun CoachingCarouselDashboardContent(
    loaded: LoadedDynasty,
    state: CoachingCarouselState,
    repository: CoachingCarouselRepository,
    onStateChange: (CoachingCarouselState) -> Unit,
    onComplete: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val seasonIndex = loaded.metadata.seasonIndex

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.carousel_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        state.backgroundSimSummary?.let { summary ->
            Text(
                text = stringResource(
                    R.string.carousel_league_churn_summary,
                    summary.aiFired,
                    summary.aiExtended,
                    summary.aiHired,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (state.status) {
            CarouselStatus.AwaitingBackgroundSim -> {
                Text(
                    text = stringResource(R.string.carousel_background_hint),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    onClick = {
                        scope.launch {
                            val next = repository.runBackgroundSim(loaded.dynastyId, seasonIndex, state)
                            repository.save(loaded.dynastyId, seasonIndex, next)
                            onStateChange(next)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.carousel_sim_league))
                }
            }
            CarouselStatus.AwaitingUserApprovals -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    items(state.pendingApprovals, key = { it.id }) { card ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(card.headline, fontWeight = FontWeight.SemiBold)
                                Text(card.detail, style = MaterialTheme.typography.bodyMedium)
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val next = CoachingCarouselEngine.resolveApproval(state, card.id, false)
                                            repository.save(loaded.dynastyId, seasonIndex, next)
                                            onStateChange(next)
                                            if (next.canEnterOffseasonWeekOne()) onComplete()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.carousel_decline))
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val next = CoachingCarouselEngine.resolveApproval(state, card.id, true)
                                            repository.save(loaded.dynastyId, seasonIndex, next)
                                            onStateChange(next)
                                            if (next.canEnterOffseasonWeekOne()) onComplete()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.carousel_accept))
                                }
                            }
                        }
                    }
                }
            }
            CarouselStatus.Complete, CarouselStatus.NotRequired -> {
                Text(text = stringResource(R.string.carousel_complete))
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.carousel_continue_offseason))
                }
            }
        }
    }
}

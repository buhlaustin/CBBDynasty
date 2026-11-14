package dev.novux.dynasty.cbb.ui.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.news.NewsHeadline
import dev.novux.dynasty.cbb.core.news.NewsHubPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubStoriesRowPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalFiltersPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalHumanRowPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalRecentSectionsPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalRowNavigationPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalTabVisibilityPolicy
import dev.novux.dynasty.cbb.core.offseason.weektwo.NbaDraftOutcome
import dev.novux.dynasty.cbb.core.offseason.weektwo.NbaDraftResultLine
import dev.novux.dynasty.cbb.core.pack.IdentityPackIndex
import dev.novux.dynasty.cbb.core.portal.PortalCycleState
import dev.novux.dynasty.cbb.core.signingday.NationalRankRow
import dev.novux.dynasty.cbb.data.news.NewsFeedRepository
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekTwoRepository
import dev.novux.dynasty.cbb.data.pack.PackRepository
import dev.novux.dynasty.cbb.data.portal.PortalCycleRepository
import dev.novux.dynasty.cbb.data.preseason.IncomingClassSeedRepository

enum class NewsHubTab { Stories, Portal, Recruits, Draft }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsHubScreen(
    dynastyId: String,
    seasonIndex: Int,
    position: CalendarPosition,
    newsFeedRepository: NewsFeedRepository,
    incomingClassSeedRepository: IncomingClassSeedRepository,
    portalCycleRepository: PortalCycleRepository,
    offseasonWeekTwoRepository: OffseasonWeekTwoRepository,
    packRepository: PackRepository,
    onBack: () -> Unit,
    onOpenRecruitingPortal: () -> Unit,
    onOpenPriorProgram: (String) -> Unit,
    onOpenPortalEntrantFocus: (String) -> Unit,
    initialTab: NewsHubTab = NewsHubTab.Stories,
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(0) }
    var selectedStory by remember { mutableStateOf<NewsHeadline?>(null) }
    var stories by remember { mutableStateOf<List<NewsHeadline>>(emptyList()) }
    var recruits by remember { mutableStateOf<List<NationalRankRow>>(emptyList()) }
    var portalState by remember { mutableStateOf<PortalCycleState?>(null) }
    var draftLines by remember { mutableStateOf<List<NbaDraftResultLine>>(emptyList()) }
    var packIndex by remember { mutableStateOf<IdentityPackIndex?>(null) }
    val portalVisible = NewsHubPolicy.portalTabVisible(
        position.phase,
        position.weekIndex.takeIf { position.phase == CalendarPhase.Offseason },
    )
    val visibleTabs = remember(portalVisible) {
        buildList {
            add(NewsHubTab.Stories)
            if (NewsHubPortalTabVisibilityPolicy.showPortalTab(portalVisible)) {
                add(NewsHubTab.Portal)
            }
            add(NewsHubTab.Recruits)
            add(NewsHubTab.Draft)
        }
    }

    LaunchedEffect(visibleTabs.size, initialTab) {
        if (selectedTab >= visibleTabs.size) {
            selectedTab = 0
        }
        val launchIndex = visibleTabs.indexOf(initialTab)
        if (launchIndex >= 0) {
            selectedTab = launchIndex
        }
    }

    NewsHeadlineStoryDetailSheet(
        headline = selectedStory,
        onDismiss = { selectedStory = null },
        onOpenNewsHubPortal = {
            selectedStory = null
            val portalIndex = visibleTabs.indexOf(NewsHubTab.Portal)
            if (portalIndex >= 0) {
                selectedTab = portalIndex
            }
        },
    )

    LaunchedEffect(dynastyId, seasonIndex, position) {
        packIndex = packRepository.activeSession().index
        stories = newsFeedRepository.storiesFeed(dynastyId, seasonIndex)
        recruits = incomingClassSeedRepository.loadHeadline(dynastyId, seasonIndex)?.top60.orEmpty()
        if (portalVisible) {
            portalState = portalCycleRepository.loadOrSimulate(dynastyId, seasonIndex)
        }
        draftLines = offseasonWeekTwoRepository.loadPublishedResults(dynastyId, seasonIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.news_hub_title)) })
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.dynasty_shell_back))
        }
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            visibleTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tabLabel(tab)) },
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (visibleTabs.getOrNull(selectedTab)) {
                NewsHubTab.Stories -> StoriesTab(
                    stories = stories,
                    onStoryClick = { selectedStory = it },
                )
                NewsHubTab.Portal -> PortalTab(
                    portalState = portalState,
                    packIndex = packIndex,
                    onOpenRecruitingPortal = onOpenRecruitingPortal,
                    onOpenPriorProgram = onOpenPriorProgram,
                    onOpenPortalEntrantFocus = onOpenPortalEntrantFocus,
                )
                NewsHubTab.Recruits -> RecruitsTab(recruits)
                NewsHubTab.Draft -> DraftTab(draftLines)
                null -> Unit
            }
        }
    }
}

@Composable
private fun tabLabel(tab: NewsHubTab): String = when (tab) {
    NewsHubTab.Stories -> stringResource(R.string.news_hub_tab_stories)
    NewsHubTab.Portal -> stringResource(R.string.news_hub_tab_portal)
    NewsHubTab.Recruits -> stringResource(R.string.news_hub_tab_recruits)
    NewsHubTab.Draft -> stringResource(R.string.news_hub_tab_draft)
}

@Composable
private fun StoriesTab(
    stories: List<NewsHeadline>,
    onStoryClick: (NewsHeadline) -> Unit,
) {
    if (stories.isEmpty()) {
        Text(stringResource(R.string.news_hub_stories_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    stories.forEach { story ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStoryClick(story) },
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (NewsHubStoriesRowPolicy.showKindChip()) {
                    Text(
                        text = newsHeadlineKindLabel(story.kind),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(story.title, style = MaterialTheme.typography.bodyLarge)
                if (NewsHubStoriesRowPolicy.showWeekIndex(story)) {
                    Text(
                        text = stringResource(R.string.news_hub_story_week, story.rsWeekIndex!!),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PortalTab(
    portalState: PortalCycleState?,
    packIndex: IdentityPackIndex?,
    onOpenRecruitingPortal: () -> Unit,
    onOpenPriorProgram: (String) -> Unit,
    onOpenPortalEntrantFocus: (String) -> Unit,
) {
    val index = packIndex ?: return
    val portal = portalState ?: return
    if (!portal.top40Locked) {
        Text(stringResource(R.string.news_hub_portal_pre_lock), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    var starFilter by remember { mutableStateOf(NewsHubPortalFiltersPolicy.StarFilter.All) }
    val top40 = NewsHubPortalRecentSectionsPolicy.top40(portal.entrants)
    val recent = NewsHubPortalRecentSectionsPolicy.recentEntrants(portal.entrants)
    val filteredTop40 = top40.filter { NewsHubPortalFiltersPolicy.matchesStars(it, starFilter) }

    Text(
        stringResource(R.string.news_hub_portal_filters_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            NewsHubPortalFiltersPolicy.StarFilter.All to R.string.news_hub_portal_filter_all,
            NewsHubPortalFiltersPolicy.StarFilter.FourPlus to R.string.news_hub_portal_filter_four_plus,
            NewsHubPortalFiltersPolicy.StarFilter.FiveOnly to R.string.news_hub_portal_filter_five,
        ).forEach { (filter, labelRes) ->
            AssistChip(
                onClick = { starFilter = filter },
                label = { Text(stringResource(labelRes)) },
            )
        }
    }
    Text(
        stringResource(R.string.news_hub_portal_top40_heading),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    filteredTop40.forEach { entrant ->
        PortalEntrantRow(
            entrant = entrant,
            packIndex = index,
            onOpenPriorProgram = onOpenPriorProgram,
            onOpenPortalEntrantFocus = onOpenPortalEntrantFocus,
        )
    }
    if (recent.isNotEmpty()) {
        Text(
            stringResource(R.string.news_hub_portal_recent_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp),
        )
        recent.forEach { entrant ->
            PortalEntrantRow(
                entrant = entrant,
                packIndex = index,
                onOpenPriorProgram = onOpenPriorProgram,
                onOpenPortalEntrantFocus = onOpenPortalEntrantFocus,
            )
        }
    }
    Button(
        onClick = onOpenRecruitingPortal,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Text(stringResource(R.string.news_hub_portal_add_to_board))
    }
}

@Composable
private fun PortalEntrantRow(
    entrant: dev.novux.dynasty.cbb.core.portal.PortalEntrant,
    packIndex: IdentityPackIndex,
    onOpenPriorProgram: (String) -> Unit,
    onOpenPortalEntrantFocus: (String) -> Unit,
) {
    val labels = NewsHubPortalHumanRowPolicy.rowLabels(entrant, packIndex)
    val rank = entrant.nationalTop40Rank ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(
                R.string.news_hub_portal_row,
                rank,
                labels.playerDisplayName,
                labels.priorSchoolDisplayName,
                labels.stars,
                labels.positionLabel,
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onOpenPortalEntrantFocus(
                        NewsHubPortalRowNavigationPolicy.focusProspectIdForRecruiting(entrant),
                    )
                },
        )
        OutlinedButton(
            onClick = {
                onOpenPriorProgram(
                    NewsHubPortalRowNavigationPolicy.priorProgramIdForNavigation(entrant),
                )
            },
        ) {
            Text(stringResource(R.string.news_hub_portal_prior_program))
        }
    }
}

@Composable
private fun RecruitsTab(rows: List<NationalRankRow>) {
    Text(
        stringResource(R.string.news_hub_recruits_heading),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        stringResource(R.string.news_hub_recruits_scope),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    rows.forEach { row ->
        val p = row.prospect
        Text(
            stringResource(
                R.string.incoming_seed_top60_line,
                row.rank,
                p.displayName,
                p.stars,
                p.position.name,
                p.homeState,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DraftTab(lines: List<NbaDraftResultLine>) {
    if (lines.isEmpty()) {
        Text(stringResource(R.string.news_hub_draft_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    lines.forEach { line ->
        val outcome = when (line.outcome) {
            NbaDraftOutcome.Drafted -> stringResource(
                R.string.draft_results_outcome_drafted,
                line.pickSummary.orEmpty(),
            )
            NbaDraftOutcome.UndraftedGraduate -> stringResource(R.string.draft_results_outcome_undrafted)
            NbaDraftOutcome.ReturnedToRoster -> stringResource(R.string.draft_results_outcome_returned)
        }
        Text(
            stringResource(R.string.news_hub_draft_row, line.displayLabel, line.mockTier, outcome),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
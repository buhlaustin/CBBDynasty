package dev.novux.dynasty.cbb.ui.dynasty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.news.NewsHeadline
import dev.novux.dynasty.cbb.core.news.NewsStoryDrillInPolicy
import dev.novux.dynasty.cbb.ui.news.NewsHeadlineStoryDetailSheet
import dev.novux.dynasty.cbb.core.news.NewsHeadlineKind
import dev.novux.dynasty.cbb.core.news.DynastyShellNewsHeadlinesPolicy
import dev.novux.dynasty.cbb.core.news.ShallowNewsFeedSnapshot
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.roster.ShellRosterSection
import dev.novux.dynasty.cbb.core.roster.ShellRosterSectionPartition
import dev.novux.dynasty.cbb.ui.player.PlayerInjuryRosterRow
import dev.novux.dynasty.cbb.ui.player.PlayerLoyaltyChips

@Composable
fun DynastyShellRosterTab(
    players: List<DynastyPlayer>,
    contentPadding: PaddingValues,
    onPlayerClick: (DynastyPlayer) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.shell_roster_tab_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (players.isEmpty()) {
            Text(
                text = stringResource(R.string.shell_roster_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val rosterPlayers = players.map { it.toRosterPlayer() }
            ShellRosterSectionPartition.group(rosterPlayers).forEach { section ->
                Text(
                    text = shellRosterSectionTitle(section.section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                val dynastyById = players.associateBy { it.playerId }
                section.players.forEach { rosterPlayer ->
                    val player = dynastyById.getValue(rosterPlayer.playerId)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayerClick(player) },
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            PlayerInjuryRosterRow(player = rosterPlayer)
                            PlayerLoyaltyChips(player)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun shellRosterSectionTitle(section: ShellRosterSection): String = when (section) {
    ShellRosterSection.Starters -> stringResource(R.string.shell_roster_section_starters)
    ShellRosterSection.Reserves -> stringResource(R.string.shell_roster_section_reserves)
    ShellRosterSection.BenchWarmers -> stringResource(R.string.shell_roster_section_bench)
}

@Composable
fun DynastyShellCalendarTabPlaceholder(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.shell_tab_calendar),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.shell_calendar_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DynastyShellNewsTab(
    feed: ShallowNewsFeedSnapshot,
    contentPadding: PaddingValues,
    onOpenNewsHub: (() -> Unit)? = null,
    onOpenNewsHubPortal: (() -> Unit)? = null,
    emptyHint: String? = null,
) {
    val headlines = feed.headlines
    var selectedHeadline by remember { mutableStateOf<NewsHeadline?>(null) }
    NewsHeadlineStoryDetailSheet(
        headline = selectedHeadline,
        onDismiss = { selectedHeadline = null },
        onOpenNewsHub = onOpenNewsHub,
        onOpenNewsHubPortal = onOpenNewsHubPortal,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.shell_tab_news),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.shell_news_headlines_only_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.shell_news_shallow_cap, feed.cap),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (headlines.isEmpty()) {
            Text(
                text = emptyHint ?: stringResource(R.string.shell_news_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            headlines.forEach { headline ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (NewsStoryDrillInPolicy.opensMiniArticleDetail()) {
                                selectedHeadline = headline
                            }
                        },
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = newsKindLabel(headline.kind),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(text = headline.title, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (feed.truncated) {
                Text(
                    text = stringResource(
                        R.string.shell_news_more_in_hub,
                        feed.mergedCount - feed.headlines.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            onOpenNewsHub?.let { open ->
                OutlinedButton(onClick = open, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.shell_news_open_hub))
                }
            }
        }
    }
}

@Composable
private fun newsKindLabel(kind: NewsHeadlineKind): String = when (kind) {
    NewsHeadlineKind.UserInjury -> stringResource(R.string.shell_news_kind_user_injury)
    NewsHeadlineKind.LeagueInjury -> stringResource(R.string.shell_news_kind_league_injury)
    NewsHeadlineKind.LeagueResult -> stringResource(R.string.shell_news_kind_league_result)
    NewsHeadlineKind.RecruitingSpotlight -> stringResource(R.string.shell_news_kind_recruiting)
    NewsHeadlineKind.General -> stringResource(R.string.shell_news_kind_general)
    NewsHeadlineKind.Awards -> stringResource(R.string.shell_news_kind_awards)
    NewsHeadlineKind.Bracketology -> stringResource(R.string.shell_news_kind_bracketology)
}

@Composable
fun DynastyShellNewsTabPlaceholder(contentPadding: PaddingValues) {
    DynastyShellNewsTab(feed = ShallowNewsFeedSnapshot.Empty, contentPadding = contentPadding)
}

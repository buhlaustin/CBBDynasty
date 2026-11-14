package dev.novux.dynasty.cbb.ui.rankings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.rankings.ConferenceStandingsGroup
import dev.novux.dynasty.cbb.core.rankings.RankingsHubSnapshot
import dev.novux.dynasty.cbb.core.rankings.RankingsHubTeamRow
import dev.novux.dynasty.cbb.core.rankings.RankingsHubUiPolicy

private enum class RankingsHubTab { Net, Media, Coaches, Conference }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingsHubScreen(
    snapshot: RankingsHubSnapshot,
    onBack: () -> Unit,
    onPublishPreseason: (() -> Unit)? = null,
) {
    var tabIndex by remember { mutableIntStateOf(RankingsHubUiPolicy.DEFAULT_TAB_INDEX) }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(stringResource(R.string.rankings_hub_title))
                    Text(
                        text = stringResource(R.string.rankings_hub_scope),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.dynasty_shell_back))
        }
        if (!snapshot.published) {
            RankingsHubUnpublishedPlaceholder(
                snapshot = snapshot,
                onPublishPreseason = onPublishPreseason,
            )
            return
        }
        PrimaryTabRow(selectedTabIndex = tabIndex) {
            RankingsHubTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(tabLabel(tab)) },
                )
            }
        }
        when (RankingsHubTab.entries[tabIndex]) {
            RankingsHubTab.Net -> NationalRankList(snapshot.net)
            RankingsHubTab.Media -> NationalRankList(snapshot.media)
            RankingsHubTab.Coaches -> NationalRankList(snapshot.coaches)
            RankingsHubTab.Conference -> ConferenceStandingsList(snapshot.conferences)
        }
    }
}

@Composable
private fun RankingsHubUnpublishedPlaceholder(
    snapshot: RankingsHubSnapshot,
    onPublishPreseason: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.rankings_hub_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onPublishPreseason != null) {
            Text(
                text = stringResource(R.string.rankings_hub_preseason_review),
                style = MaterialTheme.typography.bodyMedium,
            )
            NationalRankList(snapshot.net.take(RankingsHubUiPolicy.UNPUBLISHED_PREVIEW_CAP))
            Button(
                onClick = onPublishPreseason,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.rankings_hub_preseason_continue))
            }
        }
    }
}

@Composable
private fun tabLabel(tab: RankingsHubTab): String = when (tab) {
    RankingsHubTab.Net -> stringResource(R.string.rankings_tab_net)
    RankingsHubTab.Media -> stringResource(R.string.rankings_tab_media)
    RankingsHubTab.Coaches -> stringResource(R.string.rankings_tab_coaches)
    RankingsHubTab.Conference -> stringResource(R.string.rankings_tab_conference)
}

@Composable
private fun NationalRankList(rows: List<RankingsHubTeamRow>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(rows, key = { it.teamId }) { row ->
            val weight = if (row.top25Highlight) FontWeight.SemiBold else FontWeight.Normal
            val color = if (row.top25Highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            Text(
                text = stringResource(R.string.rankings_hub_team_row, row.rank, row.displayName),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = weight,
                color = color,
            )
        }
    }
}

@Composable
private fun ConferenceStandingsList(groups: List<ConferenceStandingsGroup>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(groups, key = { it.conferenceId }) { group ->
            Text(
                group.conferenceName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            group.teams.forEach { team ->
                Text(
                    stringResource(
                        R.string.rankings_hub_conf_row,
                        team.displayName,
                        team.wins,
                        team.losses,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

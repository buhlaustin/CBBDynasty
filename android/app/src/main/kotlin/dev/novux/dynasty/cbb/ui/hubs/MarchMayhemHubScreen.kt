package dev.novux.dynasty.cbb.ui.hubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.march.MarchMayhemGameRow
import dev.novux.dynasty.cbb.core.march.MarchMayhemHubSnapshot
import dev.novux.dynasty.cbb.core.march.MarchMayhemInvitationalPanel
import dev.novux.dynasty.cbb.core.march.MarchMayhemNcbaPanel
import dev.novux.dynasty.cbb.core.march.MarchMayhemOverviewPanel
import dev.novux.dynasty.cbb.core.march.MarchMayhemProgressRail
import dev.novux.dynasty.cbb.core.march.MarchMayhemTeamRow

private enum class MarchMayhemTab { Overview, Ncba, Nci, Heritage }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarchMayhemHubScreen(
    snapshot: MarchMayhemHubSnapshot,
    onBack: () -> Unit,
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = MarchMayhemTab.entries

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(stringResource(R.string.march_mayhem_hub_title))
                    Text(
                        text = stringResource(R.string.march_mayhem_hub_subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.dynasty_shell_back))
        }
        if (!snapshot.selectionPublished) {
            Text(
                text = stringResource(R.string.march_mayhem_hub_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            PrimaryTabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = {
                            Text(
                                when (tab) {
                                    MarchMayhemTab.Overview -> stringResource(R.string.march_mayhem_tab_overview)
                                    MarchMayhemTab.Ncba -> stringResource(R.string.march_mayhem_tab_ncba)
                                    MarchMayhemTab.Nci -> stringResource(R.string.march_mayhem_tab_nci)
                                    MarchMayhemTab.Heritage -> stringResource(R.string.march_mayhem_tab_heritage)
                                },
                            )
                        },
                    )
                }
            }
            when (tabs[tabIndex]) {
                MarchMayhemTab.Overview -> OverviewTab(snapshot.overview, snapshot.userTeamId)
                MarchMayhemTab.Ncba -> NcbaTab(snapshot.ncba, snapshot.userTeamId)
                MarchMayhemTab.Nci -> InvitationalTab(snapshot.nci, snapshot.userTeamId)
                MarchMayhemTab.Heritage -> InvitationalTab(snapshot.heritage, snapshot.userTeamId)
            }
        }
    }
}

@Composable
private fun OverviewTab(overview: MarchMayhemOverviewPanel, userTeamId: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.march_mayhem_overview_recap, overview.selectionFieldCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        overview.rails.firstOrNull { it.openingRoundActive }?.let { rail ->
            item {
                OpeningRoundStrip(rail)
            }
        }
        items(overview.rails, key = { it.eventLabel }) { rail ->
            ProgressRailCard(rail)
        }
        item {
            Text(
                text = stringResource(R.string.march_mayhem_overview_today_header),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (overview.todaysGames.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.march_mayhem_overview_no_scores),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(overview.todaysGames, key = { "${it.postseasonWeek}:${it.homeTeamId}:${it.awayTeamId}" }) { game ->
                GameLine(game, userTeamId)
            }
        }
    }
}

@Composable
private fun OpeningRoundStrip(rail: MarchMayhemProgressRail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.march_mayhem_opening_strip_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    R.string.march_mayhem_overview_opening_strip,
                    rail.openingAdvancerCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ProgressRailCard(rail: MarchMayhemProgressRail) {
    val targetGames = (rail.fieldSize - 1).coerceAtLeast(1)
    val progress = (rail.gamesPlayed.toFloat() / targetGames).coerceIn(0f, 1f)
    val userSuffix = if (rail.userInField) {
        stringResource(
            if (rail.userTeamAlive) {
                R.string.march_mayhem_overview_rail_you_alive
            } else {
                R.string.march_mayhem_overview_rail_you_out
            },
        )
    } else {
        ""
    }
    val championSuffix = rail.championDisplayName?.let {
        stringResource(R.string.march_mayhem_overview_rail_champion, it)
    }.orEmpty()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(
                    R.string.march_mayhem_overview_rail,
                    rail.eventLabel,
                    rail.fieldSize,
                    rail.gamesPlayed,
                    championSuffix + userSuffix,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NcbaTab(panel: MarchMayhemNcbaPanel, userTeamId: String) {
    var myTeamOnly by remember { mutableStateOf(false) }
    EventTabScaffold(myTeamOnly = myTeamOnly, onMyTeamOnlyChange = { myTeamOnly = it }) {
        if (panel.openingSimmed && panel.openingAdvancers.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.march_mayhem_opening_advancers, panel.openingAdvancers.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(
                panel.openingAdvancers.filter { !myTeamOnly || it.isUserTeam },
                key = { "or:${it.teamId}" },
            ) { row ->
                TeamLine(row)
            }
        }
        item {
            Text(
                text = stringResource(R.string.march_mayhem_ncba_field, panel.field.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        panel.champion?.let { champion ->
            item {
                Text(
                    text = stringResource(R.string.march_mayhem_champion_line, champion.seedLabel, champion.displayName),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (panel.regionalChampions.isNotEmpty()) {
            items(panel.regionalChampions, key = { "rc:${it.teamId}" }) { row ->
                TeamLine(row)
            }
        }
        items(
            panel.field.filter { !myTeamOnly || it.isUserTeam },
            key = { it.teamId },
        ) { row ->
            TeamLine(row)
        }
        GameResultsSection(
            games = panel.games,
            userTeamId = userTeamId,
            myTeamOnly = myTeamOnly,
        )
    }
}

@Composable
private fun InvitationalTab(panel: MarchMayhemInvitationalPanel, userTeamId: String) {
    var myTeamOnly by remember { mutableStateOf(false) }
    EventTabScaffold(myTeamOnly = myTeamOnly, onMyTeamOnlyChange = { myTeamOnly = it }) {
        item {
            Text(
                text = stringResource(R.string.march_mayhem_invitational_field, panel.tournamentShortLabel, panel.field.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        panel.champion?.let { champion ->
            item {
                Text(
                    text = stringResource(R.string.march_mayhem_champion_line, champion.seedLabel, champion.displayName),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        items(
            panel.field.filter { !myTeamOnly || it.isUserTeam },
            key = { it.teamId },
        ) { row ->
            TeamLine(row)
        }
        GameResultsSection(
            games = panel.games,
            userTeamId = userTeamId,
            myTeamOnly = myTeamOnly,
        )
    }
}

@Composable
private fun EventTabScaffold(
    myTeamOnly: Boolean,
    onMyTeamOnlyChange: (Boolean) -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                RowFilter(myTeamOnly, onMyTeamOnlyChange)
            }
        }
        content()
    }
}

@Composable
private fun RowFilter(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.march_mayhem_filter_my_team),
            style = MaterialTheme.typography.labelLarge,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.GameResultsSection(
    games: List<MarchMayhemGameRow>,
    userTeamId: String,
    myTeamOnly: Boolean,
) {
    val visible = games.filter { game ->
        !myTeamOnly || game.homeTeamId == userTeamId || game.awayTeamId == userTeamId
    }
    if (visible.isNotEmpty()) {
        item {
            Text(
                text = stringResource(R.string.march_mayhem_results_header),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(visible, key = { "${it.postseasonWeek}:${it.homeTeamId}:${it.awayTeamId}" }) { game ->
            GameLine(game, userTeamId)
        }
    }
}

@Composable
private fun TeamLine(row: MarchMayhemTeamRow) {
    val suffix = buildString {
        if (row.isUserTeam) append(" •")
        row.regionLabel?.let { append(" · $it") }
    }
    Text(
        text = stringResource(
            R.string.march_mayhem_team_line,
            row.seedLabel,
            row.displayName,
            suffix,
        ),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (row.isUserTeam) FontWeight.SemiBold else FontWeight.Normal,
    )
}

@Composable
private fun GameLine(game: MarchMayhemGameRow, userTeamId: String) {
    val userInvolved = game.homeTeamId == userTeamId || game.awayTeamId == userTeamId
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AssistChip(
            onClick = {},
            label = { Text(stringResource(R.string.march_mayhem_game_week, game.postseasonWeek)) },
        )
        Text(
            text = stringResource(
                R.string.march_mayhem_score_line,
                game.homeSeedLabel ?: "",
                game.homeDisplayName,
                game.homeScore,
                game.awaySeedLabel ?: "",
                game.awayDisplayName,
                game.awayScore,
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (userInvolved) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

package dev.novux.dynasty.cbb.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import dev.novux.dynasty.cbb.core.statistics.StatisticsHubFramePolicy
import dev.novux.dynasty.cbb.core.statistics.StatLeaderRow
import dev.novux.dynasty.cbb.core.statistics.StatisticsHubSnapshot
import dev.novux.dynasty.cbb.core.statistics.StatisticsLeaderboards
import dev.novux.dynasty.cbb.core.player.stats.PlayerStatsEngine

private enum class StatisticsHubTab { National, Conference }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsHubScreen(
    snapshot: StatisticsHubSnapshot,
    onBack: () -> Unit,
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    var selectedConferenceId by remember { mutableStateOf(snapshot.userConferenceId) }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.statistics_hub_title)) })
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.dynasty_shell_back))
        }
        if (!StatisticsHubFramePolicy.showLiveLeaderboards(snapshot)) {
            Text(
                text = stringResource(R.string.statistics_hub_empty_season),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
            )
            return
        }
        Text(
            text = stringResource(R.string.statistics_hub_cadence_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        PrimaryTabRow(selectedTabIndex = tabIndex) {
            StatisticsHubTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = {
                        Text(
                            when (tab) {
                                StatisticsHubTab.National -> stringResource(R.string.statistics_hub_tab_national)
                                StatisticsHubTab.Conference -> stringResource(R.string.statistics_hub_tab_conference)
                            },
                        )
                    },
                )
            }
        }
        when (StatisticsHubTab.entries[tabIndex]) {
            StatisticsHubTab.National -> LeaderboardsBody(snapshot.national)
            StatisticsHubTab.Conference -> {
                ConferencePicker(
                    conferences = snapshot.conferences,
                    selectedConferenceId = selectedConferenceId,
                    onSelected = { selectedConferenceId = it },
                )
                val boards = snapshot.conferenceLeadersById[selectedConferenceId]
                if (boards == null) {
                    Text(
                        text = stringResource(R.string.statistics_hub_no_conference),
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    LeaderboardsBody(boards)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConferencePicker(
    conferences: List<dev.novux.dynasty.cbb.core.statistics.ConferencePickerRow>,
    selectedConferenceId: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = conferences.firstOrNull { it.conferenceId == selectedConferenceId }?.displayName
        ?: selectedConferenceId
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.statistics_hub_conference_picker)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            conferences.forEach { conf ->
                DropdownMenuItem(
                    text = { Text(conf.displayName) },
                    onClick = {
                        onSelected(conf.conferenceId)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LeaderboardsBody(boards: StatisticsLeaderboards) {
    val ppg = stringResource(R.string.statistics_hub_cat_ppg)
    val apg = stringResource(R.string.statistics_hub_cat_apg)
    val rpg = stringResource(R.string.statistics_hub_cat_rpg)
    val spg = stringResource(R.string.statistics_hub_cat_spg)
    val bpg = stringResource(R.string.statistics_hub_cat_bpg)
  val noQual = stringResource(R.string.statistics_hub_no_qualifiers)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        leaderSection(title = ppg, emptyLabel = noQual, rows = boards.points)
        leaderSection(title = apg, emptyLabel = noQual, rows = boards.assists)
        leaderSection(title = rpg, emptyLabel = noQual, rows = boards.rebounds)
        leaderSection(title = spg, emptyLabel = noQual, rows = boards.steals)
        leaderSection(title = bpg, emptyLabel = noQual, rows = boards.blocks)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.leaderSection(
    title: String,
    emptyLabel: String,
    rows: List<StatLeaderRow>,
) {
    item {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
    if (rows.isEmpty()) {
        item {
            Text(
                emptyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        items(rows, key = { "${title}:${it.playerId}" }) { row ->
            LeaderLine(row)
        }
    }
}

@Composable
private fun LeaderLine(row: StatLeaderRow) {
    Text(
        text = stringResource(
            R.string.statistics_hub_leader_line,
            row.rank,
            row.playerDisplayName,
            row.teamDisplayName,
            PlayerStatsEngine.formatPerGame(row.valuePerGame),
            row.gamesPlayed,
        ),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (row.isUserRosterPlayer) FontWeight.SemiBold else FontWeight.Normal,
    )
}

package dev.novux.dynasty.cbb.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
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
import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear
import dev.novux.dynasty.cbb.core.history.HistoryHubSnapshot
import dev.novux.dynasty.cbb.core.history.HistoryHubDepthPolicy
import dev.novux.dynasty.cbb.core.history.HistoryHubSurfacePolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryHubScreen(
    snapshot: HistoryHubSnapshot,
    onBack: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.history_hub_title)) })
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.dynasty_shell_back))
        }
        Text(
            text = stringResource(R.string.history_hub_cadence_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Text(
            text = stringResource(R.string.history_hub_online_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
        )
        ScrollableTabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.history_tab_goat)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.history_tab_archive)) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.history_tab_records)) })
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (tab) {
                0 -> {
                    if (snapshot.goatOverall.isEmpty()) {
                        Text(
                            stringResource(R.string.history_goat_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        snapshot.goatOverall.forEach { entry ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        "#${entry.rank} ${entry.displayName}",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(entry.schoolLabel, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        stringResource(
                                            R.string.history_goat_line,
                                            entry.goatScore,
                                            entry.careerWinShares,
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    if (entry.chips.isNotEmpty()) {
                                        Text(entry.chips.joinToString(" · "), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (snapshot.yearCards.isEmpty()) {
                        Text(
                            stringResource(R.string.history_archive_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        snapshot.yearCards.asReversed().forEach { card ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        SeasonMembershipYear.label(card.seasonIndex),
                                        fontWeight = FontWeight.Bold,
                                    )
                                    HorizontalDivider()
                                    card.ncbaChampionSchool?.let {
                                        Text(stringResource(R.string.history_ncba_champion, it))
                                    }
                                    card.ncbaMvpLine?.let {
                                        Text(stringResource(R.string.history_ncba_mvp, it))
                                    }
                                    card.goatNumberOne?.let {
                                        Text(stringResource(R.string.history_goat_one, it))
                                    }
                                    if (card.nationalHofInductees.isNotEmpty()) {
                                        Text(
                                            stringResource(
                                                R.string.history_hof_class,
                                                card.nationalHofInductees.joinToString(),
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    val lines = HistoryHubDepthPolicy.majorRecordFeedLines(snapshot)
                    if (lines.isEmpty()) {
                        Text(
                            stringResource(R.string.history_records_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        lines.forEach { line ->
                            Card(Modifier.fillMaxWidth()) {
                                Text(
                                    line,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

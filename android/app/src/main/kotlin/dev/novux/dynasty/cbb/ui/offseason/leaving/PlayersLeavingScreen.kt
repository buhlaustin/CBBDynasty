package dev.novux.dynasty.cbb.ui.offseason.leaving

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.offseason.leaving.PlayerLeavingRetentionPolicy
import dev.novux.dynasty.cbb.core.offseason.weekone.WeekOneLeavingTabPolicy
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersLeavingPanel(
    dynastyId: String,
    rosterRepository: DynastyRosterRepository,
    modifier: Modifier = Modifier,
    onPlayersChanged: (List<RosterPlayer>) -> Unit = {},
    navigationRequest: WeekOneLeavingNavigation? = null,
    onNavigationRequestConsumed: () -> Unit = {},
    nilRetentionHost: WeekOneNilRetentionHost? = null,
) {
    var players by remember { mutableStateOf<List<RosterPlayer>>(emptyList()) }
    var nilRetentionTarget by remember { mutableStateOf<RosterPlayer?>(null) }
    var tabIndex by remember { mutableIntStateOf(0) }
    var proDaysHighlighted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dynastyId) {
        players = rosterRepository.prepareWeekOneLeaving(dynastyId)
        onPlayersChanged(players)
    }

    LaunchedEffect(navigationRequest) {
        val nav = navigationRequest ?: return@LaunchedEffect
        tabIndex = nav.tabIndex
        proDaysHighlighted = nav.emphasizeProDays
        onNavigationRequestConsumed()
    }

    @Suppress("unused")
    val weekOneTabsQa = WeekOneLeavingTabPolicy.MEGA_FRAME_ID
    @Suppress("unused")
    val retentionQa = PlayerLeavingRetentionPolicy.MEGA_ID
    val rosterTab = rosterRepository.rosterTab(players)
    val transferTab = rosterRepository.transferTab(players)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.leaving_week_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.leaving_week_tabs_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrimaryTabRow(selectedTabIndex = tabIndex) {
            Tab(
                selected = tabIndex == WeekOneLeavingTabPolicy.ROSTER_TAB_INDEX,
                onClick = { tabIndex = WeekOneLeavingTabPolicy.ROSTER_TAB_INDEX },
                text = {
                    Text(
                        stringResource(
                            R.string.leaving_tab_roster_count,
                            rosterTab.size,
                        ),
                    )
                },
            )
            Tab(
                selected = tabIndex == WeekOneLeavingTabPolicy.TRANSFER_TAB_INDEX,
                onClick = { tabIndex = WeekOneLeavingTabPolicy.TRANSFER_TAB_INDEX },
                text = {
                    Text(
                        stringResource(
                            R.string.leaving_tab_transfer_count,
                            transferTab.size,
                        ),
                    )
                },
            )
        }
        val onRosterTab = tabIndex == WeekOneLeavingTabPolicy.ROSTER_TAB_INDEX
        val visible = if (onRosterTab) rosterTab else transferTab
        val proDaysModifier = if (proDaysHighlighted) {
            Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(4.dp)
        } else {
            Modifier.fillMaxWidth()
        }
        ProDaysPanel(
            players = players,
            rosterRepository = rosterRepository,
            dynastyId = dynastyId,
            onPlayersChanged = {
                players = it
                onPlayersChanged(it)
            },
            modifier = proDaysModifier,
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (visible.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(
                            if (onRosterTab) {
                                R.string.leaving_tab_roster_empty
                            } else {
                                R.string.leaving_tab_transfer_empty
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
            items(visible, key = { it.playerId }) { player ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(player.displayLabel(), fontWeight = FontWeight.Medium)
                        PlayerLeavingRetentionSummary(player = player.toDynastyPlayer())
                        nilRetentionHost?.let { host ->
                            OutlinedButton(
                                onClick = { nilRetentionTarget = player },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.week_one_nil_open_sheet))
                            }
                        }
                        if (onRosterTab) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val updated = rosterRepository.encourageTransfer(dynastyId, player.playerId)
                                        players = updated
                                        onPlayersChanged(updated)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.leaving_encourage_transfer))
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val updated = rosterRepository.convinceStay(
                                            dynastyId,
                                            player.playerId,
                                            stayed = true,
                                        )
                                        players = updated
                                        onPlayersChanged(updated)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.leaving_convince_stay))
                            }
                        }
                    }
                }
            }
        }
    }
    nilRetentionHost?.let { host ->
        WeekOneNilRetentionSheet(
            player = nilRetentionTarget,
            loaded = host.loaded,
            nilRules = host.nilRules,
            nilBudgetRepository = host.nilBudgetRepository,
            nilAnnualPoolRepository = host.nilAnnualPoolRepository,
            rosterRepository = rosterRepository,
            onDismiss = { nilRetentionTarget = null },
            onPlayersChanged = {
                players = it
                onPlayersChanged(it)
            },
        )
    }
}

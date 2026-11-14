package dev.novux.dynasty.cbb.ui.offseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import dev.novux.dynasty.cbb.core.offseason.weekeight.OffseasonRosterLockConstants
import dev.novux.dynasty.cbb.core.offseason.weekeight.RosterLockPolicy
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekEightRepository
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.ui.player.PlayerInjuryRosterRow
import kotlinx.coroutines.launch

@Composable
fun RosterCutsWeekEightPanel(
    dynastyId: String,
    seasonIndex: Int,
    rosterRepository: DynastyRosterRepository,
    weekEightRepository: OffseasonWeekEightRepository,
    onRosterChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var players by remember { mutableStateOf<List<RosterPlayer>>(emptyList()) }
    var walkOnsSigned by remember { mutableIntStateOf(0) }
    var rosterLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        players = rosterRepository.listPlayers(dynastyId)
        walkOnsSigned = weekEightRepository.walkOnsSigned(dynastyId, seasonIndex)
        rosterLocked = weekEightRepository.isRosterLocked(dynastyId, seasonIndex)
        onRosterChanged()
    }

    LaunchedEffect(dynastyId, seasonIndex) {
        refresh()
    }

    val atCap = players.size > OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP
    val canWalkOn = RosterLockPolicy.canOfferWalkOn(players, walkOnsSigned)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.roster_cuts_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.roster_cuts_count,
                players.size,
                OffseasonRosterLockConstants.SCHOLARSHIP_ROSTER_CAP,
                walkOnsSigned,
                OffseasonRosterLockConstants.MAX_WALK_ONS_PER_SEASON,
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.roster_cuts_walkon_tryout_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (atCap) {
            Text(
                text = stringResource(R.string.roster_cuts_over_cap),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (rosterLocked) {
            Text(
                text = stringResource(R.string.roster_cuts_locked),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(players, key = { it.playerId }) { player ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        PlayerInjuryRosterRow(player = player, modifier = Modifier.weight(1f))
                        if (!rosterLocked && player.classYear != dev.novux.dynasty.cbb.core.roster.PlayerClassYear.WalkOn) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        weekEightRepository.cutPlayer(dynastyId, player.playerId)
                                        refresh()
                                    }
                                },
                            ) {
                                Text(stringResource(R.string.roster_cuts_cut))
                            }
                        }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    weekEightRepository.offerWalkOn(dynastyId, seasonIndex)
                    refresh()
                }
            },
            enabled = canWalkOn && !rosterLocked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.roster_cuts_add_walkon))
        }
        Button(
            onClick = {
                scope.launch {
                    weekEightRepository.markRosterLocked(dynastyId, seasonIndex)
                    refresh()
                }
            },
            enabled = RosterLockPolicy.canAdvanceOffseasonWeek8(players) && !rosterLocked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.roster_cuts_lock))
        }
    }
}

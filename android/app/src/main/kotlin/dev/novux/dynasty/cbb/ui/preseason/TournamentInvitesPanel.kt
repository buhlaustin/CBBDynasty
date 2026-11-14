package dev.novux.dynasty.cbb.ui.preseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.preseason.tournament.EarlySeasonEvent
import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInviteResolution
import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInvitesPrestigePolicy
import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInvitesPrestigeTier
import dev.novux.dynasty.cbb.core.preseason.tournament.TournamentInvitesState
import dev.novux.dynasty.cbb.data.preseason.PreseasonProgressRepository
import dev.novux.dynasty.cbb.data.preseason.TournamentInvitesRepository
import kotlinx.coroutines.launch

@Composable
fun TournamentInvitesPanel(
    dynastyId: String,
    seasonIndex: Int,
    programPrestige: Int,
    invitesRepository: TournamentInvitesRepository,
    progressRepository: PreseasonProgressRepository,
    onResolved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf<TournamentInvitesState?>(null) }
    val scope = rememberCoroutineScope()
    val prestigeTier = TournamentInvitesPrestigePolicy.tier(programPrestige)

    suspend fun refresh() {
        state = invitesRepository.loadState(dynastyId, seasonIndex, programPrestige)
    }

    LaunchedEffect(dynastyId, seasonIndex, programPrestige) {
        refresh()
    }

    val inviteState = state ?: return
    val resolved = inviteState.isResolved

    suspend fun finishTask() {
        progressRepository.completeTask(dynastyId, seasonIndex, PreseasonTaskId.TournamentInvites)
        onResolved()
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.tournament_invites_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (!resolved) {
            Text(
                text = prestigeTierLabel(prestigeTier, inviteState.offers.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (resolved) {
            BudgetSummary(inviteState)
            Text(
                text = resolvedLabel(inviteState.resolution),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else if (inviteState.offers.isEmpty()) {
            Text(
                text = stringResource(R.string.tournament_invites_none),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedButton(
                onClick = {
                    scope.launch {
                        invitesRepository.acknowledgeNoInvites(dynastyId, seasonIndex)
                        refresh()
                        finishTask()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.tournament_invites_acknowledge))
            }
        } else {
            Text(
                text = when {
                    inviteState.offers.size > 1 ->
                        stringResource(R.string.tournament_invites_pick_one_multi, inviteState.offers.size)
                    else -> stringResource(R.string.tournament_invites_pick_one)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            inviteState.offers.forEach { event ->
                TournamentInviteEventCard(
                    event = event,
                    useElevated = inviteState.offers.size > 1,
                    onAccept = {
                        scope.launch {
                            invitesRepository.acceptInvite(dynastyId, seasonIndex, event.templateId)
                            refresh()
                            finishTask()
                        }
                    },
                )
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        invitesRepository.declineAll(dynastyId, seasonIndex)
                        refresh()
                        finishTask()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.tournament_invites_decline_all))
            }
        }
    }
}

@Composable
private fun TournamentInviteEventCard(
    event: EarlySeasonEvent,
    useElevated: Boolean,
    onAccept: () -> Unit,
) {
    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(event.displayName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(
                    R.string.tournament_invites_event_detail,
                    event.teamCount,
                    event.rsCalendarWeeks,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.tournament_invites_event_games, event.rsGameSpots),
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.tournament_invites_accept))
            }
        }
    }
    if (useElevated) {
        ElevatedCard(modifier = Modifier.fillMaxWidth(), content = body)
    } else {
        Card(modifier = Modifier.fillMaxWidth(), content = body)
    }
}

@Composable
private fun prestigeTierLabel(tier: TournamentInvitesPrestigeTier, offerCount: Int): String = when (tier) {
    TournamentInvitesPrestigeTier.Low -> stringResource(R.string.tournament_invites_tier_low)
    TournamentInvitesPrestigeTier.Mid -> stringResource(R.string.tournament_invites_tier_mid)
    TournamentInvitesPrestigeTier.High ->
        if (offerCount > 1) {
            stringResource(R.string.tournament_invites_tier_high_multi, offerCount)
        } else {
            stringResource(R.string.tournament_invites_tier_high)
        }
}

@Composable
private fun BudgetSummary(state: TournamentInvitesState) {
    val budget = state.slotBudget
    Text(
        text = stringResource(
            R.string.tournament_invites_budget,
            budget.totalRsCap,
            budget.eventRsGames,
            budget.builderSlots,
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun resolvedLabel(resolution: TournamentInviteResolution): String = when (resolution) {
    is TournamentInviteResolution.Accepted -> stringResource(
        R.string.tournament_invites_accepted,
        resolution.event.displayName,
    )
    TournamentInviteResolution.DeclinedAll -> stringResource(R.string.tournament_invites_declined_all)
    TournamentInviteResolution.NoInvitesAcknowledged -> stringResource(R.string.tournament_invites_none_ack)
    TournamentInviteResolution.Unresolved -> ""
}

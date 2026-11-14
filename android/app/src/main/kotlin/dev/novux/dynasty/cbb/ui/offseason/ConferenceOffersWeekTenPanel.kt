package dev.novux.dynasty.cbb.ui.offseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import dev.novux.dynasty.cbb.core.offseason.weekten.ConferenceOfferDecision
import dev.novux.dynasty.cbb.core.offseason.weekten.OffseasonConferenceOffersPolicy
import dev.novux.dynasty.cbb.core.qa.ConferenceOffersShellPath
import dev.novux.dynasty.cbb.data.offseason.ConferenceOffersWeekState
import dev.novux.dynasty.cbb.data.offseason.OffseasonWeekTenRepository
import kotlinx.coroutines.launch

@Composable
fun ConferenceOffersWeekTenPanel(
    dynastyId: String,
    seasonIndex: Int,
    weekTenRepository: OffseasonWeekTenRepository,
    onDecisionsChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf<ConferenceOffersWeekState?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        state = weekTenRepository.loadState(dynastyId, seasonIndex)
        onDecisionsChanged()
    }

    LaunchedEffect(dynastyId, seasonIndex) {
        refresh()
    }

    val offersState = state ?: return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.conference_offers_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.conference_offers_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (OffseasonConferenceOffersPolicy.skipConferenceOffersWeek(seasonIndex)) {
            Text(
                text = stringResource(R.string.conference_offers_season_one_skip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            @Suppress("unused")
            val qaRealignment = ConferenceOffersShellPath.MEGA_ID
            Text(
                text = stringResource(R.string.conference_offers_shell_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (offersState.offers.isEmpty()) {
            Text(
                text = stringResource(R.string.conference_offers_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(offersState.offers, key = { it.offerId }) { offer ->
                    val decision = offersState.decisions[offer.offerId]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(
                                    R.string.conference_offers_invite,
                                    offer.invitingConferenceName,
                                    offer.currentConferenceName,
                                ),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(stringResource(R.string.conference_offers_score, offer.inviteScore))
                            Text(stringResource(R.string.conference_offers_pros, offer.prosSummary))
                            Text(stringResource(R.string.conference_offers_cons, offer.consSummary))
                            when (decision) {
                                ConferenceOfferDecision.Accepted -> Text(
                                    stringResource(R.string.conference_offers_accepted),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                ConferenceOfferDecision.Declined -> Text(
                                    stringResource(R.string.conference_offers_declined),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                weekTenRepository.respond(
                                                    dynastyId,
                                                    seasonIndex,
                                                    offer.offerId,
                                                    ConferenceOfferDecision.Accepted,
                                                )
                                                refresh()
                                            }
                                        },
                                    ) {
                                        Text(stringResource(R.string.conference_offers_accept))
                                    }
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                weekTenRepository.respond(
                                                    dynastyId,
                                                    seasonIndex,
                                                    offer.offerId,
                                                    ConferenceOfferDecision.Declined,
                                                )
                                                refresh()
                                            }
                                        },
                                    ) {
                                        Text(stringResource(R.string.conference_offers_decline))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

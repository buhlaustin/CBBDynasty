package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseHistoryEntry
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseHistoryHydrator
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseHistoryPolicy
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus

@Suppress("unused")
private val promiseHistoryQaAnchor: String = PlayerPromiseHistoryPolicy.QA_ID

@Composable
fun PlayerPromiseHistoryPanel(
    player: DynastyPlayer,
    currentSeasonIndex: Int,
    modifier: Modifier = Modifier,
) {
    val promise = player.loyalty.promise
    val seasons = PlayerPromiseHistoryHydrator.historyForDisplay(player, currentSeasonIndex)
    if (promise == null) {
        Text(
            stringResource(R.string.player_card_history_promise_none),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    stringResource(R.string.player_card_history_promise_locked_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(
                        R.string.player_card_history_promise_locked_line,
                        promise.displayLabel(),
                        promise.lockedAtSeasonIndex?.let { SeasonMembershipYear.label(it) }
                            ?: stringResource(R.string.player_card_history_promise_unknown_season),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (seasons.isEmpty()) {
            Text(
                stringResource(R.string.player_card_history_promise_no_evaluations),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            seasons.forEach { entry ->
                PlayerPromiseSeasonEvaluationRow(entry = entry)
            }
        }
    }
}

@Composable
private fun PlayerPromiseSeasonEvaluationRow(entry: PlayerPromiseHistoryEntry) {
    val statusRes = when (entry.status) {
        PromiseStatus.Kept -> R.string.player_promise_status_kept
        PromiseStatus.Broken -> R.string.player_promise_status_broken
        PromiseStatus.AtRisk -> R.string.player_promise_status_at_risk
        PromiseStatus.Deferred -> R.string.player_promise_status_deferred
        PromiseStatus.Pending -> R.string.player_promise_status_pending
    }
    Text(
        stringResource(
            R.string.player_card_history_promise_season_row,
            SeasonMembershipYear.label(entry.seasonIndex),
            stringResource(statusRes),
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
}

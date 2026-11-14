package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.loyalty.PlayerPromiseAtRiskUiPolicy
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus

/** Loyalty + promise status chips; at-risk styling before break ([MEGA-COACH-170]). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerLoyaltyChips(player: DynastyPlayer, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AssistChip(
            onClick = {},
            label = {
                Text(
                    stringResource(R.string.player_loyalty_chip, player.loyalty.loyalty.value),
                )
            },
        )
        player.loyalty.promise?.let { promise ->
            @Suppress("unused")
            val qaAtRisk = PlayerPromiseAtRiskUiPolicy.MEGA_ID
            val statusRes = when (promise.status) {
                PromiseStatus.Kept -> R.string.player_promise_status_kept
                PromiseStatus.Broken -> R.string.player_promise_status_broken
                PromiseStatus.AtRisk -> R.string.player_promise_status_at_risk
                PromiseStatus.Deferred -> R.string.player_promise_status_deferred
                PromiseStatus.Pending -> R.string.player_promise_status_pending
            }
            val atRisk = promise.status == PromiseStatus.AtRisk
            AssistChip(
                onClick = {},
                colors = if (atRisk) {
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                } else {
                    AssistChipDefaults.assistChipColors()
                },
                label = {
                    Text(
                        stringResource(
                            R.string.player_promise_chip,
                            promise.displayLabel(),
                            stringResource(statusRes),
                        ),
                    )
                },
            )
        }
    }
}

@Composable
fun PlayerLoyaltyRatingsRow(player: DynastyPlayer, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.player_ratings_loyalty_row, player.loyalty.loyalty.value),
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

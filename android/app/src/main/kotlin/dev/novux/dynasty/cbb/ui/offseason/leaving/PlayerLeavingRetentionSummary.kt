package dev.novux.dynasty.cbb.ui.offseason.leaving

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.offseason.leaving.PlayerLeavingRetentionPolicy
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.loyalty.PromiseStatus

/** Combined loyalty + promise line for Week 1 retention / convince ([MEGA-COACH-172]). */
@Composable
fun PlayerLeavingRetentionSummary(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = PlayerLeavingRetentionPolicy.MEGA_ID
    val promise = player.loyalty.promise
    val loyalty = player.loyalty.loyalty.value
  val text = if (promise == null) {
        stringResource(R.string.leaving_loyalty_only, loyalty)
    } else {
        val statusRes = when (promise.status) {
            PromiseStatus.Kept -> R.string.player_promise_status_kept
            PromiseStatus.Broken -> R.string.player_promise_status_broken
            PromiseStatus.AtRisk -> R.string.player_promise_status_at_risk
            PromiseStatus.Deferred -> R.string.player_promise_status_deferred
            PromiseStatus.Pending -> R.string.player_promise_status_pending
        }
        stringResource(
            R.string.leaving_retention_promise_loyalty,
            loyalty,
            promise.displayLabel(),
            stringResource(statusRes),
        )
    }
    val emphasis = promise?.status == PromiseStatus.Broken || promise?.status == PromiseStatus.AtRisk
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (emphasis) FontWeight.SemiBold else FontWeight.Normal,
        color = when (promise?.status) {
            PromiseStatus.Broken -> MaterialTheme.colorScheme.error
            PromiseStatus.AtRisk -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

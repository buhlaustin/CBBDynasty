package dev.novux.dynasty.cbb.ui.strategy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesBudget

@Composable
fun RotationMinutesBudgetMeter(
    totalMinutes: Int,
    onBalanceToCap: () -> Unit,
    cap: Int = RotationMinutesAllocator.TEAM_MINUTES_REGULATION,
) {
    val status = RotationMinutesBudget.status(totalMinutes, cap)
    val progress = RotationMinutesBudget.progress(totalMinutes, cap).coerceAtMost(1f)
    val indicatorColor = when (status) {
        RotationMinutesBudget.Status.Exact -> MaterialTheme.colorScheme.primary
        RotationMinutesBudget.Status.Under -> MaterialTheme.colorScheme.tertiary
        RotationMinutesBudget.Status.Over -> MaterialTheme.colorScheme.error
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.strategy_rotation_total, totalMinutes, cap),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = indicatorColor,
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = indicatorColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        when (status) {
            RotationMinutesBudget.Status.Exact ->
                Text(
                    text = stringResource(R.string.strategy_rotation_budget_ok),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            RotationMinutesBudget.Status.Under ->
                Text(
                    text = stringResource(R.string.strategy_rotation_budget_under, cap - totalMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            RotationMinutesBudget.Status.Over ->
                Text(
                    text = stringResource(R.string.strategy_rotation_budget_over, totalMinutes - cap),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
        }
        if (status != RotationMinutesBudget.Status.Exact) {
            TextButton(onClick = onBalanceToCap) {
                Text(stringResource(R.string.strategy_rotation_balance_action))
            }
        }
    }
}

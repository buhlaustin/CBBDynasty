package dev.novux.dynasty.cbb.ui.strategy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.lineup.LineupRotationCadencePolicy
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesBalancer
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer

@Composable
fun StrategyRotationMinutesPanel(
    players: List<RosterPlayer>,
    starters: Map<PlayerPosition, String>,
    minutesByPlayer: Map<String, Int>,
    onMinutesChange: (Map<String, Int>) -> Unit,
) {
    val eligible = players.filter { RotationMinutesAllocator.isLineupEligible(it) }
    val ordered = buildList {
        PlayerPosition.STARTER_ORDER.forEach { slot ->
            starters[slot]?.let { id ->
                eligible.firstOrNull { it.playerId == id }?.let { add(it) }
            }
        }
        eligible
            .filter { it.playerId !in starters.values }
            .sortedBy { it.depthOrder }
            .forEach { add(it) }
    }
    val total = minutesByPlayer.values.sum()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.strategy_rotation_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.strategy_rotation_slots_hint,
                LineupRotationCadencePolicy.starterSlotCount,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RotationMinutesBudgetMeter(
            totalMinutes = total,
            onBalanceToCap = {
                onMinutesChange(
                    RotationMinutesBalancer.normalize(minutesByPlayer),
                )
            },
        )
        ordered.forEach { player ->
            val minutes = minutesByPlayer[player.playerId] ?: 0
            val isStarter = player.playerId in starters.values
            RotationMinutesRow(
                label = player.displayLabel() + if (isStarter) " *" else "",
                minutes = minutes,
                onDecrease = {
                    onMinutesChange(
                        RotationMinutesBalancer.tweak(minutesByPlayer, player.playerId, -1),
                    )
                },
                onIncrease = {
                    onMinutesChange(
                        RotationMinutesBalancer.tweak(minutesByPlayer, player.playerId, +1),
                    )
                },
            )
        }
    }
}

@Composable
private fun RotationMinutesRow(
    label: String,
    minutes: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDecrease, enabled = minutes > RotationMinutesBalancer.PLAYER_MIN) {
                Text("−")
            }
            Text(
                text = stringResource(R.string.strategy_rotation_minutes_value, minutes),
                style = MaterialTheme.typography.bodyLarge,
            )
            TextButton(onClick = onIncrease, enabled = minutes < RotationMinutesBalancer.PLAYER_MAX) {
                Text("+")
            }
        }
    }
}

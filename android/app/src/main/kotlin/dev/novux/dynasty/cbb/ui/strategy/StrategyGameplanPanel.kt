package dev.novux.dynasty.cbb.ui.strategy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.LateGameFoulStrategy

@Composable
fun StrategyGameplanPanel(
    starters: Map<PlayerPosition, String>,
    players: List<RosterPlayer>,
    gameplan: GameplanInput,
    onGameplanChange: (GameplanInput) -> Unit,
    halftimeGameplan: GameplanInput?,
    onHalftimeChange: (GameplanInput?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.strategy_gameplan_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.strategy_usage_label),
            style = MaterialTheme.typography.labelLarge,
        )
        PlayerPosition.STARTER_ORDER.forEach { slot ->
            val id = starters[slot] ?: return@forEach
            val player = players.firstOrNull { it.playerId == id } ?: return@forEach
            val weight = gameplan.usageWeights[id] ?: 20
            UsageStepperRow(
                label = player.displayLabel(),
                weight = weight,
                onDecrease = {
                    val next = (weight - 5).coerceAtLeast(5)
                    onGameplanChange(gameplan.copy(usageWeights = gameplan.usageWeights + (id to next)))
                },
                onIncrease = {
                    val next = (weight + 5).coerceAtMost(60)
                    onGameplanChange(gameplan.copy(usageWeights = gameplan.usageWeights + (id to next)))
                },
            )
        }
        StrategyDefensiveAssignmentPanel(
            players = players,
            starters = starters,
            gameplan = gameplan,
            onGameplanChange = onGameplanChange,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.strategy_deny_primary),
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = gameplan.denyPrimaryScorer,
                onCheckedChange = { onGameplanChange(gameplan.copy(denyPrimaryScorer = it)) },
            )
        }
        Text(
            text = stringResource(R.string.strategy_late_fouls),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LateGameFoulStrategy.entries.forEach { strategy ->
                FilterChip(
                    selected = gameplan.lateGameFoulStrategy == strategy,
                    onClick = { onGameplanChange(gameplan.copy(lateGameFoulStrategy = strategy)) },
                    label = {
                        Text(
                            when (strategy) {
                                LateGameFoulStrategy.Standard ->
                                    stringResource(R.string.strategy_late_foul_standard)
                                LateGameFoulStrategy.FoulEarly ->
                                    stringResource(R.string.strategy_late_foul_foul_early)
                                LateGameFoulStrategy.ProtectLead ->
                                    stringResource(R.string.strategy_late_foul_protect)
                            },
                        )
                    },
                )
            }
        }
        Text(
            text = stringResource(R.string.strategy_halftime_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = {
                if (halftimeGameplan == null) {
                    onHalftimeChange(gameplan)
                } else {
                    onHalftimeChange(null)
                }
            },
        ) {
            Text(
                if (halftimeGameplan == null) {
                    stringResource(R.string.strategy_halftime_set)
                } else {
                    stringResource(R.string.strategy_halftime_clear)
                },
            )
        }
    }
}

@Composable
private fun UsageStepperRow(
    label: String,
    weight: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onDecrease) { Text("−") }
        Text(text = stringResource(R.string.strategy_usage_percent, weight), style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onIncrease) { Text("+") }
    }
}

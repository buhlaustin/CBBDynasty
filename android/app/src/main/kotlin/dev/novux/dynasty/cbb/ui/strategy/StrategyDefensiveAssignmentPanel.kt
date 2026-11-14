package dev.novux.dynasty.cbb.ui.strategy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.FilterChip
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
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.strategy.DefensiveAssignmentPolicy
import dev.novux.dynasty.cbb.core.strategy.GameplanInput

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StrategyDefensiveAssignmentPanel(
    players: List<RosterPlayer>,
    starters: Map<PlayerPosition, String>,
    gameplan: GameplanInput,
    onGameplanChange: (GameplanInput) -> Unit,
) {
    val assignments = if (gameplan.defensiveAssignments.isEmpty()) {
        DefensiveAssignmentPolicy.defaultAssignments(starters)
    } else {
        DefensiveAssignmentPolicy.resolve(gameplan, starters)
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.strategy_defensive_assignments_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.strategy_defensive_assignments_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.strategy_primary_scorer_slot),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DefensiveAssignmentPolicy.OPPONENT_SLOTS.forEach { slot ->
                FilterChip(
                    selected = gameplan.primaryScorerSlot == slot,
                    onClick = { onGameplanChange(gameplan.copy(primaryScorerSlot = slot)) },
                    label = { Text(stringResource(R.string.strategy_opp_slot_label, slot.label)) },
                )
            }
        }
        DefensiveAssignmentPolicy.OPPONENT_SLOTS.forEach { oppSlot ->
            val defenderId = assignments[oppSlot]
            val defender = players.firstOrNull { it.playerId == defenderId }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.strategy_defensive_assignment_row,
                        oppSlot.label,
                        defender?.displayLabel() ?: "—",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        val next = DefensiveAssignmentPolicy.cycleAssignment(
                            players = players,
                            opponentSlot = oppSlot,
                            currentDefenderId = defenderId,
                        ) ?: return@TextButton
                        onGameplanChange(
                            gameplan.copy(
                                defensiveAssignments = assignments + (oppSlot to next),
                            ),
                        )
                    },
                ) {
                    Text(stringResource(R.string.strategy_defensive_assignment_swap))
                }
            }
        }
    }
}

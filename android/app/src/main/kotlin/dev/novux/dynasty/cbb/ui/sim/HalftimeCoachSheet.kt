package dev.novux.dynasty.cbb.ui.sim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import dev.novux.dynasty.cbb.core.lineup.LineupSlotPolicy
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.HalftimeCoachAdjustments
import dev.novux.dynasty.cbb.core.sim.HalftimeScoutingSnapshot
import dev.novux.dynasty.cbb.core.sim.TeamBoxScoreDisplay
import dev.novux.dynasty.cbb.core.sim.HalftimeRotationBandHint
import dev.novux.dynasty.cbb.core.sim.HalftimeRotationEmphasis
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.GameplanUsageNormalizer
import dev.novux.dynasty.cbb.core.strategy.LateGameFoulStrategy
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import dev.novux.dynasty.cbb.ui.newgame.SchemesStepContent

@Composable
fun HalftimeCoachSheet(
    scoreLine: String,
    scouting: HalftimeScoutingSnapshot? = null,
    players: List<RosterPlayer>,
    state: HalftimeCoachAdjustments,
    onStateChange: (HalftimeCoachAdjustments) -> Unit,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.game_watch_halftime_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(scoreLine, fontWeight = FontWeight.SemiBold)
                scouting?.let { HalftimeOpponentScoutingStrip(it) }
                Text(
                    text = stringResource(R.string.game_watch_halftime_schemes),
                    style = MaterialTheme.typography.titleSmall,
                )
                SchemesStepContent(
                    offensive = state.offensiveScheme,
                    defensive = state.defensiveScheme,
                    onOffensiveChange = { onStateChange(state.copy(offensiveScheme = it)) },
                    onDefensiveChange = { onStateChange(state.copy(defensiveScheme = it)) },
                )
                Text(
                    text = stringResource(R.string.game_watch_halftime_rotation),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HalftimeRotationEmphasis.entries.forEach { emphasis ->
                        FilterChip(
                            selected = state.rotationEmphasis == emphasis,
                            onClick = { onStateChange(state.copy(rotationEmphasis = emphasis)) },
                            label = {
                                Text(
                                    when (emphasis) {
                                        HalftimeRotationEmphasis.RideStarters ->
                                            stringResource(R.string.game_watch_halftime_rotation_starters)
                                        HalftimeRotationEmphasis.Neutral ->
                                            stringResource(R.string.game_watch_halftime_rotation_neutral)
                                        HalftimeRotationEmphasis.ExpandBench ->
                                            stringResource(R.string.game_watch_halftime_rotation_bench)
                                    },
                                )
                            },
                        )
                    }
                }
                Text(
                    text = HalftimeRotationBandHint.minuteBandSummary(state.rotationEmphasis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.game_watch_halftime_starters),
                    style = MaterialTheme.typography.titleSmall,
                )
                PlayerPosition.STARTER_ORDER.forEach { slot ->
                    val currentId = state.starters[slot]
                    val current = players.firstOrNull { it.playerId == currentId }
                    val options = eligibleForSlot(players, slot)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.game_watch_halftime_starter_row,
                                slot.label,
                                current?.displayLabel() ?: "—",
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            enabled = options.size > 1,
                            onClick = {
                                val next = cycleStarter(options, currentId)
                                if (next == null) return@TextButton
                                val newStarters = state.starters + (slot to next)
                                val newGameplan = ensureUsageForStarters(state.gameplan, newStarters)
                                onStateChange(state.copy(starters = newStarters, gameplan = newGameplan))
                            },
                        ) {
                            Text(stringResource(R.string.game_watch_halftime_swap))
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.strategy_usage_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                state.starters.values.distinct().forEach { id ->
                    val player = players.firstOrNull { it.playerId == id } ?: return@forEach
                    val weight = state.gameplan.usageWeights[id] ?: 20
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(player.displayLabel(), style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    val next = (weight - 5).coerceAtLeast(5)
                                    onStateChange(
                                        state.copy(
                                            gameplan = state.gameplan.copy(
                                                usageWeights = state.gameplan.usageWeights + (id to next),
                                            ),
                                        ),
                                    )
                                },
                            ) { Text("−") }
                            Text(stringResource(R.string.strategy_usage_percent, weight))
                            TextButton(
                                onClick = {
                                    val next = (weight + 5).coerceAtMost(60)
                                    onStateChange(
                                        state.copy(
                                            gameplan = state.gameplan.copy(
                                                usageWeights = state.gameplan.usageWeights + (id to next),
                                            ),
                                        ),
                                    )
                                },
                            ) { Text("+") }
                        }
                    }
                }
                HalftimeGameplanToggles(
                    gameplan = state.gameplan,
                    onGameplanChange = { onStateChange(state.copy(gameplan = it)) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(stringResource(R.string.game_watch_halftime_resume))
            }
        },
    )
}

@Composable
@Composable
private fun HalftimeOpponentScoutingStrip(scouting: HalftimeScoutingSnapshot) {
    val display = TeamBoxScoreDisplay.from(scouting.opponentBox, emptyList())
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.game_watch_halftime_scouting_title),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = stringResource(
                R.string.game_watch_halftime_scouting_matchup,
                scouting.userPoints,
                scouting.opponentPoints,
                scouting.opponentName,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(
                R.string.game_watch_halftime_scouting_ratings,
                scouting.opponentOffensiveRating,
                scouting.opponentDefensiveRating,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = stringResource(
                R.string.game_watch_halftime_scouting_shooting,
                display.box.fieldGoalsMade,
                display.box.fieldGoalsAttempted,
                display.fieldGoalPct,
                display.box.threePointsMade,
                display.box.threePointsAttempted,
                display.threePointPct,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = stringResource(
                R.string.game_watch_halftime_scouting_misc,
                display.box.turnovers,
                display.box.personalFouls,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun HalftimeGameplanToggles(
    gameplan: GameplanInput,
    onGameplanChange: (GameplanInput) -> Unit,
) {
    Text(stringResource(R.string.strategy_deny_primary))
    TextButton(
        onClick = {
            onGameplanChange(gameplan.copy(denyPrimaryScorer = !gameplan.denyPrimaryScorer))
        },
    ) {
        Text(
            if (gameplan.denyPrimaryScorer) {
                stringResource(R.string.game_watch_halftime_deny_on)
            } else {
                stringResource(R.string.game_watch_halftime_deny_off)
            },
        )
    }
    Text(stringResource(R.string.strategy_late_fouls))
    LateGameFoulStrategy.entries.forEach { strategy ->
        TextButton(
            onClick = { onGameplanChange(gameplan.copy(lateGameFoulStrategy = strategy)) },
        ) {
            Text(
                when (strategy) {
                    LateGameFoulStrategy.Standard -> stringResource(R.string.strategy_late_foul_standard)
                    LateGameFoulStrategy.FoulEarly -> stringResource(R.string.strategy_late_foul_foul_early)
                    LateGameFoulStrategy.ProtectLead -> stringResource(R.string.strategy_late_foul_protect)
                },
            )
        }
    }
}

private fun eligibleForSlot(players: List<RosterPlayer>, slot: PlayerPosition): List<RosterPlayer> =
    players
        .filter { RotationMinutesAllocator.isLineupEligible(it) }
        .filter { LineupSlotPolicy.slotEligible(it.position, slot) }
        .sortedBy { it.depthOrder }

private fun cycleStarter(options: List<RosterPlayer>, currentId: String?): String? {
    if (options.isEmpty()) return null
    val index = options.indexOfFirst { it.playerId == currentId }.let { if (it < 0) 0 else (it + 1) % options.size }
    return options[index].playerId
}

private fun ensureUsageForStarters(
    gameplan: GameplanInput,
    starters: Map<PlayerPosition, String>,
): GameplanInput {
    val ids = starters.values.toSet()
    val missing = ids.filter { it !in gameplan.usageWeights }
    if (missing.isEmpty()) return gameplan
    val extra = GameplanUsageNormalizer.defaultForStarters(missing)
    return gameplan.copy(usageWeights = gameplan.usageWeights + extra)
}

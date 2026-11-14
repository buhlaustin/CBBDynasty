package dev.novux.dynasty.cbb.ui.strategy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.discipline.DisciplineResolver
import dev.novux.dynasty.cbb.core.lineup.DefaultStarterAlgorithm
import dev.novux.dynasty.cbb.core.lineup.LineupSlotPolicy
import dev.novux.dynasty.cbb.core.lineup.RosterStarterWireResolver
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesAllocator
import dev.novux.dynasty.cbb.core.lineup.RotationMinutesBalancer
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.GameLineupPrep
import dev.novux.dynasty.cbb.core.sim.GameLineupPrepValidator
import dev.novux.dynasty.cbb.core.strategy.DefensiveAssignmentPolicy
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.GameplanUsageNormalizer
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import dev.novux.dynasty.cbb.data.roster.DynastyRosterRepository
import dev.novux.dynasty.cbb.data.strategy.DynastyStrategyPrepRepository
import dev.novux.dynasty.cbb.ui.player.PlayerInjuryRosterRow
import kotlinx.coroutines.launch

@Composable
fun StrategyScreen(
    dynastyId: String,
    rosterRepository: DynastyRosterRepository,
    strategyPrepRepository: DynastyStrategyPrepRepository,
    teamChemistry: Int? = null,
    fatigueTuning: dev.novux.dynasty.cbb.core.balance.BalanceFatigue =
        dev.novux.dynasty.cbb.core.balance.BalanceFatigue.DEFAULT,
    onBack: () -> Unit,
    onWatchScrimmage: () -> Unit = {},
    homeTeamLabel: String = "Home",
    awayTeamLabel: String = "Away",
    prepEditable: Boolean = true,
) {
    val editsEnabled = prepEditable
    val scope = rememberCoroutineScope()
    var players by remember { mutableStateOf<List<RosterPlayer>>(emptyList()) }
    var userStarters by remember { mutableStateOf<Map<PlayerPosition, String>>(emptyMap()) }
    var assignTargetSlot by remember { mutableStateOf<PlayerPosition?>(null) }
    LaunchedEffect(dynastyId) {
        players = rosterRepository.listPlayers(dynastyId)
    }
    LaunchedEffect(players, fatigueTuning.starterBlockThreshold) {
        if (players.isNotEmpty() && userStarters.isEmpty()) {
            userStarters = RosterStarterWireResolver.resolve(players, fatigueTuning.starterBlockThreshold)
        }
    }
    val starters = userStarters
    val bench = players
        .filter { it.playerId !in starters.values }
        .sortedBy { it.depthOrder }
    var gameplan by remember { mutableStateOf(GameplanInput()) }
    var halftimeGameplan by remember { mutableStateOf<GameplanInput?>(null) }
    var rotationMinutes by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var offensiveScheme by remember { mutableStateOf(OffensiveSchemePreset.Balanced) }
    var defensiveScheme by remember { mutableStateOf(DefensiveSchemePreset.ManToMan) }
    var coachDefaultOffensive by remember { mutableStateOf(OffensiveSchemePreset.Balanced) }
    var coachDefaultDefensive by remember { mutableStateOf(DefensiveSchemePreset.ManToMan) }
    LaunchedEffect(dynastyId) {
        val prep = strategyPrepRepository.loadForStrategy(dynastyId)
        coachDefaultOffensive = prep.coachOffensiveScheme
        coachDefaultDefensive = prep.coachDefensiveScheme
        offensiveScheme = prep.gameOffensiveScheme
        defensiveScheme = prep.gameDefensiveScheme
    }
    LaunchedEffect(players, starters) {
        if (players.isNotEmpty() && rotationMinutes.isEmpty()) {
            rotationMinutes = RotationMinutesBalancer.baseline(players, starters)
        }
    }
    val lineupValidation = remember(players, starters) {
        GameLineupPrepValidator.validate(players, starters)
    }

    LaunchedEffect(starters.values) {
        if (gameplan.usageWeights.isEmpty() && starters.isNotEmpty()) {
            gameplan = gameplan.copy(
                usageWeights = GameplanUsageNormalizer.defaultForStarters(starters.values),
            )
        }
        if (gameplan.defensiveAssignments.isEmpty() && starters.isNotEmpty()) {
            gameplan = gameplan.copy(
                defensiveAssignments = DefensiveAssignmentPolicy.defaultAssignments(starters),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.strategy_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.strategy_injured_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!editsEnabled) {
            Text(
                text = stringResource(R.string.strategy_prep_locked),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        teamChemistry?.let { chem ->
            Text(
                text = stringResource(R.string.strategy_chemistry, chem),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = stringResource(R.string.strategy_starters),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.strategy_tap_slot_to_assign),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PlayerPosition.STARTER_ORDER.forEach { slot ->
            val player = starters[slot]?.let { id -> players.firstOrNull { it.playerId == id } }
            StrategyLineupCard(
                title = slot.label,
                player = player,
                selected = assignTargetSlot == slot,
                onSelect = {
                    if (!editsEnabled) return@StrategyLineupCard
                    assignTargetSlot = if (assignTargetSlot == slot) null else slot
                },
                onCycle = {
                    if (!editsEnabled) return@StrategyLineupCard
                    val options = eligibleForSlot(players, slot)
                    val next = cycleStarter(options, starters[slot])
                    if (next != null) {
                        val cleaned = starters.filterValues { it != next }
                        userStarters = cleaned + (slot to next)
                    }
                },
            )
        }
        OutlinedButton(
            enabled = editsEnabled,
            onClick = {
                scope.launch {
                    players = rosterRepository.applyStarterLineup(dynastyId, userStarters)
                    userStarters = RosterStarterWireResolver.resolve(players, fatigueTuning.starterBlockThreshold)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.strategy_save_lineup))
        }
        Text(
            text = stringResource(R.string.strategy_bench),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        bench.forEach { player ->
            StrategyLineupCard(
                title = player.position.label,
                player = player,
                selected = false,
                onSelect = {},
                onCycle = {},
                onAssign = {
                    if (!editsEnabled) return@StrategyLineupCard
                    val slot = assignTargetSlot ?: return@StrategyLineupCard
                    if (!LineupSlotPolicy.slotEligible(player.position, slot)) return@StrategyLineupCard
                    if (player.injuryActive) return@StrategyLineupCard
                    if (!RotationMinutesAllocator.isLineupEligible(player)) return@StrategyLineupCard
                    if (DisciplineResolver.isIneligibleForLineup(player)) return@StrategyLineupCard
                    val cleaned = starters.filterValues { it != player.playerId }
                    userStarters = cleaned + (slot to player.playerId)
                    assignTargetSlot = null
                },
            )
        }
        StrategyGameSchemePanel(
            offensiveScheme = offensiveScheme,
            defensiveScheme = defensiveScheme,
            coachDefaultOffensive = coachDefaultOffensive,
            coachDefaultDefensive = coachDefaultDefensive,
            onOffensiveChange = { if (editsEnabled) offensiveScheme = it },
            onDefensiveChange = { if (editsEnabled) defensiveScheme = it },
            onSaveGamePrep = {
                if (!editsEnabled) return@StrategyGameSchemePanel
                scope.launch {
                    strategyPrepRepository.saveGamePrep(dynastyId, offensiveScheme, defensiveScheme)
                }
            },
        )
        if (rotationMinutes.isNotEmpty()) {
            StrategyRotationMinutesPanel(
                players = players,
                starters = starters,
                minutesByPlayer = rotationMinutes,
                onMinutesChange = { rotationMinutes = it },
            )
        }
        StrategyGameplanPanel(
            starters = starters,
            players = players,
            gameplan = gameplan,
            onGameplanChange = { gameplan = it },
            halftimeGameplan = halftimeGameplan,
            onHalftimeChange = { halftimeGameplan = it },
        )
        if (!lineupValidation.canStartSim) {
            Text(
                text = stringResource(R.string.strategy_lineup_sim_blocked),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(
            enabled = lineupValidation.canStartSim && editsEnabled,
            onClick = {
                val normalizedRotation = RotationMinutesBalancer.normalize(rotationMinutes)
                rotationMinutes = normalizedRotation
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.homeGameplan = halftimeGameplan ?: gameplan
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.homeName = homeTeamLabel
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.awayName = awayTeamLabel
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.homeRosterPlayers = players
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.homeStarters = starters
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.homeOffensiveScheme = offensiveScheme
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.homeDefensiveScheme = defensiveScheme
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.rotationMinutes = normalizedRotation
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.homeSimRoster =
                    GameLineupPrep.build(dynastyId, players, starters, normalizedRotation)
                dev.novux.dynasty.cbb.ui.sim.GameWatchLaunch.isScrimmage = true
                onWatchScrimmage()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.strategy_watch_scrimmage))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_back))
        }
    }
}

@Composable
private fun eligibleForSlot(players: List<RosterPlayer>, slot: PlayerPosition): List<RosterPlayer> =
    players
        .filter { RotationMinutesAllocator.isLineupEligible(it) }
        .filter { LineupSlotPolicy.slotEligible(it.position, slot) }
        .filter { !it.injuryActive }
        .filter { !DisciplineResolver.isIneligibleForLineup(it) }
        .sortedBy { it.depthOrder }

private fun cycleStarter(options: List<RosterPlayer>, currentId: String?): String? {
    if (options.isEmpty()) return null
    val index = options.indexOfFirst { it.playerId == currentId }.let { if (it < 0) 0 else (it + 1) % options.size }
    return options[index].playerId
}

private fun StrategyLineupCard(
    title: String,
    player: RosterPlayer?,
    selected: Boolean,
    onSelect: () -> Unit,
    onCycle: () -> Unit,
    onAssign: (() -> Unit)? = null,
) {
    val injured = player != null && (player.injuryActive || DisciplineResolver.isIneligibleForLineup(player))
    val exhausted = player != null && player.stamina < 20
    val dimmed = injured || exhausted
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.45f else 1f)
            .clickable(enabled = !dimmed) {
                if (onAssign != null) onAssign() else onSelect()
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> MaterialTheme.colorScheme.primaryContainer
                dimmed -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            if (player == null) {
                Text(
                    text = stringResource(R.string.strategy_slot_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onCycle, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.strategy_pick_starter))
                }
            } else if (injured) {
                PlayerInjuryRosterRow(player)
            } else {
                Text(text = player.displayLabel(), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(R.string.strategy_stamina, player.stamina),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

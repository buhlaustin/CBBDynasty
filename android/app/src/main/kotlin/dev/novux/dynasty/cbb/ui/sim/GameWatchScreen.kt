package dev.novux.dynasty.cbb.ui.sim

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.sim.GameLineupPrep
import dev.novux.dynasty.cbb.core.sim.GameLineupPrepValidator
import dev.novux.dynasty.cbb.core.roster.PlayerPosition
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.sim.GameSimInput
import dev.novux.dynasty.cbb.core.sim.SimHomeVenueType
import dev.novux.dynasty.cbb.core.sim.GameSimInjuryRollInput
import dev.novux.dynasty.cbb.core.sim.GameSimResult
import dev.novux.dynasty.cbb.core.sim.GameSimRosterInput
import dev.novux.dynasty.cbb.core.sim.GameSimTeamInput
import dev.novux.dynasty.cbb.core.sim.GameWatchChrome
import dev.novux.dynasty.cbb.core.sim.GameWatchPauseGate
import dev.novux.dynasty.cbb.core.sim.GameWatchPeriod
import dev.novux.dynasty.cbb.core.sim.GameWatchPlayback
import dev.novux.dynasty.cbb.core.sim.GameWatchSpeedPresets
import dev.novux.dynasty.cbb.core.sim.HalftimeCoachAdjustments
import dev.novux.dynasty.cbb.core.sim.HalftimeRosterAdjust
import dev.novux.dynasty.cbb.core.sim.HalftimeScoutingSnapshot
import dev.novux.dynasty.cbb.core.sim.tuning.SimTuning
import dev.novux.dynasty.cbb.core.reports.PostgameReportGenerator
import dev.novux.dynasty.cbb.core.reports.ReportArticle
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.GameplanInput
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import dev.novux.dynasty.cbb.core.balance.BalanceMedicalRedshirtRules
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.injuries.GameSimInjuryEvent
import dev.novux.dynasty.cbb.ui.postgame.PostgameInjuryFlow
import dev.novux.dynasty.cbb.ui.reports.ReportArticleView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private data class GameWatchFeedRow(
    val logLine: String,
    val shotClockSeconds: Int?,
)

@Composable
fun GameWatchScreen(
    tuning: SimTuning,
    homeName: String,
    awayName: String,
    homeGameplan: GameplanInput,
    homeOffensiveScheme: OffensiveSchemePreset = OffensiveSchemePreset.Balanced,
    homeDefensiveScheme: DefensiveSchemePreset = DefensiveSchemePreset.ManToMan,
    homeRosterPlayers: List<RosterPlayer> = emptyList(),
    homeStarters: Map<PlayerPosition, String> = emptyMap(),
    onHomeHalftimeAdjustments: (HalftimeCoachAdjustments) -> Unit = {},
    homeSimRoster: GameSimRosterInput? = null,
    injuryRoll: GameSimInjuryRollInput? = null,
    postgameInjuryEvents: List<GameSimInjuryEvent> = emptyList(),
    postgameInjuryRoster: List<RosterPlayer> = emptyList(),
    rsWeekIndex: Int = 0,
    medicalRules: BalanceMedicalRedshirtRules = BalanceMedicalRedshirtRules(),
    onDismissPostgameInjuries: () -> Unit = {},
    onApplyMedicalRedshirt: (String) -> Unit = {},
    onSimComplete: (GameSimResult) -> Unit = {},
    onBack: () -> Unit,
    seed: Long = 17L,
    userTeamIsHome: Boolean = true,
    homeVenue: SimHomeVenueType = SimHomeVenueType.Home,
    userOffensiveRating: Int = 78,
    userDefensiveRating: Int = 74,
    awayOffensiveRating: Int = 76,
    awayDefensiveRating: Int = 75,
) {
    val context = LocalContext.current
    val lines = remember { mutableStateListOf<GameWatchFeedRow>() }
    var watchChrome by remember { mutableStateOf<GameWatchChrome?>(null) }
    var scoreLine by remember { mutableStateOf("") }
    var halftimeOpen by remember { mutableStateOf(false) }
    var halftimeScore by remember { mutableStateOf("") }
    var halftimeScouting by remember { mutableStateOf<HalftimeScoutingSnapshot?>(null) }
    var halftimeAdjustments by remember {
        mutableStateOf(
            HalftimeCoachAdjustments(
                gameplan = homeGameplan,
                offensiveScheme = homeOffensiveScheme,
                defensiveScheme = homeDefensiveScheme,
                starters = homeStarters,
            ),
        )
    }
    var postgameArticle by remember { mutableStateOf<ReportArticle?>(null) }
    var finalResult by remember { mutableStateOf<GameSimResult?>(null) }
    val listState = rememberLazyListState()
    var followLive by remember { mutableStateOf(true) }
    val atLiveEdge by remember {
        derivedStateOf {
            if (lines.isEmpty()) true
            else {
                val info = listState.layoutInfo
                val lastIndex = lines.lastIndex
                info.visibleItemsInfo.any { it.index >= lastIndex }
            }
        }
    }
    LaunchedEffect(atLiveEdge) {
        if (atLiveEdge) {
            followLive = true
        }
    }
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, lines.size, watchInProgress) {
        if (watchInProgress && lines.isNotEmpty() && !atLiveEdge) {
            followLive = false
        }
    }
    LaunchedEffect(lines.size, followLive) {
        if (lines.isNotEmpty() && followLive) {
            listState.animateScrollToItem(lines.lastIndex)
        }
    }
    val resumeGate = remember { mutableStateOf<((HalftimeCoachAdjustments) -> Unit)?>(null) }
    var selectedSpeedMultiplier by remember {
        mutableStateOf(GameWatchPlayback.DEFAULT_WATCH_SPEED_MULTIPLIER)
    }
    var simToEnd by remember { mutableStateOf(false) }
    var watchInProgress by remember { mutableStateOf(true) }
    val pauseGate = remember { GameWatchPauseGate() }
    var watchPaused by remember { mutableStateOf(false) }

    val baseInput = remember(
        tuning,
        seed,
        homeGameplan,
        homeName,
        awayName,
        homeSimRoster,
        homeOffensiveScheme,
        homeDefensiveScheme,
        userTeamIsHome,
        homeVenue,
        userOffensiveRating,
        userDefensiveRating,
        awayOffensiveRating,
        awayDefensiveRating,
    ) {
        if (userTeamIsHome) {
            GameSimInput(
                home = GameSimTeamInput(
                    homeName,
                    userOffensiveRating,
                    userDefensiveRating,
                    offensiveScheme = homeOffensiveScheme,
                    defensiveScheme = homeDefensiveScheme,
                ),
                away = GameSimTeamInput(awayName, awayOffensiveRating, awayDefensiveRating),
                tuning = tuning,
                seed = seed,
                homeGameplan = homeGameplan,
                homeRoster = homeSimRoster,
                homeVenue = homeVenue,
            )
        } else {
            GameSimInput(
                home = GameSimTeamInput(homeName, awayOffensiveRating, awayDefensiveRating),
                away = GameSimTeamInput(
                    awayName,
                    userOffensiveRating,
                    userDefensiveRating,
                    offensiveScheme = homeOffensiveScheme,
                    defensiveScheme = homeDefensiveScheme,
                ),
                tuning = tuning,
                seed = seed,
                awayGameplan = homeGameplan,
                awayRoster = homeSimRoster,
                homeVenue = SimHomeVenueType.Home,
            )
        }
    }

    LaunchedEffect(baseInput) {
        if (homeRosterPlayers.isNotEmpty()) {
            val validation = GameLineupPrepValidator.validate(homeRosterPlayers, homeStarters)
            if (!validation.canStartSim) {
                watchInProgress = false
                scoreLine = context.getString(R.string.strategy_lineup_sim_blocked)
                return@LaunchedEffect
            }
        }
        scoreLine = context.getString(R.string.game_watch_score_start)
        val result = GameWatchPlayback.watch(
            input = baseInput,
            injuryRoll = injuryRoll,
            pauseGate = pauseGate,
            onLineDelayMs = {
                if (simToEnd) 0L else GameWatchPlayback.lineDelayMs(selectedSpeedMultiplier)
            },
            onLine = { step ->
                watchChrome = step.chrome
                lines.add(
                    GameWatchFeedRow(
                        logLine = step.logLine,
                        shotClockSeconds = step.chrome.shotClockSeconds,
                    ),
                )
            },
            onHalftime = { partial, _ ->
                halftimeScouting = HalftimeScoutingSnapshot.fromHalftimePartial(
                    userIsHome = userTeamIsHome,
                    homeName = homeName,
                    awayName = awayName,
                    partial = partial,
                    homeRating = baseInput.home,
                    awayRating = baseInput.away,
                )
                halftimeScore = context.getString(
                    R.string.game_watch_score_half,
                    partial.homeScore,
                    partial.awayScore,
                )
                val resolvedStarters = if (homeStarters.isNotEmpty()) {
                    homeStarters
                } else {
                    GameLineupPrep.resolveStarters(homeRosterPlayers, null)
                }
                halftimeAdjustments = HalftimeCoachAdjustments(
                    gameplan = homeGameplan,
                    offensiveScheme = homeOffensiveScheme,
                    defensiveScheme = homeDefensiveScheme,
                    starters = resolvedStarters,
                )
                halftimeOpen = true
                scoreLine = halftimeScore
                suspendCancellableCoroutine { cont ->
                    resumeGate.value = { adj ->
                        val teamId = homeSimRoster?.teamId ?: homeName
                        val newRoster = if (homeRosterPlayers.isNotEmpty()) {
                            val built = GameLineupPrep.build(teamId, homeRosterPlayers, adj.starters)
                            HalftimeRosterAdjust.applyRotationEmphasis(built, adj.rotationEmphasis)
                        } else {
                            homeSimRoster
                        }
                        onHomeHalftimeAdjustments(adj)
                        halftimeOpen = false
                        cont.resume(
                            if (userTeamIsHome) {
                                baseInput.copy(
                                    home = baseInput.home.copy(
                                        offensiveScheme = adj.offensiveScheme,
                                        defensiveScheme = adj.defensiveScheme,
                                    ),
                                    homeHalftimeGameplan = adj.gameplan,
                                    homeRoster = newRoster,
                                )
                            } else {
                                baseInput.copy(
                                    away = baseInput.away.copy(
                                        offensiveScheme = adj.offensiveScheme,
                                        defensiveScheme = adj.defensiveScheme,
                                    ),
                                    awayHalftimeGameplan = adj.gameplan,
                                    awayRoster = newRoster,
                                )
                            },
                        )
                        resumeGate.value = null
                    }
                }
            },
        )
        scoreLine = if (result.overtimePeriods > 0) {
            context.getString(
                R.string.game_watch_score_final_ot,
                result.homeScore,
                result.awayScore,
                result.overtimePeriods,
            )
        } else {
            context.getString(
                R.string.game_watch_score_final,
                result.homeScore,
                result.awayScore,
            )
        }
        finalResult = result
        postgameArticle = PostgameReportGenerator.generate(
            userTeamName = homeName,
            opponentName = awayName,
            userScore = result.homeScore,
            opponentScore = result.awayScore,
        )
        result.homeInjuries.forEach { event ->
            lines.add(
                GameWatchFeedRow(
                    logLine = context.getString(
                        R.string.game_watch_injury_log,
                        event.label,
                        event.weeksRemaining,
                    ),
                    shotClockSeconds = null,
                ),
            )
        }
        watchInProgress = false
        onSimComplete(result)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.game_watch_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.game_watch_subtitle, homeName, awayName),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(text = scoreLine, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        watchChrome?.let { chrome ->
            Text(
                text = stringResource(
                    R.string.game_watch_chrome,
                    periodLabel(chrome),
                    chrome.gameClockDisplay,
                    chrome.shotClockSeconds,
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    if (watchPaused) {
                        pauseGate.resume()
                        watchPaused = false
                    } else {
                        pauseGate.pause()
                        watchPaused = true
                    }
                },
                enabled = watchInProgress && !halftimeOpen && !simToEnd,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (watchPaused) R.string.game_watch_resume else R.string.game_watch_pause,
                    ),
                )
            }
        }
        if (watchInProgress && lines.isNotEmpty() && !atLiveEdge) {
            FilterChip(
                selected = false,
                onClick = {
                    followLive = true
                },
                label = { Text(stringResource(R.string.game_watch_feed_follow_live)) },
            )
        }
        GameWatchSpeedControl(
            selectedSpeedMultiplier = selectedSpeedMultiplier,
            simToEnd = simToEnd,
            enabled = watchInProgress && !watchPaused,
            onSelectMultiplier = { mult ->
                simToEnd = false
                selectedSpeedMultiplier = mult
            },
            onSimToEnd = { simToEnd = true },
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(lines) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = row.logLine,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                    row.shotClockSeconds?.let { sc ->
                        Text(
                            text = stringResource(R.string.game_watch_feed_shot_clock, sc),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
        finalResult?.let { result ->
            PostgameBoxScorePanel(
                result = result,
                homeName = homeName,
                awayName = awayName,
                homeRosterPlayers = homeRosterPlayers,
            )
        }
        postgameArticle?.let { article ->
            Text(
                text = stringResource(R.string.game_report_postgame_heading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ReportArticleView(article = article)
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_back))
        }
    }

    PostgameInjuryFlow(
        events = postgameInjuryEvents,
        players = postgameInjuryRoster.ifEmpty { homeRosterPlayers },
        rsWeekIndex = rsWeekIndex,
        medicalRules = medicalRules,
        countsMedicalRedshirt = injuryRoll?.phase != CalendarPhase.Exhibition,
        onDismiss = onDismissPostgameInjuries,
        onApplyMedicalRedshirt = onApplyMedicalRedshirt,
    )

    if (halftimeOpen) {
        HalftimeCoachSheet(
            scoreLine = halftimeScore,
            scouting = halftimeScouting,
            players = homeRosterPlayers,
            state = halftimeAdjustments,
            onStateChange = { halftimeAdjustments = it },
            onContinue = { resumeGate.value?.invoke(halftimeAdjustments) },
        )
    }
}

@Composable
private fun periodLabel(chrome: GameWatchChrome): String = when (chrome.period) {
    GameWatchPeriod.FirstHalf -> stringResource(R.string.game_watch_period_first)
    GameWatchPeriod.SecondHalf -> stringResource(R.string.game_watch_period_second)
    GameWatchPeriod.Overtime -> stringResource(R.string.game_watch_period_ot, chrome.overtimeNumber)
}

@Composable
private fun GameWatchSpeedControl(
    selectedSpeedMultiplier: Double,
    simToEnd: Boolean,
    enabled: Boolean,
    onSelectMultiplier: (Double) -> Unit,
    onSimToEnd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.game_watch_speed_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameWatchSpeedPresets.SPEED_MULTIPLIERS.forEach { mult ->
                val selected = !simToEnd && kotlin.math.abs(selectedSpeedMultiplier - mult) < 0.001
                FilterChip(
                    selected = selected,
                    enabled = enabled,
                    onClick = { onSelectMultiplier(mult) },
                    label = {
                        Text(
                            when (mult) {
                                0.5 -> stringResource(R.string.game_watch_speed_half)
                                1.0 -> stringResource(R.string.game_watch_speed_one)
                                2.0 -> stringResource(R.string.game_watch_speed_two)
                                4.0 -> stringResource(R.string.game_watch_speed_four)
                                else -> "${mult}×"
                            },
                        )
                    },
                )
            }
            FilterChip(
                selected = simToEnd,
                enabled = enabled,
                onClick = onSimToEnd,
                label = { Text(stringResource(R.string.game_watch_speed_sim_to_end)) },
            )
        }
    }
}


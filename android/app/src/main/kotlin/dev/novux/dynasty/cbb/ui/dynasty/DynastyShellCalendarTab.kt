package dev.novux.dynasty.cbb.ui.dynasty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.UserCalendarGameEntry
import dev.novux.dynasty.cbb.core.calendar.UserCalendarRowKind
import dev.novux.dynasty.cbb.core.calendar.UserCalendarScheduleDensityPolicy
import dev.novux.dynasty.cbb.core.calendar.UserCalendarSchedulePolicy
import dev.novux.dynasty.cbb.core.calendar.UserCalendarWeekRow
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind
import dev.novux.dynasty.cbb.data.calendar.UserCalendarRepository
import dev.novux.dynasty.cbb.ui.sim.PostgameBoxScorePanel

@Composable
fun DynastyShellCalendarTab(
    dynastyId: String,
    seasonIndex: Int,
    activePosition: CalendarPosition,
    calendarRepository: UserCalendarRepository,
    contentPadding: PaddingValues,
    rosterPlayers: List<RosterPlayer> = emptyList(),
    userTeamDisplayName: String = "",
    onOpenGamePreview: (UserCalendarGameEntry) -> Unit = {},
    onReplayStagedGame: () -> Unit = {},
) {
    var rows by remember { mutableStateOf<List<UserCalendarWeekRow>>(emptyList()) }
    var boxGame by remember { mutableStateOf<UserCalendarGameEntry?>(null) }
    val boxSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(dynastyId, seasonIndex, activePosition) {
        rows = calendarRepository.loadCalendarRows(dynastyId, seasonIndex, activePosition)
    }

    if (boxGame != null) {
        CalendarGameBoxSheet(
            game = boxGame!!,
            rosterPlayers = rosterPlayers,
            userTeamDisplayName = userTeamDisplayName,
            sheetState = boxSheetState,
            onDismiss = { boxGame = null },
            onReplayStagedGame = {
                boxGame = null
                onReplayStagedGame()
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "calendar_header") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.shell_tab_calendar),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.shell_calendar_your_games_only),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                UserCalendarScheduleDensityPolicy.currentWeekRow(rows)?.let { current ->
                    val count = UserCalendarScheduleDensityPolicy.gamesScheduledInWeek(current)
                    Text(
                        text = stringResource(R.string.shell_calendar_week_density, count),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (rows.isEmpty()) {
            item(key = "calendar_empty") {
                val emptyCopy = if (activePosition.phase == CalendarPhase.Offseason) {
                    stringResource(R.string.shell_calendar_offseason_no_games)
                } else {
                    stringResource(R.string.shell_calendar_no_schedule)
                }
                Text(
                    text = emptyCopy,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(rows, key = { "${it.phase}:${it.weekIndex}" }) { row ->
                CalendarWeekCard(
                    row = row,
                    onGameClick = { game ->
                        when {
                            game.playedResult != null -> boxGame = game
                            !game.isPastWeek -> onOpenGamePreview(game)
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarGameBoxSheet(
    game: UserCalendarGameEntry,
    rosterPlayers: List<RosterPlayer>,
    userTeamDisplayName: String,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onReplayStagedGame: () -> Unit,
) {
    val result = game.playedResult ?: return
    val archive = result.boxArchive
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.shell_calendar_box_score_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.shell_calendar_game_line_result,
                    game.gameDateLabel,
                    game.opponentDisplayName,
                    if (game.homeGame) stringResource(R.string.dynasty_shell_home)
                    else stringResource(R.string.dynasty_shell_away),
                    gameKindLabel(game.gameKind),
                    stringResource(R.string.shell_calendar_played),
                    result.userScore,
                    result.opponentScore,
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            if (archive != null) {
                val homeName = if (result.userTeamIsHome) userTeamDisplayName else game.opponentDisplayName
                val awayName = if (result.userTeamIsHome) game.opponentDisplayName else userTeamDisplayName
                val homeScore = if (result.userTeamIsHome) result.userScore else result.opponentScore
                val awayScore = if (result.userTeamIsHome) result.opponentScore else result.userScore
                val sim = archive.toGameSimResultForUser(
                    userTeamIsHome = result.userTeamIsHome,
                    homeScore = homeScore,
                    awayScore = awayScore,
                )
                PostgameBoxScorePanel(
                    result = sim,
                    homeName = homeName,
                    awayName = awayName,
                    homeRosterPlayers = if (result.userTeamIsHome) rosterPlayers else emptyList(),
                )
            }
            if (result.provisionalReplay) {
                Button(
                    onClick = onReplayStagedGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.shell_calendar_replay_staged_game))
                }
            }
        }
    }
}

@Composable
private fun CalendarWeekCard(
    row: UserCalendarWeekRow,
    onGameClick: (UserCalendarGameEntry) -> Unit,
) {
    val colors = if (row.isCurrentWeek) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = colors) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = row.weekLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (row.isCurrentWeek) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.shell_calendar_current)) })
                }
            }
            when (row.rowKind) {
                UserCalendarRowKind.Bye -> {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.shell_calendar_bye)) })
                }
                UserCalendarRowKind.EventEliminationBye -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.shell_calendar_event_elimination_bye)) },
                        )
                        Text(
                            text = stringResource(
                                R.string.shell_calendar_event_elimination_bye_detail,
                                row.eventEliminationByeEventName.orEmpty(),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                UserCalendarRowKind.ConfTournamentStructuralBye -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.shell_calendar_conf_tourney_structural_bye)) },
                        )
                        Text(
                            text = stringResource(
                                R.string.shell_calendar_conf_tourney_structural_bye_detail,
                                row.confTournamentStructuralByeConferenceName
                                    ?: stringResource(R.string.shell_calendar_conf_tourney_structural_bye_fallback),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                UserCalendarRowKind.NcbaOpeningRoundBye -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.shell_calendar_ncba_opening_round_bye)) },
                        )
                        Text(
                            text = stringResource(R.string.shell_calendar_ncba_opening_round_bye_detail),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                UserCalendarRowKind.OffWeek -> {
                    val copy = if (row.phase == CalendarPhase.Exhibition) {
                        stringResource(R.string.shell_calendar_exhibition_empty)
                    } else {
                        stringResource(R.string.shell_calendar_open_week)
                    }
                    Text(
                        text = copy,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                UserCalendarRowKind.Game -> {
                    val ordered = UserCalendarSchedulePolicy.gamesInChronologicalPlanOrder(row.games)
                    ordered.forEachIndexed { index, game ->
                        val kindChanged = UserCalendarSchedulePolicy.sectionBreakBefore(index, ordered) != null
                        val showSection = row.games.size > 1 && (index == 0 || kindChanged)
                        if (showSection) {
                            Text(
                                text = calendarSectionTitle(game.gameKind.calendarKindBucket()),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        CalendarGameLine(game = game, onClick = { onGameClick(game) })
                    }
                }
            }
        }
    }
}

private enum class CalendarKindBucket {
    Conference,
    NonConference,
    Other,
}

private fun ScheduleGameKind.calendarKindBucket(): CalendarKindBucket = when (this) {
    ScheduleGameKind.Conference -> CalendarKindBucket.Conference
    ScheduleGameKind.NonConference -> CalendarKindBucket.NonConference
    ScheduleGameKind.EarlyEvent,
    ScheduleGameKind.Exhibition,
    -> CalendarKindBucket.Other
}

@Composable
private fun calendarSectionTitle(bucket: CalendarKindBucket): String = when (bucket) {
    CalendarKindBucket.Conference -> stringResource(R.string.shell_calendar_section_conf)
    CalendarKindBucket.NonConference -> stringResource(R.string.shell_calendar_section_nonconf)
    CalendarKindBucket.Other -> stringResource(R.string.shell_calendar_section_other)
}

@Composable
private fun CalendarGameLine(
    game: UserCalendarGameEntry,
    onClick: () -> Unit,
) {
    val result = game.playedResult
    val status = when {
        result != null -> stringResource(R.string.shell_calendar_played)
        game.isPastWeek -> stringResource(R.string.shell_calendar_played)
        else -> stringResource(R.string.shell_calendar_upcoming)
    }
    val location = if (game.homeGame) {
        stringResource(R.string.dynasty_shell_home)
    } else {
        stringResource(R.string.dynasty_shell_away)
    }
    val date = game.gameDateLabel.ifBlank { "—" }
    val line = if (result != null) {
        stringResource(
            R.string.shell_calendar_game_line_result,
            date,
            game.opponentDisplayName,
            location,
            gameKindLabel(game.gameKind),
            status,
            result.userScore,
            result.opponentScore,
        )
    } else {
        stringResource(
            R.string.shell_calendar_game_line,
            date,
            game.opponentDisplayName,
            location,
            gameKindLabel(game.gameKind),
            status,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (game.isTappable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = line,
            style = MaterialTheme.typography.bodyMedium,
            color = if (game.isTappable) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        AssistChip(onClick = if (game.isTappable) onClick else ({}), enabled = game.isTappable, label = {
            Text(gameKindLabel(game.gameKind))
        })
    }
}

@Composable
private fun gameKindLabel(kind: ScheduleGameKind): String = when (kind) {
    ScheduleGameKind.Conference -> stringResource(R.string.shell_calendar_kind_conf)
    ScheduleGameKind.NonConference -> stringResource(R.string.shell_calendar_kind_nonconf)
    ScheduleGameKind.EarlyEvent -> stringResource(R.string.shell_calendar_kind_event)
    ScheduleGameKind.Exhibition -> stringResource(R.string.shell_calendar_kind_exhibition)
}

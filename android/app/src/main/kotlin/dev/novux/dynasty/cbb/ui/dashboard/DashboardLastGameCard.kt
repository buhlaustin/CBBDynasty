package dev.novux.dynasty.cbb.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.dashboard.DashboardLastGameSummary
import dev.novux.dynasty.cbb.core.reports.PostgameReportGenerator
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.ui.reports.ReportArticleView
import dev.novux.dynasty.cbb.ui.sim.PostgameBoxScorePanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardLastGameCard(
    summary: DashboardLastGameSummary,
    rosterPlayers: List<RosterPlayer>,
    onOpenTeamStats: () -> Unit,
    onOpenPlayerStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snap = summary.snapshot
    var showBox by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    val boxSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val location = if (snap.userTeamIsHome) {
        stringResource(R.string.dynasty_shell_home)
    } else {
        stringResource(R.string.dynasty_shell_away)
    }
    val outcome = when {
        snap.userScore > snap.opponentScore -> stringResource(R.string.dashboard_last_game_win)
        snap.userScore < snap.opponentScore -> stringResource(R.string.dashboard_last_game_loss)
        else -> stringResource(R.string.dashboard_last_game_tie)
    }
    val scoreLine = if (snap.userTeamIsHome) {
        "${snap.userScore}–${snap.opponentScore}"
    } else {
        "${snap.userScore}–${snap.opponentScore}"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.dashboard_last_game_heading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.dashboard_last_game_summary,
                    outcome,
                    location,
                    snap.opponentDisplayName,
                    scoreLine,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedButton(
                onClick = {
                    if (summary.hasBoxScore) showBox = true
                },
                enabled = summary.hasBoxScore,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.dashboard_last_game_box_score))
            }
            OutlinedButton(
                onClick = onOpenTeamStats,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.dashboard_last_game_team_stats))
            }
            OutlinedButton(
                onClick = onOpenPlayerStats,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.dashboard_last_game_player_stats))
            }
            OutlinedButton(
                onClick = { showReport = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.dashboard_last_game_postgame_report))
            }
        }
    }

    if (showBox && snap.boxArchive != null) {
        val homeName = if (snap.userTeamIsHome) snap.userTeamDisplayName else snap.opponentDisplayName
        val awayName = if (snap.userTeamIsHome) snap.opponentDisplayName else snap.userTeamDisplayName
        val homeScore = if (snap.userTeamIsHome) snap.userScore else snap.opponentScore
        val awayScore = if (snap.userTeamIsHome) snap.opponentScore else snap.userScore
        val result = snap.boxArchive.toGameSimResultForUser(
            userTeamIsHome = snap.userTeamIsHome,
            homeScore = homeScore,
            awayScore = awayScore,
        )
        ModalBottomSheet(
            onDismissRequest = { showBox = false },
            sheetState = boxSheetState,
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                PostgameBoxScorePanel(
                    result = result,
                    homeName = homeName,
                    awayName = awayName,
                    homeRosterPlayers = if (snap.userTeamIsHome) rosterPlayers else emptyList(),
                )
            }
        }
    }

    if (showReport) {
        val article = PostgameReportGenerator.generate(
            snap.userTeamDisplayName,
            snap.opponentDisplayName,
            snap.userScore,
            snap.opponentScore,
        )
        ModalBottomSheet(
            onDismissRequest = { showReport = false },
            sheetState = reportSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.game_report_postgame_heading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ReportArticleView(article = article)
            }
        }
    }
}

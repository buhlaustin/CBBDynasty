package dev.novux.dynasty.cbb.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.preview.GamePreviewResult
import dev.novux.dynasty.cbb.core.preview.GamePreviewTeamScouting
import dev.novux.dynasty.cbb.core.preview.GamePreviewVenue
import dev.novux.dynasty.cbb.core.reports.PregameReportGenerator
import dev.novux.dynasty.cbb.ui.reports.ReportArticleView

@Composable
fun GamePreviewScreen(
    matchupLabel: String,
    userTeamName: String,
    opponentName: String,
    venuePhrase: String,
    venue: GamePreviewVenue,
    preview: GamePreviewResult,
    onOpenStrategy: () -> Unit,
    onPlayGame: () -> Unit = {},
    onBack: () -> Unit,
) {
    val pregameArticle = remember(userTeamName, opponentName, venuePhrase, preview) {
        PregameReportGenerator.generate(userTeamName, opponentName, venuePhrase, preview)
    }
    val venueLabel = when (venue) {
        GamePreviewVenue.Home -> stringResource(R.string.dynasty_shell_home)
        GamePreviewVenue.Away -> stringResource(R.string.dynasty_shell_away)
        GamePreviewVenue.Neutral -> stringResource(R.string.game_preview_neutral)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.game_preview_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.game_preview_surface_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = matchupLabel, style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.game_preview_venue, venueLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.game_report_pregame_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        ReportArticleView(article = pregameArticle)
        Text(
            text = stringResource(R.string.game_preview_win_pct, preview.userWinPercent),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.game_preview_predicted_score,
                preview.predictedUserScore,
                preview.predictedOpponentScore,
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        val userScout = preview.userScouting
        val oppScout = preview.opponentScouting
        if (userScout != null && oppScout != null) {
            Text(
                text = stringResource(R.string.game_preview_team_heading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            GamePreviewScoutingTable(userScout, oppScout)
        }
        Text(
            text = stringResource(R.string.game_preview_primary_cta_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.game_preview_primary_cta_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onOpenStrategy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.game_preview_open_strategy))
        }
        Text(
            text = stringResource(R.string.game_preview_h2h_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (preview.headToHead.meetings.isEmpty()) {
            Text(
                text = stringResource(R.string.game_preview_h2h_empty),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text(
                text = stringResource(
                    R.string.game_preview_h2h_record,
                    preview.headToHead.userWins,
                    preview.headToHead.userLosses,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            preview.headToHead.meetings.take(5).forEach { meeting ->
                Text(
                    text = stringResource(
                        R.string.game_preview_h2h_meeting,
                        meeting.seasonLabel,
                        meeting.userScore,
                        meeting.opponentScore,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        OutlinedButton(onClick = onPlayGame, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.game_preview_play_game))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_back))
        }
    }
}

@Composable
private fun GamePreviewScoutingTable(
    user: GamePreviewTeamScouting,
    opponent: GamePreviewTeamScouting,
) {
    val statRows = listOf(
        Triple(R.string.game_preview_stat_ppg, user.line.pointsPerGame, opponent.line.pointsPerGame),
        Triple(R.string.game_preview_stat_rpg, user.line.reboundsPerGame, opponent.line.reboundsPerGame),
        Triple(
            R.string.game_preview_stat_off_eff,
            user.line.offensiveEfficiency.toDouble(),
            opponent.line.offensiveEfficiency.toDouble(),
        ),
        Triple(R.string.game_preview_stat_pace, user.line.pace, opponent.line.pace),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(user.displayName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(opponent.displayName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
        statRows.forEach { (labelRes, userVal, oppVal) ->
            GamePreviewScoutingRow(
                label = stringResource(labelRes),
                userValue = formatScoutingValue(labelRes, userVal),
                opponentValue = formatScoutingValue(labelRes, oppVal),
            )
        }
        GamePreviewScoutingRow(
            label = stringResource(R.string.game_preview_stat_net),
            userValue = formatRank(user.line.netRank),
            opponentValue = formatRank(opponent.line.netRank),
        )
        GamePreviewScoutingRow(
            label = stringResource(R.string.game_preview_stat_poll),
            userValue = formatRank(user.line.mediaPollRank),
            opponentValue = formatRank(opponent.line.mediaPollRank),
        )
    }
}

@Composable
private fun GamePreviewScoutingRow(label: String, userValue: String, opponentValue: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(userValue, style = MaterialTheme.typography.bodyMedium)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(opponentValue, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun formatScoutingValue(labelRes: Int, value: Double): String =
    if (labelRes == R.string.game_preview_stat_off_eff) {
        kotlin.math.round(value).toInt().toString()
    } else {
        stringResource(R.string.game_preview_stat_decimal, value)
    }

@Composable
private fun formatRank(rank: Int?): String =
    rank?.let { stringResource(R.string.game_preview_rank_value, it) }
        ?: stringResource(R.string.game_preview_rank_unranked)


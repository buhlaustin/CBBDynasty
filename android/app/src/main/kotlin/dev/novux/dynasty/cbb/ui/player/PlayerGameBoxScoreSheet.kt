package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.games.PlayerGameLogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerGameBoxScoreSheet(
    player: DynastyPlayer,
    entry: PlayerGameLogEntry?,
    onDismiss: () -> Unit,
) {
    if (entry == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.player_box_score_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (entry.fromSimArchive) {
                Text(
                    stringResource(R.string.player_box_score_sim_source),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                stringResource(
                    R.string.player_box_score_matchup,
                    entry.opponentLabel,
                    entry.rsWeekIndex,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                stringResource(
                    R.string.player_box_score_team_score,
                    entry.teamScore,
                    entry.opponentScore,
                    entry.resultLabel,
                    entry.scoreLabel,
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                player.identity.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(
                    R.string.player_game_log_line,
                    entry.points,
                    entry.assists,
                    entry.rebounds,
                    entry.steals,
                    entry.blocks,
                    entry.turnovers,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                stringResource(
                    R.string.player_box_score_shooting,
                    entry.fieldGoalsMade,
                    entry.fieldGoalsAttempted,
                    entry.threePointsMade,
                    entry.threePointsAttempted,
                    entry.freeThrowsMade,
                    entry.freeThrowsAttempted,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.player_box_score_minutes, entry.minutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

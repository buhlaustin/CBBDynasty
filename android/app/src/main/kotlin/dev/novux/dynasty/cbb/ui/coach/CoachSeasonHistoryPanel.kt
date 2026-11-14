package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import dev.novux.dynasty.cbb.core.coach.card.history.CoachSeasonHistory
import dev.novux.dynasty.cbb.core.coach.card.history.CoachSeasonHistoryPolicy

/** Expandable season list for coach History tab ([MEGA-COACH-111]). */
@Composable
fun CoachSeasonHistoryList(
    seasons: List<CoachSeasonHistory>,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = CoachSeasonHistoryPolicy.MEGA_ID
    var expandedSeason by remember { mutableStateOf<Int?>(null) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        seasons.forEach { season ->
            val expanded = expandedSeason == season.seasonIndex
            CoachSeasonHistoryRowCard(
                season = season,
                expanded = expanded,
                onToggle = {
                    expandedSeason = if (expanded) null else season.seasonIndex
                },
            )
        }
    }
}

@Composable
private fun CoachSeasonHistoryRowCard(
    season: CoachSeasonHistory,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(
                    R.string.coach_deep_history_row,
                    season.seasonIndex,
                    season.schoolDisplayName,
                    season.recordLabel,
                ),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.coach_season_history_conf_record, season.conferenceRecordLabel),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.coach_deep_history_postseason, season.postseasonSummary),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (season.highlightLabels.isNotEmpty()) {
                CoachHistoryHighlightChips(season.highlightLabels)
            }
            if (expanded) {
                CoachSeasonHistoryExpandedDetail(season)
            } else {
                Text(
                    stringResource(R.string.coach_season_history_tap_expand),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CoachSeasonHistoryExpandedDetail(season: CoachSeasonHistory) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.coach_season_history_schedule, season.detail.scheduleSummaryLine),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            stringResource(R.string.coach_season_history_tourney, season.detail.tournamentPathLine),
            style = MaterialTheme.typography.bodySmall,
        )
        if (season.detail.newsTagLines.isNotEmpty()) {
            Text(
                stringResource(R.string.coach_season_history_news),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            season.detail.newsTagLines.forEach { tag ->
                Text("• $tag", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoachHistoryHighlightChips(labels: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        labels.forEach { label ->
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

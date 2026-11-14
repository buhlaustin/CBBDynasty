package dev.novux.dynasty.cbb.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview

/** In-season dashboard team overview ([planning/game-frame.md#team-overview], [MEGA-FRAME-034]). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardTeamOverviewCard(
    overview: SchoolHubOverview,
    postseasonSeedLabel: String?,
    onOpenRankings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = overview.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(
                    R.string.dashboard_team_record,
                    overview.record.overallLabel,
                    overview.record.conferenceLabel,
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.dashboard_team_conference, overview.conferenceName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            overview.rankings.conferenceStandingLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = stringResource(R.string.dashboard_team_sos, overview.sosRating),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            RankingsSection(overview, onOpenRankings)
            postseasonSeedLabel?.let { seed ->
                Text(
                    text = stringResource(R.string.dashboard_team_postseason_seed, seed),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RankingsSection(
    overview: SchoolHubOverview,
    onOpenRankings: () -> Unit,
) {
    val rankings = overview.rankings
    if (!rankings.rankingsPublished) {
        Text(
            text = stringResource(R.string.dashboard_team_rankings_unpublished),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
        )
        return
    }
    val net = rankings.netRank
    if (net != null) {
        Text(
            text = stringResource(R.string.school_hub_net_rank, net),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .clickable(onClick = onOpenRankings)
                .padding(vertical = 2.dp),
        )
    }
    val media = rankings.mediaPollRank
    val coaches = rankings.coachesPollRank
    if (media != null || coaches != null) {
        val line = listOfNotNull(
            media?.let { stringResource(R.string.dashboard_team_media_rank, it) },
            coaches?.let { stringResource(R.string.dashboard_team_coaches_rank, it) },
        ).joinToString(separator = " · ")
        Text(
            text = line,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.clickable(onClick = onOpenRankings),
        )
    }
    if (rankings.mediaTop25 || rankings.coachesTop25) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (rankings.mediaTop25) {
                AssistChip(
                    onClick = onOpenRankings,
                    label = { Text(stringResource(R.string.school_hub_media_poll_chip)) },
                )
            }
            if (rankings.coachesTop25) {
                AssistChip(
                    onClick = onOpenRankings,
                    label = { Text(stringResource(R.string.school_hub_coaches_poll_chip)) },
                )
            }
        }
    }
}

package dev.novux.dynasty.cbb.ui.preseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.preseason.PreseasonRankingsReviewUxPolicy
import dev.novux.dynasty.cbb.core.rankings.RankingsHubSnapshot
import dev.novux.dynasty.cbb.core.rankings.RankingsHubTeamRow
import dev.novux.dynasty.cbb.core.rankings.RankingsHubUiPolicy
import dev.novux.dynasty.cbb.data.rankings.RankingsHubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreseasonRankingsReviewBottomSheet(
    dynastyId: String,
    seasonIndex: Int,
    userTeamId: String?,
    rankingsHubRepository: RankingsHubRepository,
    onPublished: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var snapshot by remember { mutableStateOf<RankingsHubSnapshot?>(null) }
    var publishing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dynastyId, seasonIndex) {
        snapshot = withContext(Dispatchers.IO) {
            rankingsHubRepository.load(dynastyId, seasonIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!publishing) onDismiss() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.preseason_rankings_review_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            val data = snapshot
            if (data == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PreseasonRankingsReviewSheetBody(
                    snapshot = data,
                    userTeamId = userTeamId,
                )
                Button(
                    onClick = {
                        scope.launch {
                            publishing = true
                            try {
                                withContext(Dispatchers.IO) {
                                    rankingsHubRepository.publishPreseasonRankings(dynastyId, seasonIndex)
                                }
                                onPublished()
                                onDismiss()
                            } finally {
                                publishing = false
                            }
                        }
                    },
                    enabled = !publishing && !data.published,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (publishing) {
                            stringResource(R.string.preseason_rankings_publishing)
                        } else {
                            stringResource(R.string.rankings_hub_preseason_continue)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PreseasonRankingsReviewSheetBody(
    snapshot: RankingsHubSnapshot,
    userTeamId: String?,
) {
    Text(
        text = stringResource(R.string.rankings_hub_preseason_review),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PreseasonRankingsReviewUxPolicy.userProgramRanks(
        userTeamId,
        snapshot.net,
        snapshot.media,
        snapshot.coaches,
    )?.let { ranks ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ranks.netRank?.let {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(stringResource(R.string.preseason_rankings_chip_net, it)) },
                )
            }
            ranks.mediaRank?.let {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(stringResource(R.string.preseason_rankings_chip_media, it)) },
                )
            }
            ranks.coachesRank?.let {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(stringResource(R.string.preseason_rankings_chip_coaches, it)) },
                )
            }
        }
    }
    Text(
        text = stringResource(R.string.preseason_rankings_preview_heading),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = stringResource(R.string.preseason_rankings_preview_caption),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            snapshot.net.take(RankingsHubUiPolicy.UNPUBLISHED_PREVIEW_CAP),
            key = { it.teamId },
        ) { row ->
            RankingsPreviewRow(row)
        }
    }
}

@Composable
private fun RankingsPreviewRow(row: RankingsHubTeamRow) {
    val weight = if (row.top25Highlight) FontWeight.SemiBold else FontWeight.Normal
    val color = if (row.top25Highlight) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = stringResource(R.string.rankings_hub_team_row, row.rank, row.displayName),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = weight,
        color = color,
    )
}

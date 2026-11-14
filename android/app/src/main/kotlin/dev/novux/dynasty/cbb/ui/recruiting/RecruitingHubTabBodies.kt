package dev.novux.dynasty.cbb.ui.recruiting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.portal.PortalEntrant
import dev.novux.dynasty.cbb.core.portal.PortalRecruitingHub
import dev.novux.dynasty.cbb.core.recruiting.HsJucoBoard
import dev.novux.dynasty.cbb.core.recruiting.RecruitProspect
import dev.novux.dynasty.cbb.core.recruiting.RecruitScoutLetterEngine
import dev.novux.dynasty.cbb.ui.recruiting.RecruitScoutLetterSheet
import dev.novux.dynasty.cbb.core.signingday.NationalRankRow
import dev.novux.dynasty.cbb.core.signingday.NationalRecruitingRankings

@Composable
fun HsJucoTabBody(
    board: HsJucoBoard?,
    top60: List<NationalRankRow>,
    poolLoaded: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!poolLoaded) {
        Text(
            stringResource(R.string.recruiting_hs_juco_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    if (board == null || board.totalCount == 0) {
        Text(
            stringResource(R.string.recruiting_hs_juco_empty),
            style = MaterialTheme.typography.bodyLarge,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                stringResource(
                    R.string.recruiting_hs_juco_pool_summary,
                    board.highSchool.size,
                    board.juco.size,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (top60.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.recruiting_hs_top60_heading),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.recruiting_hs_top60_pool_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            items(top60, key = { it.prospect.prospectId }) { row ->
                HsJucoProspectRow(
                    prospect = row.prospect,
                    rankPrefix = stringResource(R.string.recruiting_hs_rank_prefix, row.rank),
                )
            }
        }
        if (board.highSchool.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.recruiting_hs_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(board.highSchool, key = { "hs-${it.prospectId}" }) { prospect ->
                HsJucoProspectRow(prospect = prospect)
            }
        }
        if (board.juco.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.recruiting_juco_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(board.juco, key = { "juco-${it.prospectId}" }) { prospect ->
                HsJucoProspectRow(prospect = prospect)
            }
        }
    }
}

@Composable
fun RecruitPoolListTabBody(
    prospects: List<RecruitProspect>,
    emptyText: String,
    watchlistIds: Set<String>,
    onToggleBoard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (prospects.isEmpty()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(prospects, key = { it.prospectId }) { prospect ->
                HsJucoProspectRow(
                    prospect = prospect,
                    onToggleBoard = onToggleBoard,
                    onBoard = prospect.prospectId in watchlistIds,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HsJucoProspectRow(
    prospect: RecruitProspect,
    rankPrefix: String? = null,
    onBoard: Boolean = false,
    onToggleBoard: ((String) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = buildString {
                rankPrefix?.let { append("$it · ") }
                append(prospect.displayName)
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            stringResource(
                R.string.recruiting_row_meta,
                prospect.stars,
                prospect.position.label,
                prospect.homeState,
                prospect.scoutLetterOverall,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (prospect.topPriorities.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                prospect.topPriorities.forEach { priority ->
                    AssistChip(onClick = {}, label = { Text(priority.chipLabel) })
                }
            }
        }
        RecruitScoutLetterSheet(letters = RecruitScoutLetterEngine.fromProspect(prospect))
        onToggleBoard?.let { toggle ->
            OutlinedButton(onClick = { toggle(prospect.prospectId) }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(
                        if (onBoard) R.string.recruiting_board_remove else R.string.recruiting_board_add,
                    ),
                )
            }
        }
    }
}

@Composable
fun PortalRecruitingTabBody(
    windowOpen: Boolean,
    modifier: Modifier = Modifier,
    top40Content: @Composable () -> Unit = {},
    recommendedContent: @Composable () -> Unit = {},
) {
    if (!windowOpen) {
        Text(
            stringResource(R.string.recruiting_portal_window_closed),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier,
        )
        return
    }
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        top40Content()
        recommendedContent()
    }
}

@Composable
fun PortalTop40Section(
    entrants: List<PortalEntrant>,
    highlightEntrantId: String? = null,
    modifier: Modifier = Modifier,
) {
    val ranked = PortalRecruitingHub.rankedEntrants(entrants)
    if (ranked.isEmpty()) {
        Text(
            stringResource(R.string.recruiting_portal_top40_pending),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.recruiting_portal_top40_heading),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        ranked.forEach { entrant ->
            val highlighted = entrant.entrantId == highlightEntrantId
            Text(
                stringResource(
                    R.string.recruiting_portal_entrant_row,
                    entrant.nationalTop40Rank?.let { "#$it" } ?: "—",
                    entrant.stars ?: 0,
                    entrant.classYear.name,
                    entrant.priorProgramId,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

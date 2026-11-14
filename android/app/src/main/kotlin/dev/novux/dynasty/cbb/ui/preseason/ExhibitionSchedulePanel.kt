package dev.novux.dynasty.cbb.ui.preseason

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.preseason.PreseasonTaskId
import dev.novux.dynasty.cbb.core.schedule.ExhibitionGameDraft
import dev.novux.dynasty.cbb.core.schedule.ExhibitionDraftIssue
import dev.novux.dynasty.cbb.core.schedule.ExhibitionScheduleUxPolicy
import dev.novux.dynasty.cbb.core.schedule.PreseasonScheduleConstants
import dev.novux.dynasty.cbb.data.preseason.PreseasonProgressRepository
import dev.novux.dynasty.cbb.data.preseason.PreseasonScheduleRepository
import kotlinx.coroutines.launch

@Composable
fun ExhibitionSchedulePanel(
    dynastyId: String,
    seasonIndex: Int,
    scheduleRepository: PreseasonScheduleRepository,
    progressRepository: PreseasonProgressRepository,
    onSaved: () -> Unit,
    onDraftIssueChanged: (ExhibitionDraftIssue?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var count by remember { mutableIntStateOf(0) }
    val drafts = remember { mutableStateListOf<ExhibitionGameDraft>() }
    var opponents by remember { mutableStateOf<List<TeamDisplayOverlay>>(emptyList()) }
    var pickerSlotIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dynastyId, seasonIndex) {
        count = scheduleRepository.loadExhibitionCount(dynastyId, seasonIndex)
        drafts.clear()
        drafts.addAll(scheduleRepository.loadExhibitionDrafts(dynastyId, seasonIndex))
        opponents = scheduleRepository.loadNonConferenceOpponents(dynastyId)
        resizeDrafts(drafts, count)
    }

  fun syncDraftsToCount(newCount: Int) {
        count = newCount
        resizeDrafts(drafts, newCount)
    }

    val draftIssue = ExhibitionScheduleUxPolicy.draftIssue(count, drafts.toList())
    val canSave = draftIssue == null

    LaunchedEffect(draftIssue) {
        onDraftIssueChanged(draftIssue)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.preseason_exhibitions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.preseason_exhibitions_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (count == 0) {
            Text(
                text = stringResource(R.string.preseason_exhibitions_zero_week_copy),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        draftIssue?.let { issue ->
            Text(
                text = exhibitionDraftIssueMessage(issue),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..PreseasonScheduleConstants.MAX_EXHIBITION_GAMES).forEach { option ->
                FilterChip(
                    selected = count == option,
                    onClick = { syncDraftsToCount(option) },
                    label = { Text(option.toString()) },
                )
            }
        }
        if (count > 0) {
            drafts.forEachIndexed { index, draft ->
                ExhibitionSlotCard(
                    slotIndex = index,
                    draft = draft,
                    onPickOpponent = { pickerSlotIndex = index },
                    onHomeAway = { home ->
                        drafts[index] = draft.copy(homeGame = home)
                    },
                    onWeek = { week ->
                        drafts[index] = draft.copy(exhibitionWeekIndex = week)
                    },
                )
            }
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    scheduleRepository.saveExhibitions(dynastyId, seasonIndex, count, drafts.toList())
                    progressRepository.completeTask(dynastyId, seasonIndex, PreseasonTaskId.ScheduleExhibitions)
                    onSaved()
                }
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (count == 0) {
                    stringResource(R.string.preseason_exhibitions_skip)
                } else {
                    stringResource(R.string.preseason_exhibitions_confirm, count)
                },
            )
        }
    }

    pickerSlotIndex?.let { slot ->
        ExhibitionOpponentPickerSheet(
            opponents = opponents,
            onSelect = { team ->
                drafts[slot] = drafts[slot].copy(
                    opponentTeamId = team.teamId,
                    opponentDisplayName = team.displayName,
                )
                pickerSlotIndex = null
            },
            onDismiss = { pickerSlotIndex = null },
        )
    }
}

private fun resizeDrafts(drafts: MutableList<ExhibitionGameDraft>, count: Int) {
    when {
        drafts.size < count -> {
            repeat(count - drafts.size) { addIndex ->
                drafts.add(
                    ExhibitionGameDraft(
                        exhibitionWeekIndex = (drafts.size + addIndex + 1)
                            .coerceAtMost(PreseasonScheduleConstants.MAX_EXHIBITION_GAMES),
                    ),
                )
            }
        }
        drafts.size > count -> {
            while (drafts.size > count) {
                drafts.removeAt(drafts.lastIndex)
            }
        }
    }
}

@Composable
private fun ExhibitionSlotCard(
    slotIndex: Int,
    draft: ExhibitionGameDraft,
    onPickOpponent: () -> Unit,
    onHomeAway: (Boolean) -> Unit,
    onWeek: (Int) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.preseason_exhibitions_slot_title, slotIndex + 1),
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedButton(onClick = onPickOpponent, modifier = Modifier.fillMaxWidth()) {
                Text(
                    draft.opponentDisplayName.ifBlank {
                        stringResource(R.string.preseason_exhibitions_pick_opponent)
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = draft.homeGame,
                    onClick = { onHomeAway(true) },
                    label = { Text(stringResource(R.string.preseason_exhibitions_home)) },
                )
                FilterChip(
                    selected = !draft.homeGame,
                    onClick = { onHomeAway(false) },
                    label = { Text(stringResource(R.string.preseason_exhibitions_away)) },
                )
            }
            Text(
                text = stringResource(R.string.preseason_exhibitions_week_label),
                style = MaterialTheme.typography.labelMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExhibitionScheduleUxPolicy.exhibitionWeekIndices.forEach { week ->
                    FilterChip(
                        selected = draft.exhibitionWeekIndex == week,
                        onClick = { onWeek(week) },
                        label = { Text(stringResource(R.string.preseason_exhibitions_week_chip, week)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun exhibitionDraftIssueMessage(issue: ExhibitionDraftIssue): String = when (issue) {
    ExhibitionDraftIssue.MissingOpponent -> stringResource(R.string.preseason_exhibitions_error_opponent)
    ExhibitionDraftIssue.InvalidWeek -> stringResource(R.string.preseason_exhibitions_error_week)
    ExhibitionDraftIssue.DuplicateWeek -> stringResource(R.string.preseason_exhibitions_error_duplicate_week)
    ExhibitionDraftIssue.DraftCountMismatch -> stringResource(R.string.preseason_exhibitions_error_draft_count)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExhibitionOpponentPickerSheet(
    opponents: List<TeamDisplayOverlay>,
    onSelect: (TeamDisplayOverlay) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = opponents.filter {
        query.isBlank() || it.displayName.contains(query, ignoreCase = true)
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.preseason_exhibitions_opponent_search_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.preseason_exhibitions_opponent_search_hint)) },
                singleLine = true,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(filtered, key = { it.teamId }) { team ->
                    OutlinedButton(
                        onClick = { onSelect(team) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(team.displayName)
                    }
                }
            }
        }
    }
}

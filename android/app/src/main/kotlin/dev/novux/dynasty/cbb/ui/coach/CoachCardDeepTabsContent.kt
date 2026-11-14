package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.deep.CoachCardDeepTabsSnapshot
import dev.novux.dynasty.cbb.core.coach.card.deep.CoachDraftResumeEntry
import dev.novux.dynasty.cbb.core.coach.card.deep.CoachHofResumeEntry
@Composable
fun CoachCardHistoryDeepTab(snapshot: CoachCardDeepTabsSnapshot?) {
    Text(stringResource(R.string.coach_tab_history), style = MaterialTheme.typography.titleMedium)
    val rows = snapshot?.seasonHistory.orEmpty()
    if (rows.isEmpty()) {
        Text(
            stringResource(R.string.coach_deep_history_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.coach_history_accumulates),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    CoachSeasonHistoryList(seasons = rows)
}

@Composable
fun CoachCardHofDeepTab(snapshot: CoachCardDeepTabsSnapshot?) {
    Text(stringResource(R.string.coach_tab_hof), style = MaterialTheme.typography.titleMedium)
    val entries = snapshot?.hallOfFame.orEmpty()
    if (entries.isEmpty()) {
        Text(
            stringResource(R.string.coach_deep_hof_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    entries.forEach { entry -> CoachHofResumeCard(entry) }
}

@Composable
private fun CoachHofResumeCard(entry: CoachHofResumeEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(entry.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                if (entry.isNational) {
                    stringResource(
                        R.string.coach_deep_hof_national,
                        entry.schoolLabel,
                        entry.honorSeasonIndex,
                    )
                } else {
                    stringResource(
                        R.string.coach_deep_hof_conference,
                        entry.schoolLabel,
                        entry.honorSeasonIndex,
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun CoachCardDraftDeepTab(snapshot: CoachCardDeepTabsSnapshot?) {
    Text(stringResource(R.string.coach_tab_draft), style = MaterialTheme.typography.titleMedium)
    val entries = snapshot?.draftDepartures.orEmpty()
    if (entries.isEmpty()) {
        Text(
            stringResource(R.string.coach_deep_draft_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    entries.forEach { entry -> CoachDraftResumeCard(entry) }
}

@Composable
private fun CoachDraftResumeCard(entry: CoachDraftResumeEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(entry.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            val pickSummary = entry.pickSummary
            val detail = when {
                entry.drafted && !pickSummary.isNullOrBlank() ->
                    stringResource(
                        R.string.coach_deep_draft_picked,
                        entry.draftSeasonIndex,
                        pickSummary,
                        entry.schoolLabel,
                    )
                entry.drafted ->
                    stringResource(R.string.coach_deep_draft_picked_unknown, entry.draftSeasonIndex, entry.schoolLabel)
                else ->
                    stringResource(R.string.coach_deep_draft_undrafted, entry.draftSeasonIndex, entry.schoolLabel)
            }
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

package dev.novux.dynasty.cbb.ui.newgame

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.pack.ConferenceDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.TeamDisplayOverlay
import dev.novux.dynasty.cbb.core.pack.NewGameStarterPrestigeFilterPolicy
import dev.novux.dynasty.cbb.core.save.NewGameSchoolSummaryPreview
import dev.novux.dynasty.cbb.core.save.NewGameSchoolSummaryPreviewEngine
import dev.novux.dynasty.cbb.core.balance.BalanceNilAnnualPoolTables
import dev.novux.dynasty.cbb.core.balance.BalanceRecruitingEconomy
import java.text.NumberFormat
import java.util.Locale

enum class SchoolSortMode {
    Name,
    Prestige,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolStepContent(
    schools: List<TeamDisplayOverlay>,
    conferences: List<ConferenceDisplayOverlay>,
    unlockAll: Boolean,
    onUnlockAllChange: (Boolean) -> Unit,
    selectedTeamId: String?,
    onSelectTeam: (String) -> Unit,
    summaryAcknowledged: Boolean,
    onSummaryAcknowledged: () -> Unit,
    recruitingEconomy: BalanceRecruitingEconomy,
    nilPoolTables: BalanceNilAnnualPoolTables,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(SchoolSortMode.Prestige) }
    var previewTeam by remember { mutableStateOf<TeamDisplayOverlay?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val confNames = remember(conferences) { conferences.associate { it.conferenceId to it.displayName } }

    val visible = remember(schools, query, sortMode, unlockAll) {
        val q = query.trim().lowercase()
        schools
            .asSequence()
            .filter { NewGameStarterPrestigeFilterPolicy.isVisible(it.programPrestige, unlockAll) }
            .filter {
                q.isEmpty() ||
                    it.displayName.lowercase().contains(q) ||
                    it.shortName.lowercase().contains(q) ||
                    confNames[it.conferenceId]?.lowercase()?.contains(q) == true
            }
            .sortedWith(
                when (sortMode) {
                    SchoolSortMode.Name -> compareBy { it.displayName.lowercase() }
                    SchoolSortMode.Prestige -> compareBy<TeamDisplayOverlay> { it.programPrestige }
                        .thenBy { it.displayName.lowercase() }
                },
            )
            .toList()
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.new_school_search)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(onClick = { onUnlockAllChange(!unlockAll) }) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = stringResource(
                        if (unlockAll) R.string.new_school_unlock_on else R.string.new_school_unlock_off,
                    ),
                    tint = if (unlockAll) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        Text(
            text = stringResource(
                if (unlockAll) R.string.new_school_filter_all else R.string.new_school_filter_starter,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.new_school_sort_prestige),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (sortMode == SchoolSortMode.Prestige) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { sortMode = SchoolSortMode.Prestige },
            )
            Text(
                text = stringResource(R.string.new_school_sort_name),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (sortMode == SchoolSortMode.Name) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { sortMode = SchoolSortMode.Name },
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(visible, key = { it.teamId }) { school ->
                val selected = school.teamId == selectedTeamId
                SchoolRow(
                    school = school,
                    conferenceName = confNames[school.conferenceId].orEmpty(),
                    selected = selected,
                    onClick = {
                        onSelectTeam(school.teamId)
                        previewTeam = school
                    },
                )
            }
        }
        if (selectedTeamId != null && summaryAcknowledged) {
            Text(
                text = stringResource(R.string.new_school_selected_ok),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    previewTeam?.let { team ->
        ModalBottomSheet(
            onDismissRequest = { previewTeam = null },
            sheetState = sheetState,
        ) {
            SchoolSummarySheet(
                school = team,
                conferenceName = confNames[team.conferenceId].orEmpty(),
                preview = NewGameSchoolSummaryPreviewEngine.build(team, recruitingEconomy, nilPoolTables),
                onConfirm = {
                    onSummaryAcknowledged()
                    previewTeam = null
                },
            )
        }
    }
}

@Composable
private fun SchoolRow(
    school: TeamDisplayOverlay,
    conferenceName: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val line = "${school.displayName} · $conferenceName · Prestige ${school.programPrestige}"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = line,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SchoolSummarySheet(
    school: TeamDisplayOverlay,
    conferenceName: String,
    preview: NewGameSchoolSummaryPreview,
    onConfirm: () -> Unit,
) {
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val pipelineLabel = preview.pipelineStates.sorted().joinToString(", ")
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(school.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        SummaryLine(stringResource(R.string.new_school_summary_prestige), school.programPrestige.toString())
        SummaryLine(stringResource(R.string.new_school_summary_conference), conferenceName)
        SummaryLine(
            stringResource(R.string.new_school_summary_location),
            "${school.homeCity}, ${school.homeState}",
        )
        SummaryLine(stringResource(R.string.new_school_summary_pipeline), pipelineLabel)
        SummaryLine(
            stringResource(R.string.new_school_summary_weekly_points),
            preview.weeklyRecruitingPoints.toString(),
        )
        SummaryLine(
            stringResource(R.string.new_school_summary_annual_nil),
            currency.format(preview.annualNilUsd),
        )
        Text(
            text = stringResource(R.string.new_school_summary_recruiting_nil_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(R.string.new_school_summary_contract_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
        androidx.compose.material3.Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
        ) {
            Text(stringResource(R.string.new_school_summary_confirm))
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}

package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.ui.portrait.CoachPortraitSlot

@Composable
fun CoachCardIdentityHeader(
    displayName: String,
    schoolDisplayName: String,
    seasonRecordLabel: String,
    roleLabel: String,
    avatarPresetId: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoachPortraitSlot(displayName = displayName, avatarPresetId = avatarPresetId)
        Column {
            Text(displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(roleLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(schoolDisplayName, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.coach_card_record, seasonRecordLabel),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun coachTabLabel(tab: CoachCardTab): String = when (tab) {
    CoachCardTab.Overview -> stringResource(R.string.coach_tab_overview)
    CoachCardTab.Contract -> stringResource(R.string.coach_tab_contract)
    CoachCardTab.History -> stringResource(R.string.coach_tab_history)
    CoachCardTab.HallOfFame -> stringResource(R.string.coach_tab_hof)
    CoachCardTab.Draft -> stringResource(R.string.coach_tab_draft)
}

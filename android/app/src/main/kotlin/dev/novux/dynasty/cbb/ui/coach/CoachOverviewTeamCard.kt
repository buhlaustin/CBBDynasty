package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

/**
 * Overview tab team card; tap opens School hub when [onOpenSchoolHub] is set ([MEGA-COACH-033]).
 */
@Composable
fun CoachOverviewTeamCard(
    schoolDisplayName: String,
    seasonRecordLabel: String,
    schoolHubOverview: SchoolHubOverview?,
    onOpenSchoolHub: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val openable = schoolHubOverview != null && onOpenSchoolHub != null
    val cardModifier = modifier
        .fillMaxWidth()
        .then(
            if (openable) {
                Modifier.clickable(onClick = onOpenSchoolHub!!)
            } else {
                Modifier
            },
        )
    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.coach_overview_team_card_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val title = schoolHubOverview?.displayName ?: schoolDisplayName
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            schoolHubOverview?.let { overview ->
                Text(
                    stringResource(R.string.school_hub_conference, overview.conferenceName),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    stringResource(
                        R.string.school_hub_record,
                        overview.record.overallLabel,
                        overview.record.conferenceLabel,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    stringResource(R.string.school_hub_prestige, overview.programPrestige),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } ?: Text(
                stringResource(R.string.coach_card_record, seasonRecordLabel),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (openable) {
                Text(
                    stringResource(R.string.coach_overview_open_school_hub),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

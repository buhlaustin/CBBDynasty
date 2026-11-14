package dev.novux.dynasty.cbb.ui.school

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
import androidx.compose.runtime.remember
import dev.novux.dynasty.cbb.R
import java.text.NumberFormat
import java.util.Locale
import dev.novux.dynasty.cbb.core.qa.SchoolHubAcademicsPath
import dev.novux.dynasty.cbb.core.school.SchoolHubOverview

@Composable
fun SchoolHubOverviewCard(
    overview: SchoolHubOverview,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(overview.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (!overview.isUserSchool) {
                Text(
                    stringResource(R.string.school_hub_read_only),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                stringResource(R.string.school_hub_conference, overview.conferenceName),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.school_hub_record, overview.record.overallLabel, overview.record.conferenceLabel),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                stringResource(R.string.school_hub_prestige, overview.programPrestige),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (overview.isUserSchool) {
                @Suppress("unused")
                val qaAcademics = SchoolHubAcademicsPath.MEGA_ID
                overview.programAcademicsGrade?.let { grade ->
                    Text(
                        stringResource(R.string.school_hub_academics_grade, grade),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    overview.recruitingBudgetMultiplier?.let { multiplier ->
                        Text(
                            stringResource(R.string.school_hub_academics_budget_multiplier, multiplier),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.school_hub_sos, overview.sosRating),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.school_hub_power_index, overview.powerIndex),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(
                    R.string.school_hub_team_ratings,
                    overview.teamOffRating,
                    overview.teamDefRating,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            overview.facilitySummary?.let { facilities ->
                Text(
                    stringResource(R.string.school_hub_facility_avg, facilities.averageGrade),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    stringResource(
                        R.string.school_hub_facility_lounge,
                        facilities.byCategory.getValue(dev.novux.dynasty.cbb.core.offseason.weeknine.FacilityCategory.RecruitingLounge),
                        facilities.recruitingPitchMultiplier,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            overview.nilCycleSummary?.let { nil ->
                @Suppress("unused")
                val qaNil = dev.novux.dynasty.cbb.core.school.SchoolHubNilCycleSummaryPolicy.MEGA_ID
                val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }
                Text(
                    stringResource(
                        R.string.school_hub_nil_cycle_summary,
                        currency.format(nil.poolAnnualUsd),
                        currency.format(nil.remainingUsd),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            RankingsBlock(overview)
        }
    }
}

@Composable
private fun RankingsBlock(overview: SchoolHubOverview) {
    val rankings = overview.rankings
    if (!rankings.rankingsPublished) {
        Text(
            stringResource(R.string.school_hub_rankings_unpublished),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Text(
        stringResource(
            R.string.school_hub_net_rank,
            rankings.netRank ?: 0,
        ),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    rankings.mediaPollRank?.let {
        Text(stringResource(R.string.school_hub_media_rank, it), style = MaterialTheme.typography.bodySmall)
        if (rankings.mediaTop25) {
            Text(stringResource(R.string.school_hub_media_poll_chip), style = MaterialTheme.typography.labelSmall)
        }
    }
    rankings.coachesPollRank?.let {
        Text(stringResource(R.string.school_hub_coaches_rank, it), style = MaterialTheme.typography.bodySmall)
        if (rankings.coachesTop25) {
            Text(stringResource(R.string.school_hub_coaches_poll_chip), style = MaterialTheme.typography.labelSmall)
        }
    }
    rankings.conferenceStandingLabel?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
}

package dev.novux.dynasty.cbb.ui.seasonreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewSnapshot
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SeasonReviewScrollContent(
    snapshot: SeasonReviewSnapshot,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    showAcknowledgeAction: Boolean = false,
    onAcknowledge: () -> Unit = {},
) {
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ReviewSection(stringResource(R.string.season_review_section_season)) {
            Text(snapshot.schoolDisplayName, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.season_review_record_line, snapshot.record.overallLabel, snapshot.record.conferenceLabel))
            Text(snapshot.postseasonFinishLabel, style = MaterialTheme.typography.bodyMedium)
        }
        ReviewSection(stringResource(R.string.season_review_section_rankings)) {
            RankLine(stringResource(R.string.season_review_net_rank), snapshot.netRank, top25 = false)
            RankLine(stringResource(R.string.season_review_media_poll), snapshot.mediaPollRank, snapshot.mediaTop25)
            RankLine(stringResource(R.string.season_review_coaches_poll), snapshot.coachesPollRank, snapshot.coachesTop25)
        }
        ReviewSection(stringResource(R.string.season_review_section_prestige)) {
            Text(
                stringResource(
                    R.string.season_review_prestige_line,
                    snapshot.prestigeStart,
                    snapshot.prestigeEnd,
                ),
            )
            val delta = snapshot.prestigeEnd - snapshot.prestigeStart
            if (delta != 0) {
                val sign = if (delta > 0) "+" else ""
                Text(stringResource(R.string.season_review_prestige_delta, "$sign$delta"))
            }
        }
        ReviewSection(stringResource(R.string.season_review_section_facilities)) {
            Text(
                stringResource(R.string.season_review_facility_avg, snapshot.facilityGradeAverage),
                style = MaterialTheme.typography.bodyMedium,
            )
            snapshot.facilityGrades.forEach { row ->
                val deltaText = when {
                    row.deltaFromBaseline > 0 -> stringResource(R.string.season_review_grade_delta_up, row.deltaFromBaseline)
                    row.deltaFromBaseline < 0 -> stringResource(R.string.season_review_grade_delta_down, -row.deltaFromBaseline)
                    else -> ""
                }
                Text(
                    "${row.category.label}: ${row.grade}$deltaText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        ReviewSection(stringResource(R.string.season_review_section_recruiting)) {
            Text(stringResource(R.string.season_review_recruiting_tier, snapshot.recruitingTierLabel))
            Text(stringResource(R.string.season_review_recruiting_this_cycle, snapshot.currentCycleWeeklyPool))
            Text(
                stringResource(R.string.season_review_recruiting_next_cycle, snapshot.nextCycleWeeklyPool),
                fontWeight = FontWeight.Medium,
            )
            Text(
                stringResource(
                    R.string.season_review_nil_next_cycle,
                    currency.format(snapshot.nextCycleNilPoolUsd),
                ),
                fontWeight = FontWeight.Medium,
            )
            Text(
                stringResource(R.string.season_review_next_cycle_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (snapshot.awardHighlights.isNotEmpty()) {
            ReviewSection(stringResource(R.string.season_review_section_awards)) {
                snapshot.awardHighlights.forEach { line ->
                    Text(
                        stringResource(R.string.awards_winner_line, line.awardLabel, line.winnerName),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    line.runnerUpName?.let {
                        Text(
                            stringResource(R.string.awards_runner_up_line, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        ReviewSection(stringResource(R.string.season_review_section_coach)) {
            Text(stringResource(R.string.season_review_job_security, snapshot.jobSecurityScore, snapshot.jobSecurityLabel))
            Text(stringResource(R.string.season_review_ad_trust, snapshot.adTrustScore))
            Text(stringResource(R.string.season_review_contract_years, snapshot.contractYearsLeft))
        }
        if (showAcknowledgeAction) {
            Button(onClick = onAcknowledge, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.season_review_acknowledge_recap))
            }
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun RankLine(label: String, rank: Int?, top25: Boolean) {
    val rankText = rank?.let { "#$it" } ?: stringResource(R.string.season_review_rank_unranked)
    val suffix = if (top25) " · ${stringResource(R.string.season_review_top25)}" else ""
    Text("$label: $rankText$suffix")
}

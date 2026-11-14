package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.achievements.CoachNcbaPostseasonRollup
import dev.novux.dynasty.cbb.core.coach.achievements.CoachNcbaPostseasonRollupPolicy
import dev.novux.dynasty.cbb.core.coach.achievements.HeadCoachAchievements

@Composable
fun CoachOverviewNcbaPostseasonBlock(
    achievements: HeadCoachAchievements,
    modifier: Modifier = Modifier,
) {
    CoachOverviewNcbaPostseasonBlock(rollup = CoachNcbaPostseasonRollup.from(achievements), modifier = modifier)
}

@Composable
fun CoachOverviewNcbaPostseasonBlock(
    rollup: CoachNcbaPostseasonRollup,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = CoachNcbaPostseasonRollupPolicy.MEGA_ID
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HorizontalDivider()
        Text(
            stringResource(R.string.coach_overview_ncba_postseason_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            stringResource(
                R.string.coach_overview_ncba_tourney_roll,
                rollup.tourneyRecordLabel,
                rollup.tourneyWinPercentLabel,
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            stringResource(R.string.coach_overview_ncba_national_titles, rollup.nationalTitles),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(R.string.coach_overview_ncba_final_fours, rollup.finalFours),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(R.string.coach_overview_ncba_appearances, rollup.ncbaAppearances),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

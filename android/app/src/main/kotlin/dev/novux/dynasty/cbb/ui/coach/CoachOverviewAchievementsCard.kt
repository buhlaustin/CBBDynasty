package dev.novux.dynasty.cbb.ui.coach

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
import dev.novux.dynasty.cbb.core.coach.achievements.CoachOverviewCareerRecordPolicy
import dev.novux.dynasty.cbb.core.coach.achievements.HeadCoachAchievements

/** Head-coach career achievements ([MEGA-COACH-036], career hero [MEGA-COACH-128]). */
@Composable
fun CoachOverviewAchievementsCard(
    achievements: HeadCoachAchievements,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(R.string.coach_overview_achievements_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            CoachOverviewCareerRecordHero(
                recordLabel = achievements.recordLabel,
                winPercentLabel = achievements.winPercentLabel,
            )
            CoachOverviewNcbaPostseasonBlock(achievements = achievements)
            AchievementLine(stringResource(R.string.coach_achievement_conf_reg, achievements.confRegTitles))
            AchievementLine(stringResource(R.string.coach_achievement_conf_tourney, achievements.confTourneyTitles))
            AchievementLine(
                stringResource(
                    R.string.coach_achievement_nci,
                    achievements.nciRecordLabel,
                    achievements.nciTitles,
                ),
            )
            AchievementLine(
                stringResource(
                    R.string.coach_achievement_heritage,
                    achievements.heritageRecordLabel,
                    achievements.heritageTitles,
                ),
            )
        }
    }
}

@Composable
fun CoachOverviewCareerRecordHero(
    recordLabel: String,
    winPercentLabel: String,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = CoachOverviewCareerRecordPolicy.MEGA_ID
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            stringResource(R.string.coach_overview_career_record_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            stringResource(
                R.string.coach_overview_career_record_value,
                recordLabel,
                winPercentLabel,
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AchievementLine(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

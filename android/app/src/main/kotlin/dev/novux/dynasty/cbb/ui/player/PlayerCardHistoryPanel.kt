package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.calendar.SeasonMembershipYear
import dev.novux.dynasty.cbb.core.player.history.PlayerPriorSeasonHistoryPolicy
import dev.novux.dynasty.cbb.core.player.history.PlayerPriorSeasonSnapshot

/** Prior season rows with school and transfer chip ([MEGA-COACH-157]). */
@Composable
fun PlayerPriorSeasonList(
    seasons: List<PlayerPriorSeasonSnapshot>,
    currentSchoolDisplayName: String,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = PlayerPriorSeasonHistoryPolicy.MEGA_ID
    var expandedSeason by remember { mutableStateOf<Int?>(null) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.player_card_history_prior_seasons_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        seasons.forEach { season ->
            val expanded = expandedSeason == season.seasonIndex
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedSeason = if (expanded) null else season.seasonIndex
                    },
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        SeasonMembershipYear.label(season.seasonIndex),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    PlayerPriorSeasonSchoolRow(
                        season = season,
                        currentSchoolDisplayName = currentSchoolDisplayName,
                    )
                    Text(
                        stringResource(
                            R.string.player_card_history_season_line,
                            season.gamesPlayed,
                            season.pointsPerGame(),
                            season.assistsPerGame(),
                            season.reboundsPerGame(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (expanded) {
                        Text(
                            stringResource(
                                R.string.player_card_history_season_totals,
                                season.totals.points,
                                season.totals.assists,
                                season.totals.rebounds,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerPriorSeasonSchoolRow(
    season: PlayerPriorSeasonSnapshot,
    currentSchoolDisplayName: String,
) {
    val school = season.schoolDisplayName?.trim().orEmpty()
    if (school.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.player_card_history_school_line, school),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
        if (season.showsTransferFrom(currentSchoolDisplayName)) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.player_card_history_transfer_chip)) },
            )
        }
    }
}

package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import dev.novux.dynasty.cbb.core.player.PlayerRatings
import dev.novux.dynasty.cbb.core.player.shootingOverall

/** OVR/OFF/DEF, shooting subs, skills, and potential rows ([MEGA-COACH-066], [MEGA-COACH-142]). */
@Composable
fun PlayerRatingAttributesPanel(
    ratings: PlayerRatings,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    stringResource(R.string.player_ratings_headline_title),
                    fontWeight = FontWeight.SemiBold,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_ovr_label),
                    value = ratings.ovr,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_off_label),
                    value = ratings.off,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_def_label),
                    value = ratings.def,
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    stringResource(R.string.player_ratings_attributes_title),
                    fontWeight = FontWeight.SemiBold,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_shooting_label),
                    value = ratings.shootingOverall(),
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    PlayerRatingAttributeRow(
                        label = stringResource(R.string.player_rating_inside_sub),
                        value = ratings.shootingInside,
                    )
                    PlayerRatingAttributeRow(
                        label = stringResource(R.string.player_rating_mid_sub),
                        value = ratings.shootingMid,
                    )
                    PlayerRatingAttributeRow(
                        label = stringResource(R.string.player_rating_three_sub),
                        value = ratings.shootingThree,
                    )
                }
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_handling_label),
                    value = ratings.handling,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_passing_label),
                    value = ratings.passing,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_rebounding_label),
                    value = ratings.rebounding,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_perimeter_def_label),
                    value = ratings.perimeterDefense,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_interior_def_label),
                    value = ratings.interiorDefense,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_steal_label),
                    value = ratings.stealing,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_block_label),
                    value = ratings.blocking,
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    stringResource(R.string.player_ratings_potential_title),
                    fontWeight = FontWeight.SemiBold,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_potential_label),
                    value = ratings.potentialOverall,
                )
                PlayerRatingAttributeRow(
                    label = stringResource(R.string.player_rating_pro_potential_label),
                    value = ratings.proPotential,
                )
            }
        }
    }
}

@Composable
private fun PlayerRatingAttributeRow(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

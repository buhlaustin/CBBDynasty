package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.ratings.PlayerRatingsTabPolicy
import dev.novux.dynasty.cbb.core.player.scout.ScoutDepth

@Suppress("unused")
private val playerRatingsTabQaId: String = PlayerRatingsTabPolicy.QA_ID

/** Ratings tab body: headline, full attributes, loyalty, scout. */
@Composable
fun PlayerCardRatingsTabContent(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.player_card_ratings_tab),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        PlayerRatingAttributesPanel(ratings = player.ratings)
        PlayerLoyaltyRatingsRow(player)
        Text(
            stringResource(R.string.player_card_scout_ovr, player.scoutReport(ScoutDepth.Full).overall),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.nil.PlayerNilSeasonDealEngine
import dev.novux.dynasty.cbb.core.player.nil.PlayerNilValuationPolicy

/** Annual program NIL valuation on player Overview ([MEGA-COACH-064], deal `↑` chip [MEGA-COACH-148]). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerNilValuationLine(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    @Suppress("unused")
    val qaId = PlayerNilValuationPolicy.QA_ID
    val showRaised = PlayerNilSeasonDealEngine.showRaisedChip(player.identity)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.player_card_nil_line, player.identity.nilUsdPerYear),
            style = if (compact) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            fontWeight = if (player.identity.nilUsdPerYear > 0) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary,
        )
        if (showRaised) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.player_nil_deal_raised_chip)) },
            )
        }
    }
}

package dev.novux.dynasty.cbb.ui.player

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.draft.PlayerDraftProjectionEngine
import dev.novux.dynasty.cbb.core.player.draft.PlayerDraftProjectionPolicy
import dev.novux.dynasty.cbb.core.player.draft.PlayerDraftRankLinePolicy
import androidx.compose.foundation.layout.Column

@Suppress("unused")
private val draftProjectionPoolAnchor: String = PlayerDraftProjectionPolicy.NATIONAL_POOL

/** Draft rank line `Draft: #N` when Top 60 + eligible ([MEGA-COACH-163]). */
@Composable
fun PlayerDraftProjectionLine(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    nationalTop60RankByPlayerId: Map<String, Int> = emptyMap(),
) {
    @Suppress("unused")
    val qaId = PlayerDraftRankLinePolicy.MEGA_ID
    val projection = PlayerDraftProjectionEngine.resolve(
        player,
        nationalTop60RankByPlayerId = nationalTop60RankByPlayerId,
    )
    if (!projection.isAvailable) {
        Text(
            text = stringResource(R.string.player_card_draft_na),
            style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.player_card_draft_line, projection.rank!!),
            style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val tier = projection.tierLabel
        if (!compact && !tier.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.player_card_draft_tier_sublabel, tier),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

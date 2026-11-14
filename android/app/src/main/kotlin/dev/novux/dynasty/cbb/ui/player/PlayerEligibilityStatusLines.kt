package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.card.PlayerEligibilityCardPolicy

@Composable
fun PlayerEligibilityStatusLines(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = PlayerEligibilityCardPolicy.MEGA_ID
    val kinds = PlayerEligibilityCardPolicy.overviewLineKinds(player)
    if (kinds.isEmpty()) {
        Text(
            text = stringResource(
                R.string.player_card_eligibility_clock,
                player.eligibility.yearsUsed,
                player.eligibility.yearsRemaining,
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.fillMaxWidth(),
        )
        return
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                R.string.player_card_eligibility_clock,
                player.eligibility.yearsUsed,
                player.eligibility.yearsRemaining,
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        kinds.forEach { kind ->
            val res = when (kind) {
                PlayerEligibilityCardPolicy.LineKind.MedicalRedshirt ->
                    R.string.player_card_eligibility_medical_rs
                PlayerEligibilityCardPolicy.LineKind.DevelopmentYear ->
                    R.string.player_card_eligibility_development_year
                PlayerEligibilityCardPolicy.LineKind.FiveYearPath ->
                    R.string.player_card_eligibility_five_year_path
            }
            Text(
                text = stringResource(res),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
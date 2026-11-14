package dev.novux.dynasty.cbb.ui.coach

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.coach.card.CoachCardIdentityStripPolicy

/** Text-only school + role (+ record) — no coach portrait per product lock ([MEGA-COACH-030]). */
@Composable
fun CoachCardIdentityStrip(
    schoolDisplayName: String,
    roleLabel: String,
    seasonRecordLabel: String,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = CoachCardIdentityStripPolicy.MEGA_ID
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = schoolDisplayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = roleLabel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.coach_card_record, seasonRecordLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

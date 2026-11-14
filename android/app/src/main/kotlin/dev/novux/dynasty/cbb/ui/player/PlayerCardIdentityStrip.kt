package dev.novux.dynasty.cbb.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.player.DynastyPlayer
import dev.novux.dynasty.cbb.core.player.card.PlayerCardIdentityStripPolicy
import dev.novux.dynasty.cbb.ui.portrait.PlayerPortraitSlot

@Composable
fun PlayerCardIdentityStrip(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
    showPortrait: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPortrait) {
            PlayerPortraitSlot(
                displayName = player.identity.displayName,
                avatarPresetId = player.appearance.avatarPresetId,
                playerId = player.playerId,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PlayerCardIdentityStripChips(player = player)
            Text(
                player.archetype.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerCardIdentityStripChips(
    player: DynastyPlayer,
    modifier: Modifier = Modifier,
) {
    @Suppress("unused")
    val qaId = PlayerCardIdentityStripPolicy.MEGA_ID
    val injuryStrip = player.injury.injuryStripOrNull()
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AssistChip(
            onClick = {},
            label = { Text(player.position.label) },
        )
        AssistChip(
            onClick = {},
            label = { Text(player.classYear.label) },
        )
        if (injuryStrip != null) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.injury_roster_out_chip)) },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = MaterialTheme.colorScheme.error,
                ),
            )
        }
    }
}

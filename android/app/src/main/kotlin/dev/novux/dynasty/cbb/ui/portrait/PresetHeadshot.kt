package dev.novux.dynasty.cbb.ui.portrait

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared pack-safe headshot entry point for player and coach cards
 * ([planning/player-cards.md], [planning/coach-card.md]).
 */
@Composable
fun PresetHeadshot(
    avatarPresetId: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    contentDescription: String? = null,
) {
    PresetPortrait(
        presetId = avatarPresetId,
        modifier = modifier,
        size = size,
        contentDescription = contentDescription,
    )
}

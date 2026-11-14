package dev.novux.dynasty.cbb.ui.portrait

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.core.player.appearance.PlayerAppearanceResolver

@Composable
fun CoachPortraitSlot(
    displayName: String,
    avatarPresetId: String,
    modifier: Modifier = Modifier,
) {
    if (avatarPresetId.isNotBlank()) {
        PresetHeadshot(
            avatarPresetId = avatarPresetId,
            modifier = modifier,
            contentDescription = displayName,
        )
    } else {
        InitialsPortraitFallback(displayName = displayName, modifier = modifier)
    }
}

@Composable
fun PlayerPortraitSlot(
    displayName: String,
    modifier: Modifier = Modifier,
    avatarPresetId: String? = null,
    playerId: String? = null,
) {
    val presetId = remember(avatarPresetId, playerId) {
        avatarPresetId?.takeIf { it.isNotBlank() }
            ?: playerId?.let(PlayerAppearanceResolver::presetIdForPlayer)
    }
    if (presetId != null) {
        PresetHeadshot(
            avatarPresetId = presetId,
            modifier = modifier,
            contentDescription = displayName,
        )
        return
    }
    InitialsPortraitFallback(displayName = displayName, modifier = modifier)
}

@Composable
fun InitialsPortraitFallback(displayName: String, modifier: Modifier = Modifier) {
    val initials = remember(displayName) {
        displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
    }
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

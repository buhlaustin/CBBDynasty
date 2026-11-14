package dev.novux.dynasty.cbb.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.GameConstants
import dev.novux.dynasty.cbb.core.save.SaveSlotSummary

/** Pre-dynasty home — [planning/game-start.md#home-screen]. */
@Composable
fun HomeMenuScreen(
    continueSlot: SaveSlotSummary?,
    continueLoading: Boolean = false,
    onContinue: () -> Unit,
    onNew: () -> Unit,
    onLoad: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = GameConstants.GAME_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        if (continueSlot != null && !continueSlot.isEmpty) {
            Text(
                text = stringResource(
                    R.string.home_continue_hint,
                    continueSlot.schoolDisplayName.orEmpty(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onContinue,
                enabled = !continueLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (continueLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.menu_continue))
                }
            }
        }

        Button(
            onClick = onNew,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.menu_new))
        }

        OutlinedButton(
            onClick = onLoad,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.menu_load))
        }

        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.menu_settings))
        }

        OutlinedButton(
            onClick = onCredits,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.menu_credits))
        }
    }
}

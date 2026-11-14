package dev.novux.dynasty.cbb.ui.newgame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.SchemePresetIdentityCopy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchemesStepContent(
    offensive: OffensiveSchemePreset,
    defensive: DefensiveSchemePreset,
    onOffensiveChange: (OffensiveSchemePreset) -> Unit,
    onDefensiveChange: (DefensiveSchemePreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.new_schemes_offense),
            style = MaterialTheme.typography.titleSmall,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OffensiveSchemePreset.entries.forEach { preset ->
                FilterChip(
                    selected = preset == offensive,
                    onClick = { onOffensiveChange(preset) },
                    label = { Text(preset.label) },
                )
            }
        }
        Text(
            text = SchemePresetIdentityCopy.offensive(offensive),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.new_schemes_defense),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DefensiveSchemePreset.entries.forEach { preset ->
                FilterChip(
                    selected = preset == defensive,
                    onClick = { onDefensiveChange(preset) },
                    label = { Text(preset.label) },
                )
            }
        }
        Text(
            text = SchemePresetIdentityCopy.defensive(defensive),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

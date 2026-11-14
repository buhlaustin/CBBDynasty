package dev.novux.dynasty.cbb.ui.strategy

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.strategy.DefensiveSchemePreset
import dev.novux.dynasty.cbb.core.strategy.OffensiveSchemePreset
import dev.novux.dynasty.cbb.ui.newgame.SchemesStepContent

@Composable
fun StrategyGameSchemePanel(
    offensiveScheme: OffensiveSchemePreset,
    defensiveScheme: DefensiveSchemePreset,
    coachDefaultOffensive: OffensiveSchemePreset,
    coachDefaultDefensive: DefensiveSchemePreset,
    onOffensiveChange: (OffensiveSchemePreset) -> Unit,
    onDefensiveChange: (DefensiveSchemePreset) -> Unit,
    onSaveGamePrep: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.strategy_game_schemes_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.strategy_game_schemes_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(
                R.string.strategy_coach_default_schemes,
                coachDefaultOffensive.label,
                coachDefaultDefensive.label,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SchemesStepContent(
            offensive = offensiveScheme,
            defensive = defensiveScheme,
            onOffensiveChange = onOffensiveChange,
            onDefensiveChange = onDefensiveChange,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = onSaveGamePrep, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.strategy_save_game_prep))
        }
    }
}

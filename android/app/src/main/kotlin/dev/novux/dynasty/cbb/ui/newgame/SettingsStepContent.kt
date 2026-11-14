package dev.novux.dynasty.cbb.ui.newgame

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.save.NewGameDynastySettings
import dev.novux.dynasty.cbb.core.save.NewGameWizardSeasonPolicy

@Composable
fun SettingsStepContent(
    settings: NewGameDynastySettings,
    activePackLabel: String,
    onChange: (NewGameDynastySettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.new_wizard_active_pack, activePackLabel),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = stringResource(R.string.new_wizard_pack_selection_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(
            text = stringResource(
                R.string.new_settings_starting_season,
                NewGameWizardSeasonPolicy.startingMembershipYearLabel(),
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        DynastyToggleRow(
            title = stringResource(R.string.new_settings_auto_upgrade),
            summary = stringResource(R.string.new_settings_auto_upgrade_summary),
            checked = settings.autoUpgrade,
            onCheckedChange = { onChange(settings.copy(autoUpgrade = it)) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        DynastyToggleRow(
            title = stringResource(R.string.new_settings_unlimited_age),
            summary = stringResource(R.string.new_settings_unlimited_age_summary),
            checked = settings.unlimitedCoachAge,
            onCheckedChange = { onChange(settings.copy(unlimitedCoachAge = it)) },
        )
        Text(
            text = stringResource(R.string.new_settings_import_stub),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}

@Composable
private fun DynastyToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

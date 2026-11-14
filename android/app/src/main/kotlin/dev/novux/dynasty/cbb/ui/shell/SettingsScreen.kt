package dev.novux.dynasty.cbb.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCategory
import dev.novux.dynasty.cbb.data.settings.AppSettingsRepository
import kotlinx.coroutines.launch

/** Home app settings — [planning/game-start.md#home-settings--credits]. */
@Composable
fun SettingsScreen(
    settingsRepository: AppSettingsRepository,
    onBack: () -> Unit,
    onOpenPackImport: () -> Unit = {},
) {
    val settings by settingsRepository.settingsFlow.collectAsState(
        initial = dev.novux.dynasty.cbb.core.settings.AppSettings(),
    )
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.menu_settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.settings_section_startup),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        SettingToggleRow(
            label = stringResource(R.string.settings_skip_studio_intro),
            description = stringResource(R.string.settings_skip_studio_intro_summary),
            checked = settings.skipStudioIntro,
            onCheckedChange = { enabled ->
                scope.launch { settingsRepository.setSkipStudioIntro(enabled) }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_section_notifications),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        SettingToggleRow(
            label = stringResource(R.string.settings_dashboard_notification_feed),
            description = stringResource(R.string.settings_dashboard_notification_feed_summary),
            checked = settings.showDashboardNotificationFeed,
            onCheckedChange = { enabled ->
                scope.launch { settingsRepository.setShowDashboardNotificationFeed(enabled) }
            },
        )
        Text(
            text = stringResource(R.string.settings_notification_mute_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        NotificationFeedCategoryMuteRow(
            label = stringResource(R.string.settings_mute_feed_recruiting),
            description = stringResource(R.string.settings_mute_feed_recruiting_summary),
            muted = NotificationFeedCategory.Recruiting in settings.mutedNotificationFeedCategories,
            onMutedChange = { muted ->
                scope.launch {
                    settingsRepository.setNotificationCategoryMuted(NotificationFeedCategory.Recruiting, muted)
                }
            },
        )
        NotificationFeedCategoryMuteRow(
            label = stringResource(R.string.settings_mute_feed_roster_health),
            description = stringResource(R.string.settings_mute_feed_roster_health_summary),
            muted = NotificationFeedCategory.RosterHealth in settings.mutedNotificationFeedCategories,
            onMutedChange = { muted ->
                scope.launch {
                    settingsRepository.setNotificationCategoryMuted(NotificationFeedCategory.RosterHealth, muted)
                }
            },
        )
        NotificationFeedCategoryMuteRow(
            label = stringResource(R.string.settings_mute_feed_coach_career),
            description = stringResource(R.string.settings_mute_feed_coach_career_summary),
            muted = NotificationFeedCategory.CoachCareer in settings.mutedNotificationFeedCategories,
            onMutedChange = { muted ->
                scope.launch {
                    settingsRepository.setNotificationCategoryMuted(NotificationFeedCategory.CoachCareer, muted)
                }
            },
        )
        NotificationFeedCategoryMuteRow(
            label = stringResource(R.string.settings_mute_feed_game_week),
            description = stringResource(R.string.settings_mute_feed_game_week_summary),
            muted = NotificationFeedCategory.GameWeek in settings.mutedNotificationFeedCategories,
            onMutedChange = { muted ->
                scope.launch {
                    settingsRepository.setNotificationCategoryMuted(NotificationFeedCategory.GameWeek, muted)
                }
            },
        )
        NotificationFeedCategoryMuteRow(
            label = stringResource(R.string.settings_mute_feed_league),
            description = stringResource(R.string.settings_mute_feed_league_summary),
            muted = NotificationFeedCategory.LeagueUpdates in settings.mutedNotificationFeedCategories,
            onMutedChange = { muted ->
                scope.launch {
                    settingsRepository.setNotificationCategoryMuted(NotificationFeedCategory.LeagueUpdates, muted)
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_section_automation_defaults),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        SettingToggleRow(
            label = stringResource(R.string.settings_default_recruiting_auto),
            description = stringResource(R.string.settings_default_recruiting_auto_summary),
            checked = settings.defaultRecruitingAutoMaintain,
            onCheckedChange = { enabled ->
                scope.launch { settingsRepository.setDefaultRecruitingAutoMaintain(enabled) }
            },
        )
        SettingToggleRow(
            label = stringResource(R.string.settings_default_coach_auto_spend),
            description = stringResource(R.string.settings_default_coach_auto_spend_summary),
            checked = settings.defaultCoachAutoSpend,
            onCheckedChange = { enabled ->
                scope.launch { settingsRepository.setDefaultCoachAutoSpend(enabled) }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_section_legal),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.settings_offline_play_summary),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_section_identity_pack),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.settings_identity_pack_summary),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Button(
            onClick = onOpenPackImport,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_open_pack_import))
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_section_audio),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.settings_audio_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        SettingToggleRow(
            label = stringResource(R.string.settings_sfx_enabled),
            checked = settings.sfxEnabled,
            onCheckedChange = { enabled ->
                scope.launch { settingsRepository.setSfxEnabled(enabled) }
            },
        )
        SettingToggleRow(
            label = stringResource(R.string.settings_music_enabled),
            checked = settings.musicEnabled,
            onCheckedChange = { enabled ->
                scope.launch { settingsRepository.setMusicEnabled(enabled) }
            },
        )
        Text(
            text = stringResource(R.string.settings_master_volume),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Slider(
            value = settings.masterVolumePercent.toFloat(),
            onValueChange = { value ->
                scope.launch {
                    settingsRepository.setMasterVolumePercent(value.toInt())
                }
            },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                R.string.settings_master_volume_value,
                settings.masterVolumePercent,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_back))
        }
    }
}

@Composable
private fun NotificationFeedCategoryMuteRow(
    label: String,
    description: String,
    muted: Boolean,
    onMutedChange: (Boolean) -> Unit,
) {
    SettingToggleRow(
        label = label,
        description = description,
        checked = muted,
        onCheckedChange = onMutedChange,
    )
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

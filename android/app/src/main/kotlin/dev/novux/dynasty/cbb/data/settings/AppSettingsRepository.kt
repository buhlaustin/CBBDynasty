package dev.novux.dynasty.cbb.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCategory
import dev.novux.dynasty.cbb.core.settings.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
)

class AppSettingsRepository(
    private val context: Context,
) {
    val settingsFlow: Flow<AppSettings> =
        context.appSettingsDataStore.data.map { prefs -> prefs.toAppSettings() }

    suspend fun currentSettings(): AppSettings = settingsFlow.first()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.appSettingsDataStore.edit { prefs ->
            val current = prefs.toAppSettings()
            val updated = transform(current)
            prefs[Keys.SKIP_STUDIO_INTRO] = updated.skipStudioIntro
            prefs[Keys.SFX_ENABLED] = updated.sfxEnabled
            prefs[Keys.MUSIC_ENABLED] = updated.musicEnabled
            prefs[Keys.MASTER_VOLUME_PERCENT] = updated.masterVolumePercent
            prefs[Keys.SHOW_DASHBOARD_NOTIFICATION_FEED] = updated.showDashboardNotificationFeed
            prefs[Keys.DEFAULT_RECRUITING_AUTO_MAINTAIN] = updated.defaultRecruitingAutoMaintain
            prefs[Keys.DEFAULT_COACH_AUTO_SPEND] = updated.defaultCoachAutoSpend
            prefs[Keys.MUTED_NOTIFICATION_FEED_CATEGORIES] =
                updated.mutedNotificationFeedCategories.joinToString(",") { it.name }
        }
    }

    suspend fun setNotificationCategoryMuted(category: NotificationFeedCategory, muted: Boolean) {
        update { current ->
            val next = current.mutedNotificationFeedCategories.toMutableSet()
            if (muted) next.add(category) else next.remove(category)
            current.copy(mutedNotificationFeedCategories = next)
        }
    }

    suspend fun setDefaultRecruitingAutoMaintain(enabled: Boolean) {
        update { it.copy(defaultRecruitingAutoMaintain = enabled) }
    }

    suspend fun setDefaultCoachAutoSpend(enabled: Boolean) {
        update { it.copy(defaultCoachAutoSpend = enabled) }
    }

    suspend fun setShowDashboardNotificationFeed(enabled: Boolean) {
        update { it.copy(showDashboardNotificationFeed = enabled) }
    }

    suspend fun setSkipStudioIntro(enabled: Boolean) {
        update { it.copy(skipStudioIntro = enabled) }
    }

    suspend fun setSfxEnabled(enabled: Boolean) {
        update { it.copy(sfxEnabled = enabled) }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        update { it.copy(musicEnabled = enabled) }
    }

    suspend fun setMasterVolumePercent(percent: Int) {
        update { it.copy(masterVolumePercent = percent.coerceIn(0, 100)) }
    }

    private fun Preferences.toAppSettings(): AppSettings =
        AppSettings(
            skipStudioIntro = this[Keys.SKIP_STUDIO_INTRO] ?: false,
            sfxEnabled = this[Keys.SFX_ENABLED] ?: true,
            musicEnabled = this[Keys.MUSIC_ENABLED] ?: true,
            masterVolumePercent = this[Keys.MASTER_VOLUME_PERCENT]
                ?: AppSettings.DEFAULT_MASTER_VOLUME,
            showDashboardNotificationFeed = this[Keys.SHOW_DASHBOARD_NOTIFICATION_FEED] ?: true,
            defaultRecruitingAutoMaintain = this[Keys.DEFAULT_RECRUITING_AUTO_MAINTAIN] ?: true,
            defaultCoachAutoSpend = this[Keys.DEFAULT_COACH_AUTO_SPEND] ?: false,
            mutedNotificationFeedCategories = parseMutedCategories(
                this[Keys.MUTED_NOTIFICATION_FEED_CATEGORIES],
            ),
        )

    private fun parseMutedCategories(raw: String?): Set<NotificationFeedCategory> {
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split(',')
            .mapNotNull { token ->
                runCatching { NotificationFeedCategory.valueOf(token.trim()) }.getOrNull()
            }
            .toSet()
    }

    private object Keys {
        val SKIP_STUDIO_INTRO = booleanPreferencesKey("skip_studio_intro")
        val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val MASTER_VOLUME_PERCENT = intPreferencesKey("master_volume_percent")
        val SHOW_DASHBOARD_NOTIFICATION_FEED = booleanPreferencesKey("show_dashboard_notification_feed")
        val DEFAULT_RECRUITING_AUTO_MAINTAIN = booleanPreferencesKey("default_recruiting_auto_maintain")
        val DEFAULT_COACH_AUTO_SPEND = booleanPreferencesKey("default_coach_auto_spend")
        val MUTED_NOTIFICATION_FEED_CATEGORIES = stringPreferencesKey("muted_notification_feed_categories")
    }
}

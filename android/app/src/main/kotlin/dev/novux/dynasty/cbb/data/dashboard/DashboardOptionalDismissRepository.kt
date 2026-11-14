package dev.novux.dynasty.cbb.data.dashboard

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dashboardOptionalDismissStore by preferencesDataStore(
    name = "dashboard_optional_dismiss",
)

/** Persists scoped optional-task dismiss keys across process death ([MEGA-FRAME-101]). */
class DashboardOptionalDismissRepository(private val context: Context) {
    suspend fun load(dynastyId: String, seasonIndex: Int): Set<String> {
        val raw = context.dashboardOptionalDismissStore.data.first()[prefsKey(dynastyId, seasonIndex)]
            ?: return emptySet()
        return raw.split(DELIMITER).filter { it.isNotBlank() }.toSet()
    }

    suspend fun recordDismiss(dynastyId: String, seasonIndex: Int, dismissKey: String): Set<String> {
        val next = load(dynastyId, seasonIndex) + dismissKey
        persist(dynastyId, seasonIndex, next)
        return next
    }

    private suspend fun persist(dynastyId: String, seasonIndex: Int, keys: Set<String>) {
        context.dashboardOptionalDismissStore.edit { prefs ->
            prefs[prefsKey(dynastyId, seasonIndex)] = keys.joinToString(DELIMITER)
        }
    }

    private fun prefsKey(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("offseason_optional_dismiss_${dynastyId}_$seasonIndex")

    companion object {
        private const val DELIMITER = "\u001f"
    }
}
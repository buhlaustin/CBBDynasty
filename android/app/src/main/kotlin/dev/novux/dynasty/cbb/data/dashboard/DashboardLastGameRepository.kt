package dev.novux.dynasty.cbb.data.dashboard

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.dashboard.DashboardLastGameSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.lastGameStore: DataStore<Preferences> by preferencesDataStore(name = "dashboard_last_game")

class DashboardLastGameRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(dynastyId: String, seasonIndex: Int): DashboardLastGameSnapshot? {
        val raw = context.lastGameStore.data.first()[key(dynastyId, seasonIndex)] ?: return null
        return runCatching { json.decodeFromString<DashboardLastGameSnapshot>(raw) }.getOrNull()
    }

    suspend fun save(dynastyId: String, seasonIndex: Int, snapshot: DashboardLastGameSnapshot) {
        context.lastGameStore.edit { prefs ->
            prefs[key(dynastyId, seasonIndex)] = json.encodeToString(snapshot)
        }
    }

    private fun key(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("last_${dynastyId}_$seasonIndex")
}

package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.recruitingWatchlistStore by preferencesDataStore(name = "recruiting_watchlist")

class RecruitingWatchlistRepository(private val context: Context) {
    suspend fun load(dynastyId: String, cycleSeasonIndex: Int): Set<String> {
        val raw = context.recruitingWatchlistStore.data.first()[key(dynastyId, cycleSeasonIndex)] ?: return emptySet()
        return raw.split(";").filter { it.isNotBlank() }.toSet()
    }

    suspend fun add(dynastyId: String, cycleSeasonIndex: Int, prospectId: String): Set<String> {
        val next = load(dynastyId, cycleSeasonIndex) + prospectId
        persist(dynastyId, cycleSeasonIndex, next)
        return next
    }

    suspend fun remove(dynastyId: String, cycleSeasonIndex: Int, prospectId: String): Set<String> {
        val next = load(dynastyId, cycleSeasonIndex) - prospectId
        persist(dynastyId, cycleSeasonIndex, next)
        return next
    }

    private suspend fun persist(dynastyId: String, cycleSeasonIndex: Int, ids: Set<String>) {
        context.recruitingWatchlistStore.edit { prefs ->
            prefs[key(dynastyId, cycleSeasonIndex)] = ids.joinToString(";")
        }
    }

    private fun key(dynastyId: String, cycleSeasonIndex: Int) =
        stringPreferencesKey("watchlist_${dynastyId}_$cycleSeasonIndex")
}

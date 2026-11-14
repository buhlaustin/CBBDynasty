package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.recruitingWatchlistTop60Store by preferencesDataStore(name = "recruiting_watchlist_top60")

/**
 * Last observed national Top 60 ranks for watchlist targets — drives movement nudges (MEGA-RECR-147).
 */
class RecruitingWatchlistTop60RankRepository(private val context: Context) {
    suspend fun load(dynastyId: String, cycleSeasonIndex: Int): Map<String, Int> {
        val raw = context.recruitingWatchlistTop60Store.data.first()[key(dynastyId, cycleSeasonIndex)]
            ?: return emptyMap()
        return raw.split(";").mapNotNull { part ->
            val pieces = part.split(":")
            if (pieces.size != 2) return@mapNotNull null
            val rank = pieces[1].toIntOrNull() ?: return@mapNotNull null
            pieces[0] to rank
        }.toMap()
    }

    suspend fun save(dynastyId: String, cycleSeasonIndex: Int, rankByProspect: Map<String, Int>) {
        val encoded = rankByProspect.entries.joinToString(";") { "${it.key}:${it.value}" }
        context.recruitingWatchlistTop60Store.edit { prefs ->
            prefs[key(dynastyId, cycleSeasonIndex)] = encoded
        }
    }

    private fun key(dynastyId: String, cycleSeasonIndex: Int) =
        stringPreferencesKey("watchlist_top60_${dynastyId}_$cycleSeasonIndex")
}

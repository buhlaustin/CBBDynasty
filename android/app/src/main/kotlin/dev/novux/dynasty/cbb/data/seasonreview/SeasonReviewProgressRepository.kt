package dev.novux.dynasty.cbb.data.seasonreview

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewProgress
import kotlinx.coroutines.flow.first

private val Context.seasonReviewProgressStore: DataStore<Preferences> by preferencesDataStore(
    name = "season_review_progress",
)

class SeasonReviewProgressRepository(private val context: Context) {
    suspend fun load(dynastyId: String, seasonIndex: Int): SeasonReviewProgress {
        val prefs = context.seasonReviewProgressStore.data.first()
        val acknowledged = prefs[ackKey(dynastyId, seasonIndex)] ?: false
        return SeasonReviewProgress(acknowledged = acknowledged)
    }

    suspend fun acknowledge(dynastyId: String, seasonIndex: Int): SeasonReviewProgress {
        context.seasonReviewProgressStore.edit { prefs ->
            prefs[ackKey(dynastyId, seasonIndex)] = true
        }
        return SeasonReviewProgress(acknowledged = true)
    }

    private fun ackKey(dynastyId: String, seasonIndex: Int) =
        booleanPreferencesKey("ack_${dynastyId}_$seasonIndex")
}

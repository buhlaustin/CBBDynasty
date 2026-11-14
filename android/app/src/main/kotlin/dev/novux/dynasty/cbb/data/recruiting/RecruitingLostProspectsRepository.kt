package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.recruitingLostStore by preferencesDataStore(name = "recruiting_lost")

class RecruitingLostProspectsRepository(private val context: Context) {
    suspend fun load(dynastyId: String, cycleSeasonIndex: Int): Set<String> {
        val raw = context.recruitingLostStore.data.first()[key(dynastyId, cycleSeasonIndex)] ?: return emptySet()
        return raw.split(";").filter { it.isNotBlank() }.toSet()
    }

    suspend fun record(dynastyId: String, cycleSeasonIndex: Int, prospectId: String): Set<String> {
        val next = load(dynastyId, cycleSeasonIndex) + prospectId
        context.recruitingLostStore.edit { prefs ->
            prefs[key(dynastyId, cycleSeasonIndex)] = next.joinToString(";")
        }
        return next
    }

    private fun key(dynastyId: String, cycleSeasonIndex: Int) =
        stringPreferencesKey("lost_${dynastyId}_$cycleSeasonIndex")
}

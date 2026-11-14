package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.recruiting.RecruitingProspectPointSpendLedger
import kotlinx.coroutines.flow.first

private val Context.recruitingInterestSpendStore: DataStore<Preferences> by preferencesDataStore(
    name = "recruiting_interest_spend",
)

class RecruitingInterestSpendRepository(private val context: Context) {
    suspend fun loadSpendDeltaByProspect(dynastyId: String, cycleSeasonIndex: Int): Map<String, Int> {
        val raw = context.recruitingInterestSpendStore.data.first()[interestKey(dynastyId, cycleSeasonIndex)]
            ?: return emptyMap()
        return decode(raw)
    }

    suspend fun spendDeltaFor(dynastyId: String, cycleSeasonIndex: Int, prospectId: String): Int =
        loadSpendDeltaByProspect(dynastyId, cycleSeasonIndex)[prospectId] ?: 0

    suspend fun addSpendDelta(
        dynastyId: String,
        cycleSeasonIndex: Int,
        prospectId: String,
        delta: Int,
    ) {
        if (delta <= 0) return
        val current = loadSpendDeltaByProspect(dynastyId, cycleSeasonIndex).toMutableMap()
        current[prospectId] = (current[prospectId] ?: 0) + delta
        context.recruitingInterestSpendStore.edit { prefs ->
            prefs[interestKey(dynastyId, cycleSeasonIndex)] = encode(current)
        }
    }

    suspend fun loadPointSpendByProspect(dynastyId: String, cycleSeasonIndex: Int): Map<String, Int> {
        val raw = context.recruitingInterestSpendStore.data.first()[pointKey(dynastyId, cycleSeasonIndex)]
            ?: return emptyMap()
        return RecruitingProspectPointSpendLedger.decode(raw)
    }

    suspend fun pointSpendFor(dynastyId: String, cycleSeasonIndex: Int, prospectId: String): Int =
        loadPointSpendByProspect(dynastyId, cycleSeasonIndex)[prospectId] ?: 0

    suspend fun addPointSpend(
        dynastyId: String,
        cycleSeasonIndex: Int,
        prospectId: String,
        pointsCharged: Int,
    ) {
        if (pointsCharged <= 0) return
        val current = loadPointSpendByProspect(dynastyId, cycleSeasonIndex).toMutableMap()
        val next = RecruitingProspectPointSpendLedger.record(current, prospectId, pointsCharged)
        context.recruitingInterestSpendStore.edit { prefs ->
            prefs[pointKey(dynastyId, cycleSeasonIndex)] = RecruitingProspectPointSpendLedger.encode(next)
        }
    }

    private fun interestKey(dynastyId: String, cycleSeasonIndex: Int) =
        stringPreferencesKey("interest_spend_${dynastyId}_$cycleSeasonIndex")

    private fun pointKey(dynastyId: String, cycleSeasonIndex: Int) =
        stringPreferencesKey("point_spend_${dynastyId}_$cycleSeasonIndex")

    private fun encode(map: Map<String, Int>): String =
        map.entries.joinToString(";") { "${it.key}:${it.value}" }

    private fun decode(raw: String): Map<String, Int> =
        raw.split(";").mapNotNull { token ->
            val idx = token.indexOf(':')
            if (idx <= 0) return@mapNotNull null
            val delta = token.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
            token.substring(0, idx) to delta
        }.toMap()
}

package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.recruiting.RecruitOfferPromise
import kotlinx.coroutines.flow.first

private val Context.recruitOfferPromiseStore: DataStore<Preferences> by preferencesDataStore(
    name = "recruit_offer_promises",
)

class RecruitOfferPromiseRepository(
    private val context: Context,
) {
    suspend fun loadAll(dynastyId: String, cycleSeasonIndex: Int): Map<String, RecruitOfferPromise> {
        val raw = context.recruitOfferPromiseStore.data.first()[key(dynastyId, cycleSeasonIndex)] ?: return emptyMap()
        return decode(raw)
    }

    suspend fun save(dynastyId: String, cycleSeasonIndex: Int, promise: RecruitOfferPromise) {
        val next = loadAll(dynastyId, cycleSeasonIndex).toMutableMap()
        next[promise.prospectId] = promise
        persist(dynastyId, cycleSeasonIndex, next)
    }

    suspend fun remove(dynastyId: String, cycleSeasonIndex: Int, prospectId: String) {
        val next = loadAll(dynastyId, cycleSeasonIndex).toMutableMap()
        next.remove(prospectId)
        persist(dynastyId, cycleSeasonIndex, next)
    }

    private suspend fun persist(
        dynastyId: String,
        cycleSeasonIndex: Int,
        map: Map<String, RecruitOfferPromise>,
    ) {
        context.recruitOfferPromiseStore.edit { prefs ->
            prefs[key(dynastyId, cycleSeasonIndex)] = encode(map.values.toList())
        }
    }

    private fun key(dynastyId: String, cycleSeasonIndex: Int) =
        stringPreferencesKey("offer_promises_${dynastyId}_$cycleSeasonIndex")

    private fun encode(promises: List<RecruitOfferPromise>): String =
        promises.joinToString(";") { p ->
            val stay = p.stayTopN?.toString().orEmpty()
            "${p.prospectId}|${p.promiseType.wire}|$stay"
        }

    private fun decode(raw: String): Map<String, RecruitOfferPromise> =
        raw.split(";").filter { it.isNotBlank() }.mapNotNull { token ->
            val parts = token.split("|")
            if (parts.size != 3) return@mapNotNull null
            val type = CoachPromiseType.fromWire(parts[1]) ?: return@mapNotNull null
            val stay = parts[2].takeIf { it.isNotBlank() }?.toIntOrNull()
            RecruitOfferPromise(
                prospectId = parts[0],
                promiseType = type,
                stayTopN = stay,
            )
        }.associateBy { it.prospectId }
}

package dev.novux.dynasty.cbb.data.coach

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachExtensionOffer
import dev.novux.dynasty.cbb.core.coach.contract.HeadCoachUserContractState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.headCoachUserContractStore: DataStore<Preferences> by preferencesDataStore(
    name = "head_coach_user_contract",
)

class HeadCoachUserContractRepository(
    private val context: Context,
) {
    suspend fun ensureDefaults(dynastyId: String) = withContext(Dispatchers.IO) {
        val key = stateKey(dynastyId)
        if (context.headCoachUserContractStore.data.first().contains(key)) return@withContext
        persist(HeadCoachUserContractState.defaults(dynastyId))
    }

    suspend fun load(dynastyId: String): HeadCoachUserContractState = withContext(Dispatchers.IO) {
        val raw = context.headCoachUserContractStore.data.first()[stateKey(dynastyId)]
            ?: return@withContext HeadCoachUserContractState.defaults(dynastyId)
        Json.decodeFromString(raw)
    }

    suspend fun acceptExtension(dynastyId: String, offer: HeadCoachExtensionOffer) = withContext(Dispatchers.IO) {
        val current = load(dynastyId)
        persist(
            current.copy(
                contractLengthYears = offer.proposedContractLengthYears,
                contractYearsLeft = offer.proposedYearsLeft,
                annualSalaryUsd = offer.proposedAnnualSalaryUsd,
                declinedExtensionSeasonIndex = null,
            ),
        )
    }

    suspend fun declineExtension(dynastyId: String, seasonIndex: Int) = withContext(Dispatchers.IO) {
        val current = load(dynastyId)
        persist(current.copy(declinedExtensionSeasonIndex = seasonIndex))
    }

    private suspend fun persist(state: HeadCoachUserContractState) {
        context.headCoachUserContractStore.edit { prefs ->
            prefs[stateKey(state.dynastyId)] = Json.encodeToString(state)
        }
    }

    private fun stateKey(dynastyId: String) = stringPreferencesKey("hc_contract_$dynastyId")
}

package dev.novux.dynasty.cbb.data.pack

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.packDynastyIdentityStore: DataStore<Preferences> by preferencesDataStore(
    name = "pack_dynasty_identity",
)

/** Records identity pack membership year at dynasty sign — overlay swaps never rewrite save IDs ([MEGA-GDD-019]). */
class PackDynastyIdentityRepository(private val context: Context) {
    suspend fun recordMembershipYearAtSign(dynastyId: String, membershipYear: String) {
        context.packDynastyIdentityStore.edit { prefs ->
            prefs[stringPreferencesKey(dynastyId)] = membershipYear
        }
    }

    suspend fun loadMembershipYearAtSign(dynastyId: String): String? =
        context.packDynastyIdentityStore.data.first()[stringPreferencesKey(dynastyId)]

}
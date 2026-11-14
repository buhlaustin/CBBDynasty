package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.player.loyalty.CoachPromiseType
import dev.novux.dynasty.cbb.core.player.loyalty.RecruitLockedCommitPromise
import kotlinx.coroutines.flow.first

private val Context.recruitLockedPromiseStore: DataStore<Preferences> by preferencesDataStore(
    name = "recruit_locked_promises",
)

class RecruitLockedPromiseRepository(private val context: Context) {
    suspend fun saveAll(dynastyId: String, locked: List<RecruitLockedCommitPromise>) {
        context.recruitLockedPromiseStore.edit { prefs ->
            prefs[key(dynastyId)] = encode(locked)
        }
    }

    suspend fun loadAll(dynastyId: String): Map<String, RecruitLockedCommitPromise> {
        val raw = context.recruitLockedPromiseStore.data.first()[key(dynastyId)] ?: return emptyMap()
        return decode(raw).associateBy { it.prospectId }
    }

    private fun key(dynastyId: String) = stringPreferencesKey("locked_promises_$dynastyId")

    private fun encode(locked: List<RecruitLockedCommitPromise>): String =
        locked.joinToString(";") { p ->
            val stay = p.stayTopN?.toString().orEmpty()
            "${p.prospectId}|${p.promiseType.wire}|$stay|${p.lockedAtSeasonIndex}"
        }

    private fun decode(raw: String): List<RecruitLockedCommitPromise> =
        raw.split(";").filter { it.isNotBlank() }.mapNotNull { token ->
            val parts = token.split("|")
            if (parts.size != 4) return@mapNotNull null
            val type = CoachPromiseType.fromWire(parts[1]) ?: return@mapNotNull null
            RecruitLockedCommitPromise(
                prospectId = parts[0],
                promiseType = type,
                stayTopN = parts[2].takeIf { it.isNotBlank() }?.toIntOrNull(),
                lockedAtSeasonIndex = parts[3].toInt(),
            )
        }
}

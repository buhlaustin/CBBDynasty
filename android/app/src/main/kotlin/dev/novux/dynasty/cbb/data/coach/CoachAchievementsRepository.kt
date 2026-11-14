package dev.novux.dynasty.cbb.data.coach

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.coach.achievements.CoachAchievementEngine
import dev.novux.dynasty.cbb.core.coach.achievements.CoachAchievementLedger
import dev.novux.dynasty.cbb.core.coach.achievements.HeadCoachAchievements
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.data.league.LeagueWeekBatchRepository
import dev.novux.dynasty.cbb.database.DynastyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.coachAchievementsStore: DataStore<Preferences> by preferencesDataStore(
    name = "coach_achievements",
)

class CoachAchievementsRepository(
    private val context: Context,
    private val database: DynastyDatabase,
    private val leagueWeekBatchRepository: LeagueWeekBatchRepository,
) {
    suspend fun ensureEmpty(dynastyId: String) = withContext(Dispatchers.IO) {
        val key = ledgerKey(dynastyId)
        if (context.coachAchievementsStore.data.first().contains(key)) return@withContext
        persist(CoachAchievementEngine.empty(dynastyId))
    }

    suspend fun loadLedger(dynastyId: String): CoachAchievementLedger = withContext(Dispatchers.IO) {
        val raw = context.coachAchievementsStore.data.first()[ledgerKey(dynastyId)]
            ?: return@withContext CoachAchievementEngine.empty(dynastyId)
        Json.decodeFromString(raw)
    }

    suspend fun snapshot(dynastyId: String, seasonIndex: Int): HeadCoachAchievements? = withContext(Dispatchers.IO) {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return@withContext null
        val ledger = loadLedger(dynastyId)
        val standings = leagueWeekBatchRepository.loadStandings(dynastyId, seasonIndex)
        CoachAchievementEngine.snapshot(ledger, standings, coach.userTeamId)
    }

    suspend fun finalizeSeason(
        dynastyId: String,
        standings: LeagueStandingsState,
    ) = withContext(Dispatchers.IO) {
        val coach = database.dynastyCoachDao().getByDynastyId(dynastyId) ?: return@withContext
        val ledger = loadLedger(dynastyId)
        val next = CoachAchievementEngine.finalizeSeason(ledger, standings, coach.userTeamId)
        persist(next)
    }

    private suspend fun persist(ledger: CoachAchievementLedger) {
        context.coachAchievementsStore.edit { prefs ->
            prefs[ledgerKey(ledger.dynastyId)] = Json.encodeToString(ledger)
        }
    }

    private fun ledgerKey(dynastyId: String) = stringPreferencesKey("ledger_$dynastyId")
}

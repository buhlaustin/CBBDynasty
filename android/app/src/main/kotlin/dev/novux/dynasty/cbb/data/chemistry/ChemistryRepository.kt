package dev.novux.dynasty.cbb.data.chemistry

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.balance.BalanceChemistry
import dev.novux.dynasty.cbb.core.chemistry.ChemistrySeasonState
import dev.novux.dynasty.cbb.core.chemistry.ChemistryWeekEngine
import dev.novux.dynasty.cbb.core.chemistry.ChemistryWeeklyTickInput
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.roster.RosterPlayer
import dev.novux.dynasty.cbb.data.balance.BalanceRulesetRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.chemistryStore: DataStore<Preferences> by preferencesDataStore(name = "team_chemistry")

class ChemistryRepository(
    private val context: Context,
    private val balanceRulesetRepository: BalanceRulesetRepository,
) {
    suspend fun loadState(dynastyId: String, seasonIndex: Int): ChemistrySeasonState {
        val key = stateKey(dynastyId, seasonIndex)
        val prefs = context.chemistryStore.data.first()
        val raw = prefs[key]
        if (raw == null) {
            val start = balanceRulesetRepository.activeRuleset().chemistry.startValue
            val boot = ChemistryWeekEngine.bootstrap(start)
            persist(dynastyId, seasonIndex, boot)
            return boot
        }
        return Json.decodeFromString(raw)
    }

    suspend fun afterWeekAdvance(
        dynastyId: String,
        seasonIndex: Int,
        phase: CalendarPhase,
        players: List<RosterPlayer>,
        tickInput: ChemistryWeeklyTickInput,
    ): ChemistrySeasonState {
        if (phase == CalendarPhase.Offseason || phase == CalendarPhase.Preseason) {
            return loadState(dynastyId, seasonIndex)
        }
        val tuning = balanceRulesetRepository.activeRuleset().chemistry
        val current = loadState(dynastyId, seasonIndex)
        val next = ChemistryWeekEngine.tickAfterWeekAdvance(current, players, tickInput, tuning)
        persist(dynastyId, seasonIndex, next)
        return next
    }

    fun clutchModifier(state: ChemistrySeasonState, tuning: BalanceChemistry): Double =
        dev.novux.dynasty.cbb.core.chemistry.ChemistryClutchModifier.winProbabilityDelta(
            state.teamChemistry,
            tuning,
        )

    private suspend fun persist(dynastyId: String, seasonIndex: Int, state: ChemistrySeasonState) {
        context.chemistryStore.edit {
            it[stateKey(dynastyId, seasonIndex)] = Json.encodeToString(state)
        }
    }

    private fun stateKey(dynastyId: String, seasonIndex: Int) =
        stringPreferencesKey("chem_${dynastyId}_$seasonIndex")
}

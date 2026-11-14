package dev.novux.dynasty.cbb.data.recruiting

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import kotlinx.coroutines.flow.first

private val Context.recruitingManualWeekSpendStore by preferencesDataStore(
    name = "recruiting_manual_week_spend",
)

/** Manual recruiting points spent this calendar week ([MEGA-FRAME-110], MEGA-QA-026). */
class RecruitingManualWeekSpendRepository(private val context: Context) {
    suspend fun totalForDynasty(loaded: LoadedDynasty): Int {
        val position = loaded.weekCheckpoint.activePosition
        return context.recruitingManualWeekSpendStore.data.first()[prefsKey(loaded.dynastyId, position)]
            ?.toIntOrNull() ?: 0
    }

    suspend fun recordSpend(dynastyId: String, position: CalendarPosition, pointsCharged: Int) {
        if (pointsCharged <= 0) return
        val key = prefsKey(dynastyId, position)
        context.recruitingManualWeekSpendStore.edit { prefs ->
            val prior = prefs[key]?.toIntOrNull() ?: 0
            prefs[key] = (prior + pointsCharged).toString()
        }
    }

    private fun prefsKey(dynastyId: String, position: CalendarPosition) = stringPreferencesKey(
        "week_${dynastyId}_${position.seasonIndex}_${position.phase.name}_${position.weekIndex}",
    )
}
package dev.novux.dynasty.cbb.core.preseason

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition
import dev.novux.dynasty.cbb.core.calendar.CalendarWeekDisplayDate
import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind

/** Calendar focus after preseason — [planning/preseason.md#advance-to-regular-season]. */
data class FirstPlayableGame(
    val gameId: String,
    val opponentTeamId: String,
    val opponentDisplayName: String,
    val homeGame: Boolean,
    val kind: ScheduleGameKind,
    val calendarWeekIndex: Int,
) {
    fun gameDateLabel(seasonIndex: Int): String = CalendarWeekDisplayDate.gameDateLabel(
        CalendarPosition(
            seasonIndex = seasonIndex,
            phase = when (kind) {
                ScheduleGameKind.Exhibition -> CalendarPhase.Exhibition
                else -> CalendarPhase.RegularSeason
            },
            weekIndex = calendarWeekIndex,
        ),
    ).orEmpty()
}

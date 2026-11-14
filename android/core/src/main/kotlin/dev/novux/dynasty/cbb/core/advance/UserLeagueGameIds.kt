package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.calendar.CalendarPosition

/** Stable checkpoint game id for the user's scheduled game this week. */
object UserLeagueGameIds {
    fun forPosition(position: CalendarPosition, index: Int = 0): String =
        "user-week-${position.seasonIndex}-${position.phase.name}-${position.weekIndex}-$index"
}

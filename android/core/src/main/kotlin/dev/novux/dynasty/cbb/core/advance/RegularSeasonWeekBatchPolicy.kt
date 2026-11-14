package dev.novux.dynasty.cbb.core.advance

import dev.novux.dynasty.cbb.core.GameConstants
import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition

/**
 * RS weeks 1–32: UI week index is the save checkpoint; league-wide sim is a silent batch behind confirm copy only.
 * ([MEGA-FRAME-152], [planning/time-system.md]).
 */
object RegularSeasonWeekBatchPolicy {
    const val MEGA_FRAME_ID = "MEGA-FRAME-152"

    fun isRegularSeasonWeek(position: CalendarPosition): Boolean =
        position.phase == CalendarPhase.RegularSeason &&
            position.weekIndex in 1..GameConstants.MAX_REGULAR_SEASON_GAMES
}
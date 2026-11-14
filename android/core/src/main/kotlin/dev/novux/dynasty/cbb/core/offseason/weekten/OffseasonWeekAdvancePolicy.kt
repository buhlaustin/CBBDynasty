package dev.novux.dynasty.cbb.core.offseason.weekten

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.calendar.CalendarPosition

/** Offseason-only week transitions (conference week skip on season 1). */
object OffseasonWeekAdvancePolicy {
    const val MEGA_FRAME_ID: String = "MEGA-FRAME-067"

    /** True on last offseason week before preseason (week 9 season 1, week 10 season 2+). */
    fun showsAdvanceToPreseasonCta(position: CalendarPosition): Boolean {
        if (position.phase != CalendarPhase.Offseason) return false
        if (position.weekIndex >= OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX) return true
        return position.weekIndex == 9 &&
            OffseasonConferenceOffersPolicy.skipConferenceOffersWeek(position.seasonIndex)
    }

    fun nextPosition(current: CalendarPosition): CalendarPosition {
        require(current.phase == CalendarPhase.Offseason) { "offseason only" }
        if (current.weekIndex == 9 && OffseasonConferenceOffersPolicy.skipConferenceOffersWeek(current.seasonIndex)) {
            return CalendarPosition(
                seasonIndex = current.seasonIndex,
                phase = CalendarPhase.Preseason,
                weekIndex = 1,
            )
        }
        if (current.weekIndex < OffseasonWeekTenConstants.OFFSEASON_WEEK_INDEX) {
            return current.copy(weekIndex = current.weekIndex + 1)
        }
        return CalendarPosition(
            seasonIndex = current.seasonIndex,
            phase = CalendarPhase.Preseason,
            weekIndex = 1,
        )
    }
}

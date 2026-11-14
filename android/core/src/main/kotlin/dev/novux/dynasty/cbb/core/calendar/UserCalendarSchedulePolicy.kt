package dev.novux.dynasty.cbb.core.calendar

import dev.novux.dynasty.cbb.core.schedule.ScheduleGameKind

/**
 * User calendar shows **your** schedule only ([planning/game-frame.md#calendar-tab]) —
 * never the full D-I weekly slate.
 */
object UserCalendarSchedulePolicy {
    /** Display order within a week: plan list order, with section breaks when kind changes. */
    fun gamesInChronologicalPlanOrder(games: List<UserCalendarGameEntry>): List<UserCalendarGameEntry> =
        games

    fun sectionBreakBefore(
        index: Int,
        games: List<UserCalendarGameEntry>,
    ): ScheduleGameKind? {
        if (games.size <= 1 || index == 0) return null
        val current = games[index].gameKind
        val previous = games[index - 1].gameKind
        return if (current.calendarSectionKind() != previous.calendarSectionKind()) current else null
    }

    private fun ScheduleGameKind.calendarSectionKind(): ScheduleGameKind = when (this) {
        ScheduleGameKind.Conference -> ScheduleGameKind.Conference
        ScheduleGameKind.NonConference -> ScheduleGameKind.NonConference
        ScheduleGameKind.EarlyEvent,
        ScheduleGameKind.Exhibition,
        -> ScheduleGameKind.EarlyEvent
    }
}

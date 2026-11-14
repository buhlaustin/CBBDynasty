package dev.novux.dynasty.cbb.core.dashboard

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase

/**
 * In-season recruiting optional dashboard inputs ([planning/dashboard-tasks.md#in-season-exhibitions--postseason]).
 */
data class InSeasonRecruitingDashboardContext(
    val phase: CalendarPhase,
    val offseasonWeekIndex: Int? = null,
    val pointsAvailable: Int,
    val cycleClosed: Boolean,
    val recruitingNudges: List<RecruitingDashboardNudge> = emptyList(),
)

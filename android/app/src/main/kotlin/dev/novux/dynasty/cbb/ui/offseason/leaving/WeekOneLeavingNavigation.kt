package dev.novux.dynasty.cbb.ui.offseason.leaving

/** One-shot navigation from Week 1 dashboard task cards into [PlayersLeavingPanel]. */
data class WeekOneLeavingNavigation(
    val tabIndex: Int = 0,
    val emphasizeProDays: Boolean = false,
)

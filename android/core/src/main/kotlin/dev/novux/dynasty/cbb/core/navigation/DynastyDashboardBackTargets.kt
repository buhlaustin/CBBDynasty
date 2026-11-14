package dev.novux.dynasty.cbb.core.navigation

/**
 * Nav route ids for in-career dashboards (not main menu). Order = prefer nearest parent when
 * popping from hubs such as Recruiting ([MEGA-QA-046]).
 */
object DynastyDashboardBackTargets {
    const val DYNASTY_CAREER = "dynasty_career"
    const val DYNASTY_SHELL = "dynasty_shell"
    const val OFFSEASON = "offseason"
    const val PRESEASON = "preseason"
    const val SEASON_REVIEW = "season_review"

    val ORDERED: List<String> = listOf(
        DYNASTY_CAREER,
        DYNASTY_SHELL,
        OFFSEASON,
        PRESEASON,
        SEASON_REVIEW,
    )
}

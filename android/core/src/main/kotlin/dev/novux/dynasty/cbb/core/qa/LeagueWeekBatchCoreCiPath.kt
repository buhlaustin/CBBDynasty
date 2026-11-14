package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchPerformanceBudget

/**
 * CI gate for full 365-team RS week batch time ([MEGA-QA-015]).
 */
object LeagueWeekBatchCoreCiPath {
    const val QA_ID: String = "MEGA-QA-015"

    const val GRADLE_TASK: String = ":core:test"

    const val GRADLE_TEST_FILTER: String =
        "dev.novux.dynasty.cbb.core.league.LeagueWeekBatchPerformanceTest"

    const val CI_WORKFLOW: String = ".github/workflows/core-test.yml"

    const val VERIFY_SCRIPT: String = "scripts/verify_league_week_batch_core_budget.sh"

    val TEAM_COUNT: Int = LeagueWeekBatchPerformanceBudget.FULL_LEAGUE_TEAM_COUNT

    val MAX_MEDIAN_WEEK_MS: Long = LeagueWeekBatchPerformanceBudget.MAX_CORE_REGULAR_SEASON_WEEK_MS

    val BENCHMARK_ITERATIONS: Int = LeagueWeekBatchPerformanceBudget.BENCHMARK_ITERATIONS
}

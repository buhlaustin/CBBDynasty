package dev.novux.dynasty.cbb.core.league

/**
 * Time budgets for full-league **week advance** work in `:core` (batch sim + weekly NET/polls).
 *
 * Product target (mid-tier phone, UI thread): advance should feel instant — see
 * `docs/LEAGUE_WEEK_BATCH_PROFILING.md`.
 */
object LeagueWeekBatchPerformanceBudget {
    const val FULL_LEAGUE_TEAM_COUNT = 365

    /**
     * JVM CI ceiling for one RS week (fast batch scores + NET + Media/Coaches polls).
     * Mobile ships the same `:core` path; device profiling uses the doc workflow.
     */
    const val MAX_CORE_REGULAR_SEASON_WEEK_MS: Long = 12_000L

    /** Mid-tier device: UI thread should not run league batch work during Sim Week ([MEGA-QA-014]). */
    const val MAX_PRODUCT_UI_ADVANCE_MS: Long = 2_500L

    /**
     * Scaled JVM smoke for product perceived budget — 64-team RS week, not full 365 (see CI test above).
     */
    const val MAX_PRODUCT_SMOKE_REGULAR_SEASON_WEEK_MS: Long = MAX_PRODUCT_UI_ADVANCE_MS

    const val PRODUCT_SMOKE_TEAM_COUNT = 64

    const val BENCHMARK_ITERATIONS = 3
}

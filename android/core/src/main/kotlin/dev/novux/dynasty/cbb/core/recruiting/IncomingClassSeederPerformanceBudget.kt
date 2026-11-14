package dev.novux.dynasty.cbb.core.recruiting

/** JVM guard for new-dynasty recruiting seed ([MEGA-QA-001], [MEGA-QA-012]). */
object IncomingClassSeederPerformanceBudget {
    const val BENCHMARK_ITERATIONS = 3

    /** Wall time for full HS+JUCO pool generation on CI hardware — subset of the 3 s new-game sign budget. */
    const val MAX_SEED_WALL_MS: Long = 4_000L
}

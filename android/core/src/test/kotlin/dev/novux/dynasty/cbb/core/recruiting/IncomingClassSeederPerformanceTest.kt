package dev.novux.dynasty.cbb.core.recruiting

import kotlin.system.measureNanoTime
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingClassSeederPerformanceTest {
    @Test
    fun fullPoolSeed_underCoreBudget() {
        val timingsMs = mutableListOf<Long>()
        repeat(IncomingClassSeederPerformanceBudget.BENCHMARK_ITERATIONS) { iteration ->
            val elapsedMs = measureNanoTime {
                val pool = IncomingClassSeeder.seed(
                    enrollingSeasonIndex = 1,
                    seedKey = 42_000L + iteration,
                )
                assertTrue(pool.size >= IncomingClassSeeder.HS_POOL_MIN + IncomingClassSeeder.JUCO_POOL_SIZE)
            } / 1_000_000
            timingsMs += elapsedMs
        }
        val medianMs = timingsMs.sorted()[timingsMs.size / 2]
        assertTrue(
            "IncomingClassSeeder median ${medianMs}ms (all $timingsMs ms) exceeds " +
                "${IncomingClassSeederPerformanceBudget.MAX_SEED_WALL_MS}ms",
            medianMs <= IncomingClassSeederPerformanceBudget.MAX_SEED_WALL_MS,
        )
    }
}

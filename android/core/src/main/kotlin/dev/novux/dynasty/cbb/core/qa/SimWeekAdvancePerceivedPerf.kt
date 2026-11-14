package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.league.LeagueWeekBatchPerformanceBudget

/**
 * Perceived Sim Week advance timing on device ([MEGA-QA-128], [MEGA-QA-014]).
 * UI records elapsed realtime while the advance coroutine runs (spinner visible).
 */
object SimWeekAdvancePerceivedPerf {
    const val QA_ID: String = "MEGA-QA-128"

    /** Mid-season RS week for device profiling per `LEAGUE_WEEK_BATCH_PROFILING.md`. */
    const val DEVICE_PROFILE_RS_WEEK_INDEX: Int = 5

    val maxPerceivedMs: Long = LeagueWeekBatchPerformanceBudget.MAX_PRODUCT_UI_ADVANCE_MS

    data class Sample(
        val elapsedMs: Long,
        val recordedAtEpochMs: Long,
    )

    @Volatile
    var lastSample: Sample? = null

    fun recordSample(elapsedMs: Long) {
        lastSample = Sample(elapsedMs, System.currentTimeMillis())
    }

    fun withinDeviceBudget(elapsedMs: Long): Boolean = elapsedMs <= maxPerceivedMs
}

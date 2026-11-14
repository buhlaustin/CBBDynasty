package dev.novux.dynasty.cbb.ui.advance

import android.os.SystemClock
import android.util.Log
import dev.novux.dynasty.cbb.BuildConfig
import dev.novux.dynasty.cbb.core.qa.SimWeekAdvancePerceivedPerf
import dev.novux.dynasty.cbb.core.save.DynastyLoadResult

/**
 * Measures perceived advance latency from the UI coroutine ([MEGA-QA-128]).
 */
object SimWeekAdvanceUiPerf {
    private const val LOG_TAG = "SimWeekAdvancePerf"

    suspend fun measure(block: suspend () -> DynastyLoadResult): DynastyLoadResult {
        val started = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val elapsedMs = SystemClock.elapsedRealtime() - started
            SimWeekAdvancePerceivedPerf.recordSample(elapsedMs)
            if (BuildConfig.DEBUG && !SimWeekAdvancePerceivedPerf.withinDeviceBudget(elapsedMs)) {
                Log.w(
                    LOG_TAG,
                    "Perceived sim-week advance ${elapsedMs}ms exceeds product budget " +
                        "${SimWeekAdvancePerceivedPerf.maxPerceivedMs}ms (${SimWeekAdvancePerceivedPerf.QA_ID})",
                )
            }
        }
    }
}

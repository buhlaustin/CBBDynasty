package dev.novux.dynasty.cbb.ui.advance

import android.os.SystemClock
import android.util.Log
import dev.novux.dynasty.cbb.BuildConfig
import dev.novux.dynasty.cbb.core.advance.SimWeekConfirmation
import dev.novux.dynasty.cbb.core.qa.SimWeekAdvancePreDialogPerf

/**
 * Measures latency building the advance confirmation before the dialog is shown ([MEGA-QA-008]).
 */
object SimWeekAdvanceConfirmationUiPerf {
    private const val LOG_TAG = "SimWeekAdvancePreDialog"

    suspend fun measure(block: suspend () -> SimWeekConfirmation?): SimWeekConfirmation? {
        val started = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val elapsedMs = SystemClock.elapsedRealtime() - started
            SimWeekAdvancePreDialogPerf.recordSample(elapsedMs)
            if (BuildConfig.DEBUG && !SimWeekAdvancePreDialogPerf.withinDeviceBudget(elapsedMs)) {
                Log.w(
                    LOG_TAG,
                    "Advance confirmation build ${elapsedMs}ms exceeds product budget " +
                        "${SimWeekAdvancePreDialogPerf.MAX_PRE_DIALOG_MS}ms " +
                        "(${SimWeekAdvancePreDialogPerf.QA_ID})",
                )
            }
        }
    }
}

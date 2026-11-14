package dev.novux.dynasty.cbb.core.qa

/**
 * Time from Advance tap until the confirmation dialog is shown ([MEGA-QA-008]).
 *
 * Measured on device from the shell coroutine before [pendingAdvanceConfirmation] is set.
 */
object SimWeekAdvancePreDialogPerf {
    const val QA_ID: String = "MEGA-QA-008"

    /** Playtest scenario: in-season regular season week 5, first Advance tap. */
    const val DEVICE_PROFILE_RS_WEEK_INDEX: Int = 5

    const val MAX_PRE_DIALOG_MS: Long = 500L

    data class Sample(
        val elapsedMs: Long,
        val recordedAtEpochMs: Long,
    )

    @Volatile
    var lastSample: Sample? = null

    fun recordSample(elapsedMs: Long) {
        lastSample = Sample(elapsedMs, System.currentTimeMillis())
    }

    fun withinDeviceBudget(elapsedMs: Long): Boolean = elapsedMs <= MAX_PRE_DIALOG_MS
}

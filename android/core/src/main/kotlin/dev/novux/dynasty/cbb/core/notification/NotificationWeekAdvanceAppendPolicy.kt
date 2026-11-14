package dev.novux.dynasty.cbb.core.notification

/** After week advance, append one feed row per user-relevant event ([MEGA-SHELL-128], [MEGA-SHELL-132]). */
object NotificationWeekAdvanceAppendPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-128"

    const val ONE_ROW_PER_EVENT: Boolean = true

    const val PIPELINE_STAGE_ID: String = "MEGA-SHELL-132"
}
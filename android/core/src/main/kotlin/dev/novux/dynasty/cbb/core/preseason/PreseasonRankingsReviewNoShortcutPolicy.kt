package dev.novux.dynasty.cbb.core.preseason

/**
 * Rankings review task completes only through the review sheet — no generic Complete chip
 * ([MEGA-RECR-056], [PreseasonFeatureTaskCompletionPolicy]).
 */
object PreseasonRankingsReviewNoShortcutPolicy {
    const val MEGA_ID: String = "MEGA-RECR-056"

    fun blocksGenericCompleteButton(taskId: PreseasonTaskId): Boolean =
        !PreseasonFeatureTaskCompletionPolicy.allowsGenericCompleteButton(taskId)
}
package dev.novux.dynasty.cbb.core.notification

/**
 * Tasks vs feed duplication rules ([MEGA-SHELL-133]).
 * Discipline keeps a feed row alongside the required task (different copy surfaces).
 * Required carousel approval skips redundant feed rows when the required task card is active.
 */
object NotificationTaskFeedDedupPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-133"

    fun includeDisciplineFeedRow(): Boolean = true

    fun includeCarouselFeedRows(hasRequiredCarouselApprovalTask: Boolean): Boolean =
        !hasRequiredCarouselApprovalTask
}
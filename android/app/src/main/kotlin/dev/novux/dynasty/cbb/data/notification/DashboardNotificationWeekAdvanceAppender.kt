package dev.novux.dynasty.cbb.data.notification

import dev.novux.dynasty.cbb.core.notification.NotificationSnapshotInput
import dev.novux.dynasty.cbb.core.notification.NotificationTaskFeedDedupPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationWeekAdvanceAppendPolicy
import dev.novux.dynasty.cbb.core.save.LoadedDynasty

/**
 * Appends user-relevant notification feed rows after confirmed week advance
 * ([NotificationWeekAdvanceAppendPolicy]).
 */
class DashboardNotificationWeekAdvanceAppender(
    private val notificationFeedRepository: NotificationFeedRepository,
) {
    suspend fun appendAfterWeekAdvance(
        loaded: LoadedDynasty,
        userNetRankBefore: Int?,
        userNetRankAfter: Int?,
        disciplineHeadline: String?,
        hasRequiredCarouselApprovalTask: Boolean,
    ) {
        val rankingsLine = when {
            userNetRankBefore == null || userNetRankAfter == null -> null
            userNetRankBefore == userNetRankAfter -> null
            else -> "Your program moved from NET #$userNetRankBefore to #$userNetRankAfter."
        }
        val discipline = disciplineHeadline?.takeIf { NotificationTaskFeedDedupPolicy.includeDisciplineFeedRow() }
        if (!NotificationTaskFeedDedupPolicy.includeCarouselFeedRows(hasRequiredCarouselApprovalTask)) {
            // Carousel feed rows hydrate on dashboard load; week-advance path only appends deltas here.
        }
        val input = NotificationSnapshotInput(
            asOfMs = System.currentTimeMillis(),
            rankingsMoveLine = rankingsLine,
            disciplineHeadline = discipline,
        )
        if (rankingsLine == null && discipline == null) return
        notificationFeedRepository.refreshFromSnapshot(loaded.dynastyId, input)
    }

    companion object {
        const val STAGE_MARKER: String = "dashboardNotificationWeekAdvanceAppender.appendAfterWeekAdvance"
    }
}
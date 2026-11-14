package dev.novux.dynasty.cbb.core.notification

/** UI lists use priority tier first, then newest within tier ([MEGA-SHELL-123]). */
object NotificationFeedTierSortPolicy {
    const val MEGA_ID: String = "MEGA-SHELL-123"

    fun sort(feed: List<NotificationFeedItem>): List<NotificationFeedItem> =
        NotificationFeedEngine.sort(feed)
}
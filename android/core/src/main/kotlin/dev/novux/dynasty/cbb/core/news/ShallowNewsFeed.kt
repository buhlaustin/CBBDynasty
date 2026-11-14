package dev.novux.dynasty.cbb.core.news

/** Top-N shallow slice for dashboard News tab ([planning/game-frame.md#news-tab-dashboard]). */
object ShallowNewsFeed {
    fun select(
        archive: List<NewsHeadline>,
        extra: List<NewsHeadline> = emptyList(),
        maxItems: Int = NewsHeadlinePublisher.SHALLOW_FEED_CAP,
    ): List<NewsHeadline> = snapshot(archive, extra, maxItems).headlines

    fun snapshot(
        archive: List<NewsHeadline>,
        extra: List<NewsHeadline> = emptyList(),
        maxItems: Int = NewsHeadlinePublisher.SHALLOW_FEED_CAP,
    ): ShallowNewsFeedSnapshot {
        val cap = maxItems.coerceAtLeast(1)
        val merged = (archive + extra)
            .distinctBy { it.id }
            .sortedWith(compareByDescending<NewsHeadline> { it.priority }.thenByDescending { it.createdAtMs })
        return ShallowNewsFeedSnapshot(
            headlines = merged.take(cap),
            cap = cap,
            mergedCount = merged.size,
        )
    }
}

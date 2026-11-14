package dev.novux.dynasty.cbb.core.news

/** Dashboard News tab slice vs full hub archive ([planning/game-frame.md#news-tab-dashboard]). */
data class ShallowNewsFeedSnapshot(
    val headlines: List<NewsHeadline>,
    val cap: Int,
    val mergedCount: Int,
) {
    val truncated: Boolean get() = mergedCount > headlines.size
    companion object {
        val Empty = ShallowNewsFeedSnapshot(emptyList(), NewsHeadlinePublisher.SHALLOW_FEED_CAP, 0)
    }
}

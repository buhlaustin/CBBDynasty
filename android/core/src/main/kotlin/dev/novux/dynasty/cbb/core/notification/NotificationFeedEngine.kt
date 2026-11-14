package dev.novux.dynasty.cbb.core.notification

object NotificationFeedEngine {
    private val priorityOrder = NotificationPriority.entries.withIndex().associate { it.value to it.index }

    fun sort(feed: List<NotificationFeedItem>): List<NotificationFeedItem> =
        feed.sortedWith(
            compareBy<NotificationFeedItem> { priorityOrder[it.priority] ?: 0 }
                .thenByDescending { it.createdAtMs },
        )

    fun mergePreservingAck(
        existing: List<NotificationFeedItem>,
        incoming: List<NotificationFeedItem>,
    ): List<NotificationFeedItem> {
        val ackByKey = existing.associate { it.eventKey to it.modalAcknowledged }
        val merged = (existing + incoming)
            .groupBy { it.eventKey }
            .map { (_, rows) -> rows.maxByOrNull { it.createdAtMs }!! }
            .map { row ->
                val ack = ackByKey[row.eventKey] == true
                row.copy(modalAcknowledged = ack || row.modalAcknowledged)
            }
        return NotificationFeedTierSortPolicy.sort(merged).take(MAX_ROWS)
    }

    const val MAX_ROWS = 48
}

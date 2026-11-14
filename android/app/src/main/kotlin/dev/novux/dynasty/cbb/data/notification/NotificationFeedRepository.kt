package dev.novux.dynasty.cbb.data.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.notification.NotificationEventKind
import dev.novux.dynasty.cbb.core.notification.NotificationFeedComposer
import dev.novux.dynasty.cbb.core.notification.NotificationFeedTierSortPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedItem
import dev.novux.dynasty.cbb.core.notification.NotificationPriority
import dev.novux.dynasty.cbb.core.notification.NotificationSnapshotInput
import kotlinx.coroutines.flow.first

private val Context.notificationFeedStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_feed",
)

class NotificationFeedRepository(private val context: Context) {
    suspend fun loadFeed(dynastyId: String): List<NotificationFeedItem> {
        val raw = context.notificationFeedStore.data.first()[feedKey(dynastyId)] ?: return emptyList()
        return NotificationFeedTierSortPolicy.sort(decode(raw))
    }

    suspend fun refreshFromSnapshot(dynastyId: String, input: NotificationSnapshotInput): List<NotificationFeedItem> {
        val existing = loadFeed(dynastyId)
        val incoming = NotificationFeedComposer.compose(input)
        val merged = NotificationFeedEngine.mergePreservingAck(existing, incoming)
        persist(dynastyId, merged)
        return merged
    }

    suspend fun appendCommit(
        dynastyId: String,
        prospectName: String,
        portal: Boolean = false,
        atMs: Long = System.currentTimeMillis(),
    ) {
        val input = if (portal) {
            NotificationSnapshotInput(asOfMs = atMs, userPortalCommitsThisWeek = listOf(prospectName))
        } else {
            NotificationSnapshotInput(asOfMs = atMs, userCommitsThisWeek = listOf(prospectName))
        }
        refreshFromSnapshot(dynastyId, input)
    }

    suspend fun appendMajorRecord(dynastyId: String, line: String, atMs: Long = System.currentTimeMillis()) {
        refreshFromSnapshot(
            dynastyId,
            NotificationSnapshotInput(asOfMs = atMs, majorRecordLines = listOf(line)),
        )
    }

    suspend fun acknowledgeCareerModal(dynastyId: String, eventKey: String) {
        val next = loadFeed(dynastyId).map { row ->
            if (row.eventKey == eventKey) row.copy(modalAcknowledged = true) else row
        }
        persist(dynastyId, next)
    }

    suspend fun pendingCareerModal(dynastyId: String): NotificationFeedItem? =
        loadFeed(dynastyId).firstOrNull { it.requiresBlockingModal && !it.modalAcknowledged }

    private suspend fun persist(dynastyId: String, feed: List<NotificationFeedItem>) {
        context.notificationFeedStore.edit { prefs ->
            prefs[feedKey(dynastyId)] = encode(feed)
        }
    }

    private fun feedKey(dynastyId: String) = stringPreferencesKey("feed_$dynastyId")

    private fun encode(feed: List<NotificationFeedItem>): String =
        feed.joinToString("\n") { row ->
            listOf(
                row.id,
                row.eventKey,
                row.kind.name,
                row.priority.name,
                row.deepLink.name,
                row.title.replace('|', '/'),
                row.body.replace('|', '/').replace('\n', ' '),
                row.createdAtMs.toString(),
                row.requiresBlockingModal.toString(),
                row.modalAcknowledged.toString(),
            ).joinToString("|")
        }

    private fun decode(raw: String): List<NotificationFeedItem> =
        raw.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val p = line.split("|")
            if (p.size < 10) return@mapNotNull null
            runCatching {
                NotificationFeedItem(
                    id = p[0],
                    eventKey = p[1],
                    kind = NotificationEventKind.valueOf(p[2]),
                    priority = NotificationPriority.valueOf(p[3]),
                    deepLink = DashboardDeepLink.valueOf(p[4]),
                    title = p[5],
                    body = p[6],
                    createdAtMs = p[7].toLong(),
                    requiresBlockingModal = p[8].toBooleanStrict(),
                    modalAcknowledged = p[9].toBooleanStrict(),
                )
            }.getOrNull()
        }
}

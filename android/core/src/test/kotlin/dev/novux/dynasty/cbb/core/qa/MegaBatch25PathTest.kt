package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.news.NewsHubBackNavigationPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationCategoryMutePolicy
import dev.novux.dynasty.cbb.core.notification.NotificationEventKind
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCarouselComposerPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCategory
import dev.novux.dynasty.cbb.core.notification.NotificationFeedCommitRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedComposer
import dev.novux.dynasty.cbb.core.notification.NotificationFeedDisciplineComposerPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedMajorRecordRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedMedicalRedshirtComposerPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedSigningDayFeedPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedItem
import dev.novux.dynasty.cbb.core.notification.NotificationFeedTierSortPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationPriority
import dev.novux.dynasty.cbb.core.notification.NotificationSnapshotInput
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch25PathTest {
    @Test
    fun notificationFeedPoliciesBatchWired() {
        assertEquals("MEGA-SHELL-114", NotificationFeedCommitRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-115", NotificationFeedMajorRecordRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-117", NotificationFeedSigningDayFeedPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-118", NewsHubBackNavigationPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-121", NotificationCategoryMutePolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-123", NotificationFeedTierSortPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-125", NotificationFeedCarouselComposerPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-126", NotificationFeedMedicalRedshirtComposerPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-127", NotificationFeedDisciplineComposerPolicy.MEGA_ID)

        val portalCommit = NotificationFeedComposer.compose(
            NotificationSnapshotInput(
                asOfMs = 1L,
                userPortalCommitsThisWeek = listOf("Transfer X"),
            ),
        ).single()
        assertEquals(NotificationFeedCommitRowPolicy.portalCommitDeepLink, portalCommit.deepLink)

        val record = NotificationFeedComposer.compose(
            NotificationSnapshotInput(asOfMs = 2L, majorRecordLines = listOf("Record")),
        ).single()
        assertEquals(NotificationFeedMajorRecordRowPolicy.deepLink, record.deepLink)

        val sorted = NotificationFeedTierSortPolicy.sort(
            listOf(
                stubRow(priority = NotificationPriority.Low, createdAtMs = 10L),
                stubRow(priority = NotificationPriority.High, createdAtMs = 5L),
                stubRow(priority = NotificationPriority.High, createdAtMs = 20L),
            ),
        )
        assertEquals(20L, sorted[0].createdAtMs)
        assertEquals(5L, sorted[1].createdAtMs)

        val muted = NotificationCategoryMutePolicy.filterVisible(
            listOf(
                stubRow(kind = NotificationEventKind.UserCommit),
                stubRow(kind = NotificationEventKind.RankingsMove),
            ),
            setOf(NotificationFeedCategory.Recruiting),
        )
        assertEquals(1, muted.size)
        assertEquals(NotificationEventKind.RankingsMove, muted[0].kind)

        val root = repoRoot()
        val panel = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dashboard/DashboardNotificationFeedPanel.kt",
        ).readText()
        assertTrue(panel.contains("onSigningDayOpen(row.deepLink)"))
        assertTrue(panel.contains("NotificationFeedSigningDayFeedPolicy"))

        val dynastyApp = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt",
        ).readText()
        assertTrue(dynastyApp.contains("popBackToInShellDashboard"))
        assertTrue(dynastyApp.contains("AppRoute.NewsHub.route"))

        val settings = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/SettingsScreen.kt",
        ).readText()
        assertTrue(settings.contains("setNotificationCategoryMuted"))
    }

    private fun stubRow(
        priority: NotificationPriority = NotificationPriority.Medium,
        createdAtMs: Long = 0L,
        kind: NotificationEventKind = NotificationEventKind.GameThisWeek,
    ) = NotificationFeedItem(
        id = "id",
        eventKey = "key",
        kind = kind,
        priority = priority,
        deepLink = DashboardDeepLink.Strategy,
        title = "t",
        body = "b",
        createdAtMs = createdAtMs,
    )

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
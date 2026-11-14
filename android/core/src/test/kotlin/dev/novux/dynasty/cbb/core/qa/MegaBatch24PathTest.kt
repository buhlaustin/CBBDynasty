package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.dashboard.DashboardDeepLink
import dev.novux.dynasty.cbb.core.dashboard.DashboardNotificationDeepLinkCatalogPolicy
import dev.novux.dynasty.cbb.core.news.NewsHeadline
import dev.novux.dynasty.cbb.core.news.NewsHeadlineKind
import dev.novux.dynasty.cbb.core.news.NewsPortalStoryFooterPolicy
import dev.novux.dynasty.cbb.core.news.NewsStoryDrillInPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationEventKind
import dev.novux.dynasty.cbb.core.notification.NotificationFeedDisciplineFeedPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedGameWeekRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedMedicalRedshirtFeedPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedRankingsMoveRowPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationFeedSelectionSundayFeedPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch24PathTest {
    @Test
    fun feedAndNewsDeepLinkBatchWired() {
        assertEquals("MEGA-SHELL-106", DashboardNotificationDeepLinkCatalogPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-107", NotificationFeedRankingsMoveRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-108", NotificationFeedGameWeekRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-109", NotificationFeedSelectionSundayFeedPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-110", NotificationFeedDisciplineFeedPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-111", NotificationFeedMedicalRedshirtFeedPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-112", NewsStoryDrillInPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-113", NewsPortalStoryFooterPolicy.MEGA_ID)
        assertTrue(DashboardNotificationDeepLinkCatalogPolicy.isCatalogued(DashboardDeepLink.RankingsHub))
        assertTrue(DashboardNotificationDeepLinkCatalogPolicy.isCatalogued(DashboardDeepLink.HistoryHub))
        assertEquals(DashboardDeepLink.RankingsHub, NotificationFeedRankingsMoveRowPolicy.deepLink)
        assertEquals(DashboardDeepLink.PlayWeekGame, NotificationFeedGameWeekRowPolicy.deepLink)
        assertEquals(NotificationEventKind.SelectionSunday, NotificationFeedSelectionSundayFeedPolicy.eventKind)
        assertEquals(NotificationEventKind.DisciplinePending, NotificationFeedDisciplineFeedPolicy.eventKind)
        assertEquals(NotificationEventKind.MedicalRedshirtEligible, NotificationFeedMedicalRedshirtFeedPolicy.eventKind)
        assertTrue(NewsStoryDrillInPolicy.opensMiniArticleDetail())
        val portalHeadline = NewsHeadline(
            id = "h1",
            seasonIndex = 1,
            kind = NewsHeadlineKind.RecruitingSpotlight,
            title = "Portal movement",
        )
        assertTrue(NewsPortalStoryFooterPolicy.showViewInPortalFooter(portalHeadline))

        val root = repoRoot()
        val composer = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/notification/NotificationFeedComposer.kt",
        ).readText()
        assertTrue(composer.contains("NotificationFeedRankingsMoveRowPolicy"))
        assertTrue(composer.contains("NotificationFeedGameWeekRowPolicy"))
        val panel = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dashboard/DashboardNotificationFeedPanel.kt",
        ).readText()
        assertTrue(panel.contains("DashboardDeepLink.RankingsHub"))
        assertTrue(panel.contains("NotificationFeedGameWeekRowPolicy"))
        val newsTab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellTabContent.kt",
        ).readText()
        assertTrue(newsTab.contains("NewsHeadlineStoryDetailSheet"))
        assertTrue(newsTab.contains("NewsStoryDrillInPolicy"))
        val hub = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/news/NewsHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("NewsHeadlineStoryDetailSheet"))
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/news/NewsHeadlineStoryDetailSheet.kt",
            ).readText().contains("NewsPortalStoryFooterPolicy"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
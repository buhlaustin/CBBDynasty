package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.news.NewsHubPortalAddToBoardPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalFiltersPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalHumanRowPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalRecentSectionsPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalRowNavigationPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubPortalTabVisibilityPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch20PathTest {
    @Test
    fun newsHubPortalBatchWired() {
        assertEquals("MEGA-SHELL-028", NewsHubPortalTabVisibilityPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-031", NewsHubPortalHumanRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-032", NewsHubPortalRecentSectionsPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-033", NewsHubPortalFiltersPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-034", NewsHubPortalRowNavigationPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-035", NewsHubPortalAddToBoardPolicy.MEGA_ID)
        assertFalse(NewsHubPortalTabVisibilityPolicy.showPortalTab(false))
        assertTrue(NewsHubPortalTabVisibilityPolicy.showPortalTab(true))
        assertEquals(RecruitingHubTab.TransferPortal, NewsHubPortalAddToBoardPolicy.recruitingHubTab)
        val hub = repoRoot().resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/news/NewsHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("NewsHubPortalTabVisibilityPolicy"))
        assertTrue(hub.contains("visibleTabs"))
        assertTrue(hub.contains("NewsHubPortalHumanRowPolicy"))
        assertTrue(hub.contains("news_hub_portal_add_to_board"))
        assertTrue(hub.contains("NewsHubPortalRowNavigationPolicy"))
        assertTrue(
            repoRoot().resolve("android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt")
                .readText()
                .contains("NewsHubPortalAddToBoardPolicy"),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
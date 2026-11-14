package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.history.HistoryHubDepthPolicy
import dev.novux.dynasty.cbb.core.history.HistoryHubSnapshot
import dev.novux.dynasty.cbb.core.history.HistoryYearCard
import dev.novux.dynasty.cbb.core.navigation.HubRouteShellRetentionPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubFocusPolicy
import dev.novux.dynasty.cbb.core.recruiting.RecruitingHubTab
import dev.novux.dynasty.cbb.core.seasonreview.SeasonReviewMenuPolicy
import dev.novux.dynasty.cbb.core.shell.InSeasonShellMenuExtrasPolicy
import dev.novux.dynasty.cbb.core.shell.ShellHamburgerDrawerPolicy
import dev.novux.dynasty.cbb.core.statistics.StatisticsHubLiveContentPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch23PathTest {
    @Test
    fun shellHubMenuBatchWired() {
        assertEquals("MEGA-SHELL-093", InSeasonShellMenuExtrasPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-096", StatisticsHubLiveContentPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-098", HistoryHubDepthPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-099", ShellHamburgerDrawerPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-100", HubRouteShellRetentionPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-102", RecruitingHubFocusPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-104", SeasonReviewMenuPolicy.MEGA_ID)
        assertTrue(InSeasonShellMenuExtrasPolicy.includeStaffAndAutomation(CalendarPhase.RegularSeason))
        assertFalse(InSeasonShellMenuExtrasPolicy.includeStaffAndAutomation(CalendarPhase.Offseason))
        assertTrue(ShellHamburgerDrawerPolicy.usesModalNavigationDrawer())
        assertTrue(StatisticsHubLiveContentPolicy.isLiveHubRoute())
        assertEquals(RecruitingHubTab.TransferPortal, RecruitingHubFocusPolicy.portalDeepLinkTab)

        val root = repoRoot()
        val shell = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(shell.contains("InSeasonShellMenuExtrasPolicy"))
        val chrome = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellChrome.kt",
        ).readText()
        assertTrue(chrome.contains("ModalNavigationDrawer"))
        val app = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/DynastyApp.kt",
        ).readText()
        assertTrue(app.contains("recruitingFocusProspectId"))
        assertTrue(app.contains("onOpenPortalEntrantFocus"))
        assertTrue(app.contains("popBackToInShellDashboard()"))
        val season = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/seasonreview/SeasonReviewScreen.kt",
        ).readText()
        assertTrue(season.contains("SeasonReviewMenuPolicy"))
        assertTrue(season.contains("buildDynastyHubMenu"))
        val news = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/news/NewsHubScreen.kt",
        ).readText()
        assertTrue(news.contains("focusProspectIdForRecruiting"))
        val history = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/history/HistoryHubScreen.kt",
        ).readText()
        assertTrue(history.contains("HistoryHubDepthPolicy"))
    }

    @Test
    fun historyDepthAddsChampionLines() {
        val snapshot = HistoryHubSnapshot(
            dynastyId = "d",
            seasonIndex = 1,
            yearCards = listOf(
                HistoryYearCard(
                    seasonIndex = 0,
                    ncbaChampionSchool = "Duke",
                    ncbaMvpLine = null,
                    nationalHofInductees = emptyList(),
                    goatNumberOne = null,
                    recordsNoted = listOf("3PT record"),
                ),
            ),
            goatOverall = emptyList(),
            goatByPosition = emptyMap(),
        )
        assertTrue(HistoryHubDepthPolicy.majorRecordFeedLines(snapshot).any { it.contains("Duke") })
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
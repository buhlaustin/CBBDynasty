package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.calendar.CalendarPhase
import dev.novux.dynasty.cbb.core.league.LeagueStandingsState
import dev.novux.dynasty.cbb.core.news.NewsHeadlineKind
import dev.novux.dynasty.cbb.core.news.NewsHeadlineKindChipPolicy
import dev.novux.dynasty.cbb.core.news.NewsHubStoriesRowPolicy
import dev.novux.dynasty.cbb.core.news.NewsPortalHeadlineKindPolicy
import dev.novux.dynasty.cbb.core.news.PollNetMovementNewsEngine
import dev.novux.dynasty.cbb.core.notification.NotificationFeedPortalOffseasonPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationTaskFeedDedupPolicy
import dev.novux.dynasty.cbb.core.notification.NotificationWeekAdvanceAppendPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MegaBatch26PathTest {
    @Test
    fun newsAndAdvanceBatchWired() {
        assertEquals("MEGA-SHELL-128", NotificationWeekAdvanceAppendPolicy.MEGA_ID)
        assertTrue(NotificationWeekAdvanceAppendPolicy.ONE_ROW_PER_EVENT)
        assertEquals("MEGA-SHELL-132", NotificationWeekAdvanceAppendPolicy.PIPELINE_STAGE_ID)
        assertEquals("MEGA-SHELL-130", NotificationFeedPortalOffseasonPolicy.MEGA_ID)
        assertTrue(
            NotificationFeedPortalOffseasonPolicy.portalNotificationWindow(CalendarPhase.Offseason, 3),
        )
        assertEquals("MEGA-SHELL-133", NotificationTaskFeedDedupPolicy.MEGA_ID)
        assertTrue(NotificationTaskFeedDedupPolicy.includeDisciplineFeedRow())
        assertEquals("MEGA-SHELL-134", NewsHeadlineKindChipPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-135", NewsHubStoriesRowPolicy.MEGA_ID)
        assertEquals("MEGA-SHELL-136", NewsPortalHeadlineKindPolicy.MEGA_ID)
        assertTrue(NewsPortalHeadlineKindPolicy.isPortalKind(NewsHeadlineKind.PortalCommit))
        assertEquals("MEGA-SHELL-137", PollNetMovementNewsEngine.MEGA_ID)

        val headline = PollNetMovementNewsEngine.headlineForWeek(
            standings = LeagueStandingsState(
                seasonIndex = 1,
                netRankByTeam = mapOf("duke" to 1, "unc" to 5),
                lastCommittedRsWeek = 4,
            ),
            rsWeekIndex = 4,
            teamDisplayName = { it.uppercase() },
            userTeamId = "unc",
        )
        assertNotNull(headline)
        assertEquals(NewsHeadlineKind.PollNetMovement, headline!!.kind)

        val root = repoRoot()
        val save = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/save/SaveSlotRepository.kt",
        ).readText()
        assertTrue(save.contains("dashboardNotificationWeekAdvanceAppender.appendAfterWeekAdvance"))
        val newsRepo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/news/NewsFeedRepository.kt",
        ).readText()
        assertTrue(newsRepo.contains("PollNetMovementNewsEngine"))
        val hub = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/news/NewsHubScreen.kt",
        ).readText()
        assertTrue(hub.contains("NewsHubStoriesRowPolicy"))
        assertTrue(hub.contains("newsHeadlineKindLabel"))
        assertTrue(
            DynastyWeekAdvancePipelineStages.confirmedAdvanceStageMarkers.contains(
                "dashboardNotificationWeekAdvanceAppender.appendAfterWeekAdvance",
            ),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
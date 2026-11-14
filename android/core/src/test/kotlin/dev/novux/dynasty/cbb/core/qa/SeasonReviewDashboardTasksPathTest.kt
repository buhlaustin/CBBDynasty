package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-033] */
class SeasonReviewDashboardTasksPathTest {
    @Test
    fun seasonReview_wiresRequiredDashboardTasks() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/seasonreview/SeasonReviewScreen.kt",
        ).readText()
        val tasks = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/SeasonReviewRequiredDashboardTasks.kt",
        ).readText()
        assertTrue(tasks.contains("SeasonReviewAcknowledge"))
        assertTrue(screen.contains("SeasonReviewRequiredDashboardTasks"))
        assertTrue(screen.contains("DashboardRequiredTasksPanel"))
        assertTrue(screen.contains("seasonReviewProgressRepository"))
        assertTrue(screen.contains("season_review_calendar_banner"))
        assertTrue(screen.contains("season_review_shell_header_hint"))
        assertTrue(screen.contains("text = weekHeader"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

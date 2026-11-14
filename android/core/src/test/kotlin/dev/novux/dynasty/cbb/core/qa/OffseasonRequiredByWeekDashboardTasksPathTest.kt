package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-028] */
class OffseasonRequiredByWeekDashboardTasksPathTest {
    @Test
    fun offseasonScreen_wiresRequiredTasksByWeek() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        val aggregator = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/OffseasonRequiredDashboardTasks.kt",
        ).readText()
        assertTrue(aggregator.contains("OffseasonRosterLockRequiredDashboardTasks"))
        assertTrue(aggregator.contains("OffseasonConferenceOffersRequiredDashboardTasks"))
        assertTrue(screen.contains("OffseasonRequiredDashboardTasks"))
        assertTrue(screen.contains("OffseasonRosterLockRequiredDashboardTasks"))
        assertTrue(screen.contains("OffseasonConferenceOffersRequiredDashboardTasks"))
        assertTrue(screen.contains("DashboardRequiredTasksPanel"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

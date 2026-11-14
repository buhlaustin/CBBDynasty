package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-029], [MEGA-FRAME-102] */
class OffseasonWeekOneDashboardTasksPathTest {
    @Test
    fun offseasonWeekOne_wiresRequiredDashboardTasks() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        val tasks = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/OffseasonWeekOneRequiredDashboardTasks.kt",
        ).readText()
        assertTrue(tasks.contains("WeekOneProDay"))
        assertTrue(tasks.contains("WeekOneTransferRetention"))
        assertTrue(
            screen.contains("OffseasonWeekOneRequiredDashboardTasks") ||
                screen.contains("OffseasonRequiredDashboardTasks"),
        )
        assertTrue(screen.contains("DashboardRequiredTasksPanel"))
        assertTrue(screen.contains("weekOneNavigationFor"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

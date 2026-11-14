package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.dashboard.OffseasonPortalDashboardTasksPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-061] */
class OffseasonPortalOptionalTasksPathTest {
    @Test
    fun offseasonPortalOptionalTasks_wiredOnOffseasonScreen() {
        assertEquals("MEGA-RECR-043", OffseasonPortalDashboardTasksPolicy.MEGA_ID)
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        val tasks = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/OffseasonOptionalDashboardTasks.kt",
        ).readText()
        assertTrue(screen.contains("OffseasonPortalOptionalTasksPolicy"))
        assertTrue(screen.contains("buildOffseasonPortalOptionalTasks"))
        assertTrue(screen.contains("dashboard_offseason_portal_optional_heading"))
        assertTrue(screen.contains("dashboard_offseason_portal_tasks_cadence_hint"))
        assertTrue(screen.contains("PortalCyclePolicy.portalWindowClosed"))
        assertTrue(screen.contains("dashboard_offseason_portal_window_closed_body"))
        assertTrue(screen.contains("PortalPipelineEntrantDashboardPolicy"))
        assertTrue(tasks.contains("PortalPipelineEntrant"))
        assertTrue(tasks.contains("PortalWindow"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.advance.ProvisionalGameReplayPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class ProvisionalGameReplayPathTest {
    @Test
    fun dashboardAndCalendarExposeReplayBeforeAdvance() {
        val root = repoRoot()
        val policy = root.resolve(POLICY).readText()
        val tasks = root.resolve(TASKS).readText()
        val calendar = root.resolve(CALENDAR).readText()
        val panel = root.resolve(PANEL).readText()
        assertTrue(policy.contains(ProvisionalGameReplayPolicy.MEGA_GDD_ID))
        assertTrue(tasks.contains("ReplayStagedWeekGame"))
        assertTrue(calendar.contains("provisionalReplay"))
        assertTrue(calendar.contains("shell_calendar_replay_staged_game"))
        assertTrue(panel.contains("ReplayProvisionalWeekGame"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val POLICY =
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/advance/ProvisionalGameReplayPolicy.kt"
        private const val TASKS =
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/InSeasonOptionalDashboardTasks.kt"
        private const val CALENDAR =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt"
        private const val PANEL =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dashboard/DashboardOptionalTasksPanel.kt"
    }
}
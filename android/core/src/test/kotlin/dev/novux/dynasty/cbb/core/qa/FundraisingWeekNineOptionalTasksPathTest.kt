package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-065] */
class FundraisingWeekNineOptionalTasksPathTest {
    @Test
    fun weekNine_surfacesFundraisingOptionalTasks() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        assertTrue(screen.contains("OffseasonWeekNineOptionalDashboardTasks"))
        assertTrue(screen.contains("FundraisingWeekNineBottomSheet"))
        assertTrue(screen.contains("onFundraisingWeekNineOpen"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

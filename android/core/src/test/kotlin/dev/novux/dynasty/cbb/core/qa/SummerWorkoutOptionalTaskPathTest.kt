package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-063] */
class SummerWorkoutOptionalTaskPathTest {
    @Test
    fun weekSeven_surfacesOptionalReportTaskAndSheet() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        assertTrue(screen.contains("OffseasonWeekSevenOptionalDashboardTasks"))
        assertTrue(screen.contains("SummerWorkoutReportBottomSheet"))
        assertTrue(screen.contains("onSummerWorkoutReportOpen"))
        assertTrue(
            root.resolve(
                "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/dashboard/OffseasonWeekSevenOptionalDashboardTasks.kt",
            ).toFile().exists(),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

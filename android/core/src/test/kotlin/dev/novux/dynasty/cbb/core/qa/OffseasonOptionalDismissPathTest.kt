package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-101] */
class OffseasonOptionalDismissPathTest {
    @Test
    fun offseason_persistsOptionalDismissKeys() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
        ).readText()
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/dashboard/DashboardOptionalDismissRepository.kt",
        ).readText()
        assertTrue(screen.contains("dashboardOptionalDismissRepository"))
        assertTrue(screen.contains("recordDismiss"))
        assertTrue(repo.contains("MEGA-FRAME-101"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
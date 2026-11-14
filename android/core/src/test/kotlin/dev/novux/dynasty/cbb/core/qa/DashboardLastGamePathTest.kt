package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-038] */
class DashboardLastGamePathTest {
    @Test
    fun inSeasonDashboard_wiresLastGameCard() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(screen.contains("DashboardLastGameCard"))
        assertTrue(screen.contains("DashboardLastUserGameResolver"))
        assertTrue(screen.contains("dashboardLastGameRepository"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

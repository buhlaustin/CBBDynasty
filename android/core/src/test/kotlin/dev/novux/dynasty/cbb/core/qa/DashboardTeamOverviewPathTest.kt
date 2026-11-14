package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-034]–[036] */
class DashboardTeamOverviewPathTest {
    @Test
    fun dynastyShell_wiresTeamOverviewCard() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        val card = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dashboard/DashboardTeamOverviewCard.kt",
        ).readText()
        assertTrue(card.contains("onOpenRankings"))
        assertTrue(card.contains("postseasonSeedLabel"))
        assertTrue(screen.contains("DashboardTeamOverviewCard"))
        assertTrue(screen.contains("DashboardPostseasonSeedResolver"))
        assertTrue(screen.contains("onOpenRankings"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

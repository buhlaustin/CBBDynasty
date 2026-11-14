package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-053] */
class MarchMayhemMenuVisibilityPathTest {
    @Test
    fun marchMayhemMenuGatedOnSelectionSundayPublished() {
        val root = repoRoot()
        val policy = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/hubs/DynastyHubMenuPolicy.kt",
        ).readText()
        val menu = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/hubs/DynastyHubMenu.kt",
        ).readText()
        val shell = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(policy.contains("selectionSundayPublished"))
        assertTrue(menu.contains("selectionSundayPublished"))
        assertTrue(shell.contains("selectionSundayPublished"))
        assertTrue(shell.contains("selectionSunday != null"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

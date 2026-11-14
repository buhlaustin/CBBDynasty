package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-044] */
class ShellRosterSectionsPathTest {
    @Test
    fun shellRosterTab_hasSectionHeaders() {
        val root = repoRoot()
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellTabContent.kt",
        ).readText()
        assertTrue(tab.contains("ShellRosterSectionPartition"))
        assertTrue(tab.contains("shell_roster_section_starters"))
        assertTrue(tab.contains("shell_roster_section_reserves"))
        assertTrue(tab.contains("shell_roster_section_bench"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

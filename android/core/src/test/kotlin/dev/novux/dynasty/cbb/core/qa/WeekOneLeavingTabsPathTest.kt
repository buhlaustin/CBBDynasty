package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-059] */
class WeekOneLeavingTabsPathTest {
    @Test
    fun playersLeavingPanel_wiresRosterTransferTabs() {
        val root = repoRoot()
        val panel = root.resolve(PlayerLeavingRetentionPath.SCREEN).readText()
        val roster = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/roster/DynastyRosterRepository.kt",
        ).readText()
        assertTrue(panel.contains("WeekOneLeavingTabPolicy"))
        assertTrue(panel.contains("leaving_tab_roster"))
        assertTrue(panel.contains("leaving_tab_transfer"))
        assertTrue(panel.contains("leaving_encourage_transfer"))
        assertTrue(panel.contains("leaving_convince_stay"))
        assertTrue(panel.contains("leaving_week_tabs_help"))
        assertTrue(roster.contains("PortalWeekOnePartition"))
        assertTrue(roster.contains("WeekOneLeavingActions"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

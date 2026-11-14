package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-146] */
class ConferenceTournamentWeekHeaderPathTest {
    @Test
    fun shellHeaderUsesPackMaxConfTourneyWeeks() {
        val root = repoRoot()
        val shell = root.resolve(SHELL).readText()
        val label = root.resolve(LABEL).readText()
        assertTrue(shell.contains("ConferenceTournamentCalendar.leagueMaxConfTourneyWeeks"))
        assertTrue(shell.contains("CalendarWeekLabel.display"))
        assertTrue(label.contains("Conference Tournament Week"))
        assertTrue(label.contains("leagueMaxConfTourneyWeeks"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val SHELL =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt"
        private const val LABEL =
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/calendar/CalendarWeekLabel.kt"
    }
}
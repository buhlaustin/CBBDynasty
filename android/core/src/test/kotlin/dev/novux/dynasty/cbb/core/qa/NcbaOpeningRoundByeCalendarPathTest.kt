package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-153] */
class NcbaOpeningRoundByeCalendarPathTest {
    @Test
    fun repository_andCalendarTabSurfaceOpeningRoundBye() {
        val root = repoRoot()
        val repo = root.resolve(REPO).readText()
        val tab = root.resolve(TAB).readText()
        assertTrue(repo.contains("PostseasonUserCalendar"))
        assertTrue(repo.contains("userSkipsNcbaOpeningRound"))
        assertTrue(tab.contains("NcbaOpeningRoundBye"))
        assertTrue(tab.contains("shell_calendar_ncba_opening_round_bye"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }

    companion object {
        private const val REPO =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/calendar/UserCalendarRepository.kt"
        private const val TAB =
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt"
    }
}
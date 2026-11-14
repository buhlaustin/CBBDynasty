package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-046] */
class UserCalendarYourGamesPathTest {
    @Test
    fun calendarTab_userScheduleRepositoryAndCopy() {
        val root = repoRoot()
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt",
        ).readText()
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/calendar/UserCalendarRepository.kt",
        ).readText()
        val shell = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(tab.contains("loadCalendarRows"))
        assertTrue(tab.contains("shell_calendar_your_games_only"))
        assertTrue(tab.contains("shell_calendar_no_schedule"))
        assertTrue(tab.contains("UserCalendarSchedulePolicy"))
        assertTrue(repo.contains("scheduleRepository.loadPlan"))
        assertTrue(repo.contains("return emptyList()"))
        assertTrue(shell.contains("DynastyShellCalendarTab"))
        assertTrue(!shell.contains("DynastyShellCalendarTabPlaceholder"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

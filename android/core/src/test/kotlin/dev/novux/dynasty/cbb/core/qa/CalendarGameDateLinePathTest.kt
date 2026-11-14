package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-017] */
class CalendarGameDateLinePathTest {
    @Test
    fun gameEntryAndUiLineIncludeDate() {
        val root = repoRoot()
        val models = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/calendar/UserCalendarTabModels.kt",
        ).readText()
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt",
        ).readText()
        val strings = root.resolve("android/app/src/main/res/values/strings.xml").readText()
        assertTrue(models.contains("gameDateLabel"))
        assertTrue(tab.contains("game.gameDateLabel"))
        assertTrue(strings.contains("shell_calendar_game_line"))
        assertTrue(strings.contains("%5\$s"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-048] */
class CalendarGameTapPathTest {
    @Test
    fun calendarGameLinesClickable_previewAndBoxScore() {
        val root = repoRoot()
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt",
        ).readText()
        val shell = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/shell/DynastyShellScreen.kt",
        ).readText()
        assertTrue(tab.contains("Modifier.clickable"))
        assertTrue(tab.contains("onOpenGamePreview"))
        assertTrue(tab.contains("PostgameBoxScorePanel"))
        assertTrue(tab.contains("UserCalendarGameEnricher") || tab.contains("playedResult"))
        assertTrue(shell.contains("GamePreviewFromCalendar.launch"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

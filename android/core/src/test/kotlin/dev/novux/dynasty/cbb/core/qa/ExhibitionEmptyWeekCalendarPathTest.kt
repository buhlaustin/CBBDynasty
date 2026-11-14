package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-019] */
class ExhibitionEmptyWeekCalendarPathTest {
    @Test
    fun calendarShowsExhibitionEmptyCopy() {
        val root = repoRoot()
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt",
        ).readText()
        val strings = root.resolve("android/app/src/main/res/values/strings.xml").readText()
        assertTrue(tab.contains("CalendarPhase.Exhibition"))
        assertTrue(tab.contains("shell_calendar_exhibition_empty"))
        assertTrue(strings.contains("shell_calendar_exhibition_empty"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

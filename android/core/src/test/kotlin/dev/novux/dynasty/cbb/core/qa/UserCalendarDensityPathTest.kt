package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class UserCalendarDensityPathTest {
    @Test
    fun calendarTabShowsWeekDensity() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt",
        ).readText()
        assertTrue(tab.contains("UserCalendarScheduleDensityPolicy"))
        assertTrue(tab.contains("shell_calendar_week_density"))
    }
}
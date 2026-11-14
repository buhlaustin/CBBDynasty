package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class OffseasonCalendarEmptyPathTest {
    @Test
    fun offseasonUsesPhaseSpecificEmptyCalendarCopy() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt",
        ).readText()
        val strings = root.resolve("android/app/src/main/res/values/strings.xml").readText()
        assertTrue(tab.contains("CalendarPhase.Offseason"))
        assertTrue(tab.contains("shell_calendar_offseason_no_games"))
        assertTrue(strings.contains("shell_calendar_offseason_no_games"))
    }
}
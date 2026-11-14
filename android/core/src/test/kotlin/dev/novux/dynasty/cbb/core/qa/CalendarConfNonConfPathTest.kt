package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-047] */
class CalendarConfNonConfPathTest {
    @Test
    fun calendarTab_confNonConfBadgesAndSections() {
        val root = repoRoot()
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellCalendarTab.kt",
        ).readText()
        assertTrue(tab.contains("calendarKindBucket"))
        assertTrue(tab.contains("shell_calendar_section_conf"))
        assertTrue(tab.contains("AssistChip(onClick = if (game.isTappable) onClick else"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

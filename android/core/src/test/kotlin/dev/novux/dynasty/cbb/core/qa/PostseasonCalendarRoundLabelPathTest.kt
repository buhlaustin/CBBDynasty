package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-016], [MEGA-FRAME-147] */
class PostseasonCalendarRoundLabelPathTest {
    @Test
    fun weekLabelUsesNcbaRoundNames() {
        val root = repoRoot()
        val label = root.resolve(
            "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/calendar/CalendarWeekLabel.kt",
        ).readText()
        assertTrue(label.contains("PostseasonCalendar.displayWeekHeader"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class PreseasonStartSeasonProgressPathTest {
    @Test
    fun startSeasonButtonShowsInProgressState() {
        val root = generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        val strings = root.resolve("android/app/src/main/res/values/strings.xml").readText()
        assertTrue(screen.contains("startSeasonInProgress"))
        assertTrue(screen.contains("preseason_start_season_in_progress"))
        assertTrue(strings.contains("preseason_start_season_in_progress"))
    }
}
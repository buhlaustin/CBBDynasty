package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-045] */
class ShellRosterPlayerCardPathTest {
    @Test
    fun offseasonPreseasonAndCarousel_rosterOpensPlayerCard() {
        val root = repoRoot()
        val paths = listOf(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/CoachingCarouselScreen.kt",
        )
        paths.forEach { relative ->
            val source = root.resolve(relative).readText()
            assertTrue("$relative wires cardPlayer", source.contains("var cardPlayer"))
            assertTrue("$relative shows preview sheet", source.contains("PlayerCardPreviewSheet"))
            assertTrue("$relative roster tab sets cardPlayer", source.contains("onPlayerClick = { cardPlayer = it }"))
            assertFalse("$relative has no-op roster click", source.contains("onPlayerClick = {}"))
        }
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

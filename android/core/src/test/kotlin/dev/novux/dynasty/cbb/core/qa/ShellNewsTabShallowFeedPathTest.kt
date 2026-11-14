package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-049] */
class ShellNewsTabShallowFeedPathTest {
    @Test
    fun phaseShells_useShallowNewsHeadlines_notPlaceholder() {
        val root = repoRoot()
        val paths = listOf(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/OffseasonScreen.kt",
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/offseason/CoachingCarouselScreen.kt",
        )
        paths.forEach { relative ->
            val source = root.resolve(relative).readText()
            assertTrue("$relative loads shell news", source.contains("rememberShellNewsFeed"))
            assertTrue("$relative renders news tab", source.contains("DynastyShellNewsTab("))
            assertFalse("$relative uses news placeholder", source.contains("DynastyShellNewsTabPlaceholder"))
        }
        assertTrue(
            root.resolve(
                "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/ShellShallowNewsFeed.kt",
            ).toFile().exists(),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

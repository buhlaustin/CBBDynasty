package dev.novux.dynasty.cbb.core.qa

import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-050] */
class ShellNewsShallowCapPathTest {
    @Test
    fun shellNewsTab_surfacesCapAndHubLink() {
        val root = repoRoot()
        val tab = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/DynastyShellTabContent.kt",
        ).readText()
        val repo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/news/NewsFeedRepository.kt",
        ).readText()
        assertTrue(tab.contains("shell_news_shallow_cap"))
        assertTrue(tab.contains("ShallowNewsFeedSnapshot"))
        assertTrue(tab.contains("shell_news_open_hub"))
        assertTrue(repo.contains("shallowFeedSnapshot"))
        assertTrue(
            root.resolve(
                "android/core/src/main/kotlin/dev/novux/dynasty/cbb/core/news/ShallowNewsFeedSnapshot.kt",
            ).toFile().exists(),
        )
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}

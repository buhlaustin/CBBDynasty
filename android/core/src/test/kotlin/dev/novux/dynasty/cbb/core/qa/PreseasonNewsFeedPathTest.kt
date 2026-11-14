package dev.novux.dynasty.cbb.core.qa

import dev.novux.dynasty.cbb.core.preseason.PreseasonNewsFeedPolicy
import java.nio.file.Paths
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [MEGA-FRAME-077] */
class PreseasonNewsFeedPathTest {
    @Test
    fun preseasonNews_refreshesAfterSeedAndGatesSpotlight() {
        val root = repoRoot()
        val screen = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/preseason/PreseasonScreen.kt",
        ).readText()
        val newsRepo = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/data/news/NewsFeedRepository.kt",
        ).readText()
        val shallow = root.resolve(
            "android/app/src/main/kotlin/dev/novux/dynasty/cbb/ui/dynasty/ShellShallowNewsFeed.kt",
        ).readText()
        assertEquals("MEGA-FRAME-077", PreseasonNewsFeedPolicy.MEGA_FRAME_ID)
        assertTrue(screen.contains("refreshKey = incomingClassSeedComplete"))
        assertTrue(screen.contains("publishRecruitingSpotlightWhenPoolReady"))
        assertTrue(screen.contains("incomingClassSeedRepository.isSeeded"))
        assertTrue(screen.contains("preseason_shell_header_hint"))
        assertTrue(screen.contains("shell_news_preseason_awaiting_seed"))
        assertTrue(newsRepo.contains("isSeeded"))
        assertTrue(shallow.contains("refreshKey"))
    }

    private fun repoRoot() =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { it.resolve("android").resolve("settings.gradle.kts").toFile().exists() }
}
